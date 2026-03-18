package com.online.lms.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MultipartServerConfig {

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        long maxFileSize = 500 * 1024 * 1024L;
        long maxRequestSize = 500 * 1024 * 1024L;
        int fileSizeThreshold = 0;
        return new MultipartConfigElement("", maxFileSize, maxRequestSize, fileSizeThreshold);
    }
}
