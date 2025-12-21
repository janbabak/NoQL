package com.janbabak.noqlbackend.service;

import com.janbabak.noqlbackend.error.exception.PlotScriptExecutionException;
import com.janbabak.noqlbackend.model.Settings;
import com.janbabak.noqlbackend.model.entity.Database;
import com.janbabak.noqlbackend.service.database.DatabaseCredentialsEncryptionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Responsible for generating plots/charts/graphs.
 */
@Service
@Slf4j
public class PlotService {

    private static final String PLOT_IMAGE_FILE_EXTENSION = ".png";
    private static final String PLOTS_DIRECTORY = "plots";
    private static final String PLOT_SCRIPT_NAME = "plot.py";
    private static final Long GENERATE_PLOT_TIMEOUT_SECONDS = 10L;
    private static final String WORKING_DIRECTORY_NAME = "plotService";
    public static final String PLOT_DIRECTORY_DOCKER_PATH = "./plotService/plots";
    public static Path PLOTS_DIR_PATH;
    @SuppressWarnings("FieldCanBeLocal")
    private static Path SCRIPT_PATH;
    private static final Path PLOTS_DIR_URL_PATH = Path.of("/static/images");
    @SuppressWarnings("FieldCanBeLocal")
    private static File workingDirectory;
    @SuppressWarnings("FieldCanBeLocal")
    private static File plotsDirectory;
    private static File script;
    private final Settings settings;
    private final DatabaseCredentialsEncryptionService encryptionService;

    /**
     * Create working directory and plot script
     *
     * @param settings            application settings
     * @param encryptionService   service for encrypting/decrypting database credentials
     * @param appWorkingDirectory path to the working directory (when running the app from the IDE default value from
     *                            the application.yaml is used, when running using the
     *                            {@code ./backend/gradlew -p backend bootRun} command, the value from build.gradle is
     *                            used)
     */
    PlotService(Settings settings,
                DatabaseCredentialsEncryptionService encryptionService,
                @Value("${app.config.workingDirectory}") String appWorkingDirectory) {

        log.debug("Plot service working directory is: {}", appWorkingDirectory);

        this.settings = settings;
        this.encryptionService = encryptionService;

        // create working and plot directories
        workingDirectory = Path.of(appWorkingDirectory + "/" + WORKING_DIRECTORY_NAME).toFile();
        if (!workingDirectory.exists() && !workingDirectory.mkdirs()) {
            logAndThrowRuntimeError("Cannot create working directory in plot service");
        }
        PLOTS_DIR_PATH = Path.of(workingDirectory.getPath() + "/" + PLOTS_DIRECTORY);
        plotsDirectory = PLOTS_DIR_PATH.toFile();
        if (!plotsDirectory.exists() && !plotsDirectory.mkdirs()) {
            logAndThrowRuntimeError("Cannot create plot directory in plot service");
        }

        // create script
        SCRIPT_PATH = Path.of(workingDirectory.getPath() + "/" + PLOT_SCRIPT_NAME);
        script = SCRIPT_PATH.toFile();
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
     * @param fileName      name of the file to save plot
     * @throws PlotScriptExecutionException script returned not successful return code or failed
     */
    public void generatePlot(String scriptContent, Database database, String fileName)
            throws PlotScriptExecutionException {

        try {
            createPlotScript(replaceCredentialsInScript(scriptContent, database, fileName));
            log.debug("Starting process: docker exec {} python ./{}/{}",
                    settings.getPlotServiceContainerName(), WORKING_DIRECTORY_NAME, PLOT_SCRIPT_NAME);
            ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", "docker exec %s python ./%s/%s"
                    .formatted(settings.getPlotServiceContainerName(), WORKING_DIRECTORY_NAME, PLOT_SCRIPT_NAME));

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
                    throw new PlotScriptExecutionException(error.toString());
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
     * Create name of the file
     *
     * @param chatId    identifier of the chat
     * @param messageId identifier of the message
     * @return name of the file
     */
    public static String createFileName(UUID chatId, UUID messageId) {
        return chatId + "--" + messageId + PLOT_IMAGE_FILE_EXTENSION;
    }

    /**
     * Create URL path to the file
     *
     * @param fileName name of the file with extension
     * @return URL path to the file
     */
    public static String createFileUrl(String fileName) {
        return PLOTS_DIR_URL_PATH + "/" + fileName;
    }

    /**
     * Create URL path to the file
     *
     * @param chatId    identifier of the chat
     * @param messageId identifier of the message
     * @return URL path to the file
     */
    public static String createFileUrl(UUID chatId, UUID messageId) {
        return createFileUrl(createFileName(chatId, messageId));
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
        scriptContent = scriptContent.replace(QueryService.USER_PLACEHOLDER, database.getUserName());
        scriptContent = scriptContent.replace(QueryService.DATABASE_PLACEHOLDER, database.getDatabase());
        scriptContent = scriptContent.replace(QueryService.PLOT_FILE_NAME_PLACEHOLDER, fileName);
        scriptContent = scriptContent.replace(QueryService.PASSWORD_PLACEHOLDER,
                encryptionService.decryptCredentials(database.getPassword()));
        scriptContent = scriptContent.replace(QueryService.HOST_PLACEHOLDER,
                database.getHost().equals("localhost") ? QueryService.DOCKER_LOCALHOST : database.getHost());
        return scriptContent.replace(QueryService.PORT_PLACEHOLDER, database.getPort().toString());
    }

    /**
     * Delete plots associated with a chat. This means all plots with names that start with chatId.
     *
     * @param prefix common prefix of all plots to delete.
     */
    public void deletePlots(String prefix) {
        try (Stream<Path> filesStream = Files.list(PLOTS_DIR_PATH)) {
            filesStream
                    .filter(path -> path.getFileName().toString().startsWith(prefix))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            log.error("Delete plot failed, path={}, message={}", path, e.getMessage());
                        }
                    });
        } catch (IOException e) {
            log.error("Failed to list files in directory, message={}", e.getMessage());
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