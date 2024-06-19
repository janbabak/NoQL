package com.janbabak.noqlbackend.service;

import com.janbabak.noqlbackend.error.exception.PlotScriptExecutionException;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.service.containers.PlotServiceContainer;
import jakarta.annotation.PreDestroy;
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
    private final PlotServiceContainer plotServiceContainer;

    @SuppressWarnings("FieldCanBeLocal")
    private static File workingDirectory;

    @SuppressWarnings("FieldCanBeLocal")
    private static File plotsDirectory;

    private static File script;

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
    PlotService(PlotServiceContainer plotServiceContainer) {
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

        this.plotServiceContainer = plotServiceContainer;
        try {
            this.plotServiceContainer.start(0);
        } catch (InterruptedException e) {
            logAndThrowRuntimeError(e.getMessage());
        }
    }

    @PreDestroy // springboot bean "destructor" callback
    public void destroy() {
        plotServiceContainer.stop();
    }

    /**
     * Generate plot
     *
     * @param scriptContent content of python file responsible for plot generation (code)
     * @param database      database object - use its real credentials instead of placeholders
     * @param chatId        chat identifier - used for naming the plot
     * @throws PlotScriptExecutionException script returned not successful return code or failed
     */
    public void generatePlot(String scriptContent, Database database, UUID chatId)
            throws PlotScriptExecutionException {

        try {
            createPlotScript(replaceCredentialsInScript(scriptContent, database, chatId));
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "sh", "-c", "docker exec plot-service python " + scriptPath);
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
     * @param chatId        chat identifierf
     * @return script with real credentials
     */
    String replaceCredentialsInScript(String scriptContent, Database database, UUID chatId) {
        scriptContent = scriptContent.replace(QueryService.PASSWORD_PLACEHOLDER, database.getPassword());
        scriptContent = scriptContent.replace(QueryService.USER_PLACEHOLDER, database.getUserName());
        scriptContent = scriptContent.replace(QueryService.DATABASE_PLACEHOLDER, database.getDatabase());
        scriptContent = scriptContent.replace(QueryService.PLOT_FILE_NAME_PLACEHOLDER, chatId.toString());
        scriptContent = scriptContent.replace(QueryService.HOST_PLACEHOLDER,
                database.getHost().equals("localhost") ? QueryService.DOCKER_LOCALHOST : database.getHost());
        return scriptContent.replace(QueryService.PORT_PLACEHOLDER, database.getPort().toString());
    }

    // TODO: verify that is working after credentials are injected into the script

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
     * Used in tests. Warning: deletes all files in working directory including plots.
     */
    public static void deleteWorkingDirectory() {
        if (!script.delete() || !plotsDirectory.delete() || !workingDirectory.delete()) {
            log.error("Cannot clear working directory.");
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