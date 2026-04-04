package com.pm.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        
        // Allow your React app's URL
        corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        
        // Allow all standard methods
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Allow all headers (important for JWT/Authorization)
        corsConfig.setAllowedHeaders(Arrays.asList("*"));
        
        // Allow cookies/credentials if you ever need them
        corsConfig.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply this to all routes (/**)
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}