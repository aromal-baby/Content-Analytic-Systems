package com.contentanalytics.content_analytic_system.service;

import com.contentanalytics.content_analytic_system.exception.PlatformOperationException;
import com.contentanalytics.content_analytic_system.model.dto.WordPressMetricsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
// Integrating WordPres site to get the analytics
public class WordPressIntegrationService {
    private final RestTemplate restTemplate;

    public WordPressIntegrationService() {
        this.restTemplate = new RestTemplate();
    }

    // Method to fetch POST metrics from WordPress
    public WordPressMetricsDTO getPostMetrics(String postId) {
        try {
            // For now, return dummy data
            WordPressMetricsDTO metrics = new WordPressMetricsDTO();
            metrics.setViews(300L);
            metrics.setLikes(45L);
            metrics.setComments(20L);
            metrics.setShares(15L);
            metrics.setPostId(postId);
            metrics.setTitle("Sample WordPress Post #" + postId);
            metrics.setFetchedAt(LocalDateTime.now());

            // Add WordPress-specific metrics to the additional metrics map
            Map<String, Object> additionalMetrics = new HashMap<>();
            additionalMetrics.put("wordCount", 850);
            additionalMetrics.put("readingTime", "4 min");
            additionalMetrics.put("categories", new String[]{"Technology", "Analytics"});
            additionalMetrics.put("author", "John Doe");
            additionalMetrics.put("publishDate", LocalDateTime.now().minusDays(7));
            metrics.setAdditionalMetrics(additionalMetrics);

            return metrics;

        } catch (Exception e) {
            log.error("Error fetching WordPress metrics: ", e);
            throw new PlatformOperationException("Failed to fetch WordPress metrics");
        }
    }

    // Function to get overall blog statistics
    public Map<String, Object> getBlogStatistics(String blogUrl) {

        try{

            // For API implementation

            //Dummy
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalPosts", 35);
            statistics.put("totalComments", 45);
            statistics.put("totalViews", 2000);
            statistics.put("popularCategories", new String[]{"Data", "Technology", "Analytics"});
            statistics.put("averageEngagement", 4.2);
            statistics.put("lastUpdated", LocalDateTime.now());

            return statistics;

        } catch (Exception e){
            log.error("Error fetching WordPress blog statistics: ", e);
            throw new PlatformOperationException("Failed to fetch WordPress blog statistics");
        }
    }
}
