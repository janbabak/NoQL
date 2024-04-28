package com.janbabak.noqlbackend.service;

import org.springframework.stereotype.Service;

import java.io.*;
import java.util.function.Supplier;

/**
 * Responsible for generating plots/charts/graphs.
 */
@Service
public class PlotService {

    private static final String WORKING_DIRECTORY_PATH = "./plotService";
    private static final String PLOT_SCRIPT_NAME = "plot.py";
    private static final String PLOTS_DIRECTORY = "plots";
    private final Supplier<String> scriptPath = () -> WORKING_DIRECTORY_PATH + "/" + PLOT_SCRIPT_NAME;
    @SuppressWarnings("FieldCanBeLocal")
    private final Supplier<String> plotsDirPath = () -> WORKING_DIRECTORY_PATH + "/" + PLOTS_DIRECTORY;

    File workingDirectory;
    File plotsDirectory;
    File script;

    /**
     * Create working directory and plot script
     */
    PlotService() {
        // create working and plot directories
        workingDirectory = new File(WORKING_DIRECTORY_PATH);
        if (!workingDirectory.exists() && !workingDirectory.mkdirs()) {
            throw new RuntimeException("Cannot create working directory for plot service");
        }
        plotsDirectory = new File(plotsDirPath.get());
        if (!plotsDirectory.exists() && !plotsDirectory.mkdirs()) {
            throw new RuntimeException("Cannot create plot directory in plot service");
        }

        // create script
        script = new File(scriptPath.get());
        try {
            if (!script.exists() && !script.createNewFile()) {
                throw new RuntimeException("Cannot create plot script");
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot create plot script: " + e.getMessage());
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

    /**
     * Generate plot
     *
     * @param scriptContent content of python file responsible for plot generation (code)
     * @return null if success, output if failure
     * @throws IOException cannot create script file
     */
    public String generatePlot(String scriptContent) throws IOException {
        createPlotScript(scriptContent);

        ProcessBuilder processBuilder = new ProcessBuilder("python3", scriptPath.get());
        Process process = processBuilder.start();

        // read output and return it if failure
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line);
        }

        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) { // fail
                return output.toString();
            }
        } catch (InterruptedException e) {
            return output.toString();
        }
        return null; // success
    }
}
