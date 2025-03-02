package com.contentanalytics.content_analytic_system.service;

import com.contentanalytics.content_analytic_system.exception.PlatformOperationException;
import com.contentanalytics.content_analytic_system.model.dto.MediumStatisticsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpHeaders;

@Service
@Slf4j
// Integrating Medium(for blogs, etc..)
public class MediumIntegrationService {

    private final RestTemplate restTemplate;

    public MediumIntegrationService() {
        this.restTemplate = new RestTemplate();
    }


    // Fetching metrics
    public MediumStatisticsDTO getPostStats(String postId) {
        try {
            // For now, return dummy data
            MediumStatisticsDTO stats = new MediumStatisticsDTO();
            stats.setViews(500L);
            stats.setReads(200L);
            stats.setClaps(75L);
            return stats;

        } catch (Exception e) {
            log.error("Error fetching Medium stats: ", e);
            throw new PlatformOperationException("Failed to fetch Medium stats");
        }
    }
}
