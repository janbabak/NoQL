package com.janbabak.noqlbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@SuppressWarnings("unused")
public class ResourceConfig implements WebMvcConfigurer {

    public static final String IMAGES_STATIC_FOLDER = "/static/images/";

    @Value("${app.config.workingDirectory}")
    public String workingDirectory;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        final String path = "file:" + workingDirectory + "/plotService/plots/";
        registry.addResourceHandler(IMAGES_STATIC_FOLDER + "**")
                .addResourceLocations(path);
    }
}
