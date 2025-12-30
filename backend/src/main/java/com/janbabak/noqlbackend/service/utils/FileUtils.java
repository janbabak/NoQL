package com.janbabak.noqlbackend.service.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
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
            log.error("Cannot read file at path {}: {}", path, e.getMessage());
            throw new RuntimeException("Cannot read file: " + path, e);
        }
    }
}
