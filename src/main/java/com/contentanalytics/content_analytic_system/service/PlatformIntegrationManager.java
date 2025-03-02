package com.contentanalytics.content_analytic_system.service;

import com.contentanalytics.content_analytic_system.exception.PlatformOperationException;
import com.contentanalytics.content_analytic_system.model.dto.MediumStatisticsDTO;
import com.contentanalytics.content_analytic_system.model.dto.PlatformMetricsDTO;
import com.contentanalytics.content_analytic_system.model.dto.WordPressMetricsDTO;
import com.contentanalytics.content_analytic_system.model.dto.YouTubeStatisticsDTO;
import com.contentanalytics.content_analytic_system.model.enums.Platform;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Service
//
public class PlatformIntegrationManager {

    private final YouTubeIntegrationService youTubeService;
    private final MediumIntegrationService mediumService;
    private final WordPressIntegrationService wordPressService;
    private final ContentMetricsService metricsService;

    public PlatformIntegrationManager(YouTubeIntegrationService youTubeService,
                                      MediumIntegrationService mediumService,
                                      WordPressIntegrationService wordPressService,
                                      ContentMetricsService metricsService) {

        this.youTubeService = youTubeService;
        this.mediumService = mediumService;
        this.wordPressService = wordPressService;
        this.metricsService = metricsService;

    }

    // Method to get content metrics based on platforms
    public PlatformMetricsDTO getContentMetrics(String contentId, Platform platform) {
        try {
            PlatformMetricsDTO metrics = new PlatformMetricsDTO();
                    metrics.setContentId(contentId);
                    metrics.setPlatform(platform);
                    metrics.setFetchedAt(LocalDateTime.now());


            switch (platform) {
                case YOUTUBE:
                    YouTubeAnalyticService.VideoStats ytStats = youTubeService.getVideoStats(contentId);
                    updateMetricsFromYouTube(metrics, ytStats);
                    break;
                case MEDIUM:
                    MediumStatisticsDTO mediumStats = mediumService.getPostStats(contentId);
                    updateMetricsFromMedium(metrics, mediumStats);
                    break;
                case WORDPRESS:
                    WordPressMetricsDTO wpStats = wordPressService.getPostMetrics(contentId);
                    updateMetricsFromWordPress(metrics, wpStats);
                    break;
                case CUSTOM_WEBSITE:
                    // -----
                    break;
                default:
                    if (platform.isBeta()) {
                        log.warn("Platform{} is in beta, metrics not available (coming soon..)", platform);
                        return metrics;
                    }
                    throw new PlatformOperationException("Unsupported platform: " + platform);
            }

            // Storing metrics in MongoDB
            metricsService.saveMetrics(Long.parseLong(contentId), metrics);
            return metrics;
        } catch (Exception e) {
            log.error("Error fetching metrics for {} from {}: {}", contentId, platform, e.getMessage());
            throw new RuntimeException("Failed to fetch metrics: " + e.getMessage());
        }
    }

    // HELPER METHODS
    // YouTube
    private void updateMetricsFromYouTube(PlatformMetricsDTO metrics, YouTubeAnalyticService.VideoStats ytStats) {
        if(ytStats != null) {
            metrics.setViews(ytStats.getViewCount());
            metrics.setLikes(ytStats.getLikeCount());
            metrics.setComments(ytStats.getCommentCount());
            metrics.setShares(ytStats.getShareCount());

            // Additional YouTube metrics
            Map<String, Object> additionalMetrics = new HashMap<>();
            additionalMetrics.put("favoriteCount", ytStats.getFavoriteCount());
            additionalMetrics.put("duration", ytStats.getDuration());
            metrics.setAdditionalMetrics(additionalMetrics);

        }
    }

    // Medium
    private void updateMetricsFromMedium(PlatformMetricsDTO metrics, MediumStatisticsDTO mediumStats) {
        if (mediumStats != null) {
            metrics.setViews(mediumStats.getViews());
            metrics.setLikes(mediumStats.getClaps());
            metrics.setComments(mediumStats.getResponses());

            //Additional metrics
            Map<String, Object> additionalMetrics = new HashMap<>();
            additionalMetrics.put("reads", mediumStats.getReads());
            additionalMetrics.put("readRatio",
                    mediumStats.getViews() > 0 ?
                    (double) mediumStats.getReads() / mediumStats.getViews() : 0);
            metrics.setAdditionalMetrics(additionalMetrics);

        }
    }

    // WordPress
    private void updateMetricsFromWordPress(PlatformMetricsDTO metrics, WordPressMetricsDTO wpStats) {

        if(wpStats != null) {
            metrics.setViews(wpStats.getViews());
            metrics.setLikes(wpStats.getLikes());
            metrics.setComments(wpStats.getComments());
            metrics.setShares(wpStats.getShares());

            // Additional
            Map<String, Object> additionalMetrics = new HashMap<>();
            if (wpStats.getAdditionalMetrics() != null) {
                additionalMetrics.putAll(wpStats.getAdditionalMetrics());
            }

            //Adding wordPress post-specified metadata
            additionalMetrics.put("postId", wpStats.getPostId());
            additionalMetrics.put("title", wpStats.getTitle());
            additionalMetrics.put("fetchedAt", wpStats.getFetchedAt());

            metrics.setAdditionalMetrics(additionalMetrics);
        }
    }
}
