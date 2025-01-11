package com.janbabak.noqlbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ResourceConfig implements WebMvcConfigurer {

    public static final String IMAGES_STATIC_FOLDER = "/static/images/";

    private static String WORKING_DIRECTORY;

    ResourceConfig(@Value("${app.config.workingDirectory}") String appWorkingDirectory) {
        WORKING_DIRECTORY = appWorkingDirectory;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String path = "file:" + WORKING_DIRECTORY + "/plotService/plots/";
        registry.addResourceHandler(IMAGES_STATIC_FOLDER + "**")
                .addResourceLocations(path);
    }
}
