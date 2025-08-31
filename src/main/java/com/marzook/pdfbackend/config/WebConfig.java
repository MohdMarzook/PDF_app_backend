package com.marzook.pdfbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${FRONTEND_URL}")
    private String frontEndUrl;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {

            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Apply to all routes under /api
                        .allowedOrigins(frontEndUrl,"http://localhost:3000", "http://192.168.1.5:3000") // Your Next.js app's origin
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allowed request methods
                        .allowedHeaders("*") // Allowed headers
                        .allowCredentials(true); // Allow cookies and credentials
            }
        };
    }
}
