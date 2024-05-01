package com.janbabak.noqlbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ResourceConfig implements WebMvcConfigurer {

    public static final String IMAGES_STATIC_FOLDER = "/static/images/";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String path = "file:plotService/plots/";
        registry.addResourceHandler(IMAGES_STATIC_FOLDER + "**")
                .addResourceLocations(path);
    }
}
