package com.janbabak.noqlbackend.error.exception;

public class PlotScriptExecutionException extends Exception {

    public PlotScriptExecutionException(String output) {
        super("Python script execution error: " + output);
    }
}
