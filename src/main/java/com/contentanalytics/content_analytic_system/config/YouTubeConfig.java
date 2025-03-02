package com.contentanalytics.content_analytic_system.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class YouTubeConfig {
    @Value("${youtube.api.key}")
    private String apiKey;

    @Value("${youtube.api.application.name}")
    private String applicationName;

    public String getApiKey() {
        return apiKey;
    }

    public String getApplicationName() {
        return applicationName;
    }
}

