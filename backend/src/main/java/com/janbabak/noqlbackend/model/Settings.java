package com.janbabak.noqlbackend.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Application settings that are loaded when the application starts and cannot be changed afterward.
 */
@Component
public class Settings {

    /** Maximum page size of automatically paginated query result. */
    @Value("${app.settings.pagination.maxPageSize}")
    public Integer maxPageSize;

    /** Default page size of automatically paginated query result. */
    @Value("${app.settings.pagination.defaultPageSize}")
    public Integer defaultPageSize;
}
