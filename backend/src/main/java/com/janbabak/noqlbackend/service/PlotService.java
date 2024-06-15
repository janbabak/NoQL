package com.janbabak.noqlbackend.service;

import com.janbabak.noqlbackend.error.exception.PlotScriptExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Responsible for generating plots/charts/graphs.
 */
@Service
@Slf4j
public class PlotService {

    public static final String PLOT_IMAGE_FILE_EXTENSION = ".png";
    private static final String PLOTS_DIRECTORY = "plots";
    private static final String PLOT_SCRIPT_NAME = "plot.py";
    private static final Path WORKING_DIRECTORY_PATH = Path.of("./plotService");
    public static final Supplier<Path> plotsDirPath =
            () -> Path.of(WORKING_DIRECTORY_PATH + "/" + PLOTS_DIRECTORY);
    private static final Supplier<Path> scriptPath =
            () -> Path.of(WORKING_DIRECTORY_PATH + "/" + PLOT_SCRIPT_NAME);
    @SuppressWarnings("FieldCanBeLocal")
    File workingDirectory;
    File plotsDirectory;
    File script;

    /**
     * Get path to plot of chat
     *
     * @param chatId chat identifier
     * @return path to the plot (does not verify whether the file exist).
     */
    public static Path getPlotPath(UUID chatId) {
        return Path.of(plotsDirPath.get() + "/" + chatId + PLOT_IMAGE_FILE_EXTENSION);
    }

    /**
     * Create working directory and plot script
     */
    PlotService() {
        // create working and plot directories
        workingDirectory = WORKING_DIRECTORY_PATH.toFile();
        if (!workingDirectory.exists() && !workingDirectory.mkdirs()) {
            logAndThrowRuntimeError("Cannot create working directory in plot service");
        }
        plotsDirectory = plotsDirPath.get().toFile();
        if (!plotsDirectory.exists() && !plotsDirectory.mkdirs()) {
            logAndThrowRuntimeError("Cannot create plot directory in plot service");
        }

        // create script
        script = scriptPath.get().toFile();
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
     * @throws PlotScriptExecutionException script returned not successful return code or failed
     */
    public void generatePlot(String scriptContent)
            throws PlotScriptExecutionException {

        try {
            createPlotScript(scriptContent);
            ProcessBuilder processBuilder = new ProcessBuilder("python3", scriptPath.get().toString());
            Process process = processBuilder.start();

            // read output and return it if failure
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            try {
                int exitCode = process.waitFor(); // TODO: fix
//                if (exitCode != 0) { // fail
//                    throw new PlotScriptExecutionException(output.toString());
//                }
            } catch (InterruptedException e) {
                throw new PlotScriptExecutionException(output.toString());
            }
        } catch (IOException e) {
            throw new PlotScriptExecutionException(e.getMessage());
        }
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
