package com.contentanalytics.content_analytic_system.service;

import com.contentanalytics.content_analytic_system.model.entity.Content;
import com.contentanalytics.content_analytic_system.model.enums.Platform;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AnalyticsService {
    private final ContentService contentService;
    private final ContentMetricsService metricsService;
    private final PlatformMetricsService platformMetricsService;

    public AnalyticsService(ContentService contentService,
                            ContentMetricsService metricsService,
                            PlatformMetricsService platformMetricsService) {
        this.contentService = contentService;
        this.metricsService = metricsService;
        this.platformMetricsService = platformMetricsService;
    }

    // Getting performance metrics for specific content
    public Map<String, Object> getContentPerformance(Long contentId) {
        try{
            Content content = contentService.getContent(contentId);
            Map<String, Object> performance = new HashMap<>();

            //Basic metrics to the performance map
            performance.put("views", content.getViews());
            performance.put("likes", content.getLikes());
            performance.put("shares", content.getShares());
            performance.put("comments", content.getComments());

            // Engagement rates
            double engagementRate = calculateEngagementRate(content);
            performance.put("engagementRate", engagementRate);

            // Growth metrics
            performance.put("lastUpdated", content.getLastSyncedAt());

            return performance;
        } catch (Exception e) {
            log.error("E rror getting content performance", e);
            throw new RuntimeException("Failed to get performance metrics");
        }
    }

    // Get platform-specific analytics
    public Map<String, Object> getPlatformAnalytics(Platform platform) {
        try {
            List<Content> platformContent = contentService.getContentByPlatform(platform);
            Map<String, Object> analytics = new HashMap<>();

            // Calculating Total metrics
            long totalViews = platformContent.stream().mapToLong(Content::getViews).sum();
            long totalLikes = platformContent.stream().mapToLong(Content::getLikes).sum();

            // Calculating total metrics
            analytics.put("totalContent", platformContent.size());
            analytics.put("totalViews", totalViews);
            analytics.put("totalLikes", totalLikes);

            // Calculating and adding average views to the analytics map
            double avgViews = platformContent.stream().mapToLong(Content::getViews).average().orElse(0);
            analytics.put("avgViews", avgViews);

            // Finding the top performing content to the analytics map
            List<Content> topContent = findTopPerformingContent(platformContent);
            analytics.put("topContent", topContent);

            return analytics;
        } catch (Exception e) {
            log.error("Error getting platform analytics", e);
            throw new RuntimeException("Failed to get platform analytics");
        }
    }

    // To retrieve all overall performance across all platforms
    public Map<String, Object> getOverallAnalytics() {
        Map<String, Object> OverallAnalytics = new HashMap<>();

        // Iterating through all the platforms except the beta
        for (Platform p : Platform.values()) {
            if(!p.toString().endsWith("_BETA")) {
                //adding it to the map
                OverallAnalytics.put(p.name(), getPlatformAnalytics(p));
            }
        }
        return OverallAnalytics;
    }

    // Helper methods: Calculating engagement rate and top performing content based on views

    private double calculateEngagementRate (Content content) {
        // Calculating total engagements
        long totalEngagements = content.getLikes() + content.getShares() + content.getComments();
        // Calculating engagement rate as a percentage of views
        return content.getViews() > 0
                ? (double) totalEngagements / content.getViews() * 100
                : 0;
    }

    private List<Content> findTopPerformingContent(List<Content> contents) {
        return contents.stream()
                .sorted((c1, c2) -> Long.compare(c2.getViews(), c1.getViews()))
                .limit(5)
                .collect(Collectors.toList());
    }
}

