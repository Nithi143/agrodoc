package com.agrodoc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * AgroDoc Web MVC Configuration
 *
 * Configures static resource routing to serve frontend HTML, CSS, and JavaScript
 * assets from the frontend directory through the Spring Boot backend.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path pathRel = Paths.get("..", "frontend");
        if (!Files.exists(pathRel)) {
            pathRel = Paths.get("frontend");
        }
        String frontendUri = pathRel.toAbsolutePath().normalize().toUri().toString();
        if (!frontendUri.endsWith("/")) {
            frontendUri += "/";
        }

        registry.addResourceHandler("/**")
                .addResourceLocations(
                        frontendUri,
                        "file:../frontend/",
                        "file:frontend/",
                        "classpath:/static/",
                        "classpath:/public/"
                );
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/login").setViewName("forward:/login.html");
    }
}
