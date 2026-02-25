package com.buildings.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration to serve uploaded files as static resources
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.file.base-url:/uploads}")
    private String baseUrl;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map URL /uploads/** to file system uploads/ directory
        String resourcePattern = baseUrl + "/**";
        String resourceLocation = "file:" + uploadDir + "/";

        registry.addResourceHandler(resourcePattern)
                .addResourceLocations(resourceLocation);
    }
}
