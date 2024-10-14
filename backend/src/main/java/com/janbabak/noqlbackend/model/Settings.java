package com.janbabak.noqlbackend.model;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Application settings that are loaded when the application starts and cannot be changed afterward.
 */
@Component
@Getter
public class Settings {

    /** Maximum page size of automatically paginated query result. */
    @Value("${app.settings.pagination.maxPageSize}")
    public Integer maxPageSize;

    /** Default page size of automatically paginated query result. */
    @Value("${app.settings.pagination.defaultPageSize}")
    public Integer defaultPageSize;

    /** Number of retries when translated query fails due to a syntax error */
    @Value("${app.settings.translationRetries}")
    public Integer translationRetries;

    /** Name of the container where the plot service is running (e.g. when multiple instances are running */
    @Value("${app.settings.plotServiceContainerName}")
    public String plotServiceContainerName;

    /**
     * Default limit for number of queries for a newly registered user
     */
    @Value("${app.settings.defaultUserQueryLimit}")
    public Integer defaultUserQueryLimit;
}
