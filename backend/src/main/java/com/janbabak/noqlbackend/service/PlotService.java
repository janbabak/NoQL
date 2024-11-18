package com.janbabak.noqlbackend.service;

import com.janbabak.noqlbackend.error.exception.PlotScriptExecutionException;
import com.janbabak.noqlbackend.model.Settings;
import com.janbabak.noqlbackend.model.entity.Database;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Responsible for generating plots/charts/graphs.
 */
@Service
@Slf4j
public class PlotService {

    public static final String PLOT_IMAGE_FILE_EXTENSION = ".png";
    private static final String PLOTS_DIRECTORY = "plots";
    private static final String PLOT_SCRIPT_NAME = "plot.py";
    private static final Long GENERATE_PLOT_TIMEOUT_SECONDS = 5L;
    private static final Path WORKING_DIRECTORY_PATH = Path.of("./plotService");
    public static final Path plotsDirPath = Path.of(WORKING_DIRECTORY_PATH + "/" + PLOTS_DIRECTORY);
    private static final Path scriptPath = Path.of(WORKING_DIRECTORY_PATH + "/" + PLOT_SCRIPT_NAME);
    @SuppressWarnings("FieldCanBeLocal")
    private static File workingDirectory;
    @SuppressWarnings("FieldCanBeLocal")
    private static File plotsDirectory;
    private static File script;
    private final Settings settings;

    /**
     * Get path to plot of chat
     *
     * @param chatId chat identifier
     * @return path to the plot (does not verify whether the file exist).
     */
    public static Path getPlotPath(UUID chatId) {
        return Path.of(plotsDirPath + "/" + chatId + PLOT_IMAGE_FILE_EXTENSION);
    }

    /**
     * Create working directory and plot script
     */
    PlotService(Settings settings) {
        this.settings = settings;

        // create working and plot directories
        workingDirectory = WORKING_DIRECTORY_PATH.toFile();
        if (!workingDirectory.exists() && !workingDirectory.mkdirs()) {
            logAndThrowRuntimeError("Cannot create working directory in plot service");
        }
        plotsDirectory = plotsDirPath.toFile();
        if (!plotsDirectory.exists() && !plotsDirectory.mkdirs()) {
            logAndThrowRuntimeError("Cannot create plot directory in plot service");
        }

        // create script
        script = scriptPath.toFile();
        try {
            if (!script.exists() && !script.createNewFile()) {
                logAndThrowRuntimeError("Cannot create plot script");
            }
        } catch (IOException e) {
            logAndThrowRuntimeError("Cannot create plot script: " + e.getMessage());
        }
    }

    /**
     * Generate plot
     *
     * @param scriptContent content of python file responsible for plot generation (code)
     * @param database      database object - use its real credentials instead of placeholders
     * @param fileName      name of the generated file
     * @throws PlotScriptExecutionException script returned not successful return code or failed
     */
    public void generatePlot(String scriptContent, Database database, String fileName)
            throws PlotScriptExecutionException {

        try {
            createPlotScript(replaceCredentialsInScript(scriptContent, database, fileName));
            ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", "docker exec %s python %s"
                    .formatted(settings.getPlotServiceContainerName(), scriptPath));

            Process process = processBuilder.start();

            // read output and return it if failure
            BufferedReader outputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder output = new StringBuilder();
            StringBuilder error = new StringBuilder();
            String line;
            while ((line = outputReader.readLine()) != null) {
                output.append(line);
            }
            outputReader.close();

            while ((line = errorReader.readLine()) != null) {
                error.append(line);
            }
            errorReader.close();

            try {
                process.waitFor(GENERATE_PLOT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                int exitCode = process.exitValue();
                process.destroy();
                if (exitCode != 0) { // fail
                    log.error("Plot scrip execution failed. exit code: {}, output: '{}', error: '{}'",
                            exitCode, output, error);
                    throw new PlotScriptExecutionException(output.toString());
                }
            } catch (InterruptedException e) {
                log.error("Plot script execution failed. output: '{}', error: '{}', exception: '{}'",
                        output, error, e.getMessage());
                throw new PlotScriptExecutionException(output.toString());
            }
        } catch (IOException e) {
            throw new PlotScriptExecutionException(e.getMessage());
        }
    }

    /**
     * Replace placeholders in the script with real credentials.
     *
     * @param scriptContent script with placeholders
     * @param database      database with real credentials
     * @param fileName      name of the file to save plot
     * @return script with real credentials
     */
    String replaceCredentialsInScript(String scriptContent, Database database, String fileName) {
        scriptContent = scriptContent.replace(QueryService.PASSWORD_PLACEHOLDER, database.getPassword());
        scriptContent = scriptContent.replace(QueryService.USER_PLACEHOLDER, database.getUserName());
        scriptContent = scriptContent.replace(QueryService.DATABASE_PLACEHOLDER, database.getDatabase());
        scriptContent = scriptContent.replace(QueryService.PLOT_FILE_NAME_PLACEHOLDER, fileName);
        scriptContent = scriptContent.replace(QueryService.HOST_PLACEHOLDER,
                database.getHost().equals("localhost") ? QueryService.DOCKER_LOCALHOST : database.getHost());
        return scriptContent.replace(QueryService.PORT_PLACEHOLDER, database.getPort().toString());
    }

    /**
     * Delete plot associated with a chat.
     *
     * @param chatId chat identifier
     */
    public void deletePlot(UUID chatId) {
        Path path = getPlotPath(chatId);

        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("Delete plot failed, chatId={}, message={}", chatId, e.getMessage());
        }
    }

    /**
     * Override content of script
     *
     * @param scriptContent script content (code)
     * @throws IOException cannot write into the file
     */
    private void createPlotScript(String scriptContent) throws IOException {
        FileWriter writer = new FileWriter(script, false);
        writer.write(scriptContent);
        writer.close();
    }

    private void logAndThrowRuntimeError(String errorMessage) {
        log.error(errorMessage);
        throw new RuntimeException(errorMessage);
    }
}