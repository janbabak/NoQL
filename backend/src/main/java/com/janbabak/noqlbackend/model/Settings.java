package com.janbabak.noqlbackend.model;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Application settings that are loaded when the application starts and cannot be changed afterward.
 */
@Component
@Getter
public class Settings {

    @Value("${app.settings.pagination.maxPageSize}")
    public Integer maxPageSize;

    @Value("${app.settings.pagination.defaultPageSize}")
    public Integer defaultPageSize;

    @Value("${app.settings.plotServiceContainerName}")
    public String plotServiceContainerName;

    @Value("${app.settings.defaultUserQueryLimit}")
    public Integer defaultUserQueryLimit;

    private static Settings instance;

    @PostConstruct
    public void init() {
        instance = this;
    }

    public static Integer getMaxPageSizeStatic() {
        return instance.maxPageSize;
    }

    public static Integer getDefaultPageSizeStatic() {
        return instance.defaultPageSize;
    }
}
