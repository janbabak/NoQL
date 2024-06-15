package com.janbabak.noqlbackend.service.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtils {

    /**
     * Reads content of a file at once, not buffering.
     * @param path path to the file
     * @return file content
     */
    public static String getFileContent(String path) {
        try {
            return Files.readString(Paths.get(path));
        } catch (IOException e) {
            throw new RuntimeException("Cannot read file: " + path);
        }
    }
}
