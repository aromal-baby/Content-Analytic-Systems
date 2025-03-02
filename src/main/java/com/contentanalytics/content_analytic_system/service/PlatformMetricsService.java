package com.contentanalytics.content_analytic_system.service;

import com.contentanalytics.content_analytic_system.exception.PlatformOperationException;
import com.contentanalytics.content_analytic_system.model.dto.PlatformMetricsDTO;
import com.contentanalytics.content_analytic_system.model.entity.Content;
import com.contentanalytics.content_analytic_system.model.mongo.ContentMetrics;
import com.contentanalytics.content_analytic_system.repository.mongo.IContentMetricsRepository;
import com.contentanalytics.content_analytic_system.repository.sql.IContentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PlatformMetricsService {

    private final YouTubeAnalyticService youTubeAnalyticService;
    private final IContentRepository contentRepository;
    private final IContentMetricsRepository metricsRepository;
    private final ContentMetricsService contentMetricsService;
    private final PlatformIntegrationManager platformIntegrationManager;

    public PlatformMetricsService(YouTubeAnalyticService youTubeAnalyticService,
                                  IContentRepository contentRepository,
                                  IContentMetricsRepository metricsRepository,
                                  ContentMetricsService contentMetricsService,
                                  PlatformIntegrationManager platformIntegrationManager) {

        this.youTubeAnalyticService = youTubeAnalyticService;
        this.contentRepository = contentRepository;
        this.metricsRepository = metricsRepository;
        this.contentMetricsService = contentMetricsService;
        this.platformIntegrationManager = platformIntegrationManager;

    }


    // Public method to update metrics based on platform
    public PlatformMetricsDTO updatePlatformMetrics(Content content) {
        try {

            PlatformMetricsDTO metricsDTO;

            switch (content.getPlatform()) {
                case YOUTUBE:
                    metricsDTO = updateYouTubeMetrics(content);
                    break;
                case MEDIUM:
                    metricsDTO = UpdateMediumMetrics(content);
                    break;
                case CUSTOM_WEBSITE:
                    metricsDTO = UpdateWebsiteMetrics(content);
                    break;
                default:
                    if (content.getPlatform().isBeta()){
                        log.warn("Platform {} is in beta, metrics not available", content.getPlatform());
                        return createEmptyMetricsDTO(content);
                    }
                    throw new PlatformOperationException("Unknown platform: " + content.getPlatform());
            }

            // Updating content in repository
            updateContentMetrics(content, metricsDTO);

            //saving ton MongoDB
            saveToMongoDB(content, metricsDTO);

            return metricsDTO;

        } catch (Exception e) {
            log.error("Error updating metrics for content {}: {}", content.getId(), e.getMessage());
            throw new PlatformOperationException("Failed to update metrics: " + e.getMessage());
        }
    }


    // To handle different platform metrics

    // Youtube
    private PlatformMetricsDTO updateYouTubeMetrics(Content content) {
        try{
            //Getting stats from YouTube
            YouTubeAnalyticService.VideoStats stats =
                    youTubeAnalyticService.getVideoStats(content.getContentIdentifier());

            // Update content metrics
           PlatformMetricsDTO metricsDTO = PlatformMetricsDTO.builder()
                   .contentId(content.getContentIdentifier())
                   .platform(content.getPlatform())
                   .views(stats.getViewCount())
                   .likes(stats.getLikeCount())
                   .comments(stats.getCommentCount())
                   .shares(stats.getShareCount())
                   .fetchedAt(LocalDateTime.now())
                   .build();

            // Additional youtube metrics
            Map<String, Object> additionalMetrics = new HashMap<>();
            additionalMetrics.put("favoriteCount", stats.getFavoriteCount());
            additionalMetrics.put("duration", stats.getDuration());
            metricsDTO.setAdditionalMetrics(additionalMetrics);

            log.info("Updated YouTube metrics for content: {}", content.getId());
            return metricsDTO;

        } catch (Exception e){
            log.error("Error updating {} YouTube metrics: {}", content.getId(), e.getMessage());
            throw new PlatformOperationException("Failed to update YouTube metrics: " + e.getMessage());
        }
    }

    // Medium
    private PlatformMetricsDTO UpdateMediumMetrics(Content content) {
        try{
            PlatformMetricsDTO metricsDTO =platformIntegrationManager.getContentMetrics(
                    content.getContentIdentifier(),
                    content.getPlatform()
            );

            log.info("Updated Medium metrics for content: {}", content.getId());
            return metricsDTO;
        } catch (Exception e){
            log.error("Error updating Medium {} metrics : ", content.getId(), e);
            throw new PlatformOperationException("Failed to update Medium metrics: " + e.getMessage());
        }
    }

    // Custom Website
    private PlatformMetricsDTO UpdateWebsiteMetrics(Content content) {
        try{
            // Metrics; only likes cause websites has nothing else
            PlatformMetricsDTO metricsDTO = PlatformMetricsDTO.builder()
                    .contentId(content.getContentIdentifier())
                    .platform(content.getPlatform())
                    .views(content.getViews())
                    .likes(0L)
                    .fetchedAt(LocalDateTime.now())
                    .build();

            log.info("Updated Website metrics for content: {}", content.getId());
            return metricsDTO;
        } catch (Exception e){
            log.error("Error updating Website metrics for content{} : {} ", content.getId(), e.getMessage());
            throw new PlatformOperationException("Failed to update Website metrics: " + e.getMessage());
        }
    }


    //Saving to mangoDB
    private void saveToMongoDB(Content content, PlatformMetricsDTO metricsDTO) {

        try {
            // Update MySQL content entity
            content.setViews(metricsDTO.getViews());
            content.setLikes(metricsDTO.getLikes());
            content.setComments(metricsDTO.getComments());
            content.setShares(metricsDTO.getShares());
            content.setLastSyncedAt(LocalDateTime.now());
            contentRepository.save(content);

            // Saving to MongoDB using ContentMetricService
            contentMetricsService.saveMetrics(content.getId(), metricsDTO);

           /* // Engagement specific data only if available
            Map<String, Object> engagementMetrics = new HashMap<>();
            if (metricsDTO.getViews() > 0) {
                double engagementRate = calculateEngagementRate(metricsDTO);
                engagementMetrics.put("engagementRate", engagementRate);
            }
            metrics.setEngegementMetrics(engagementMetrics);
            metricsRepository.save(metrics); */


            log.debug("Saved metrics to mongoBD for content: {}", content.getId());
        } catch (Exception e) {
            log.error("Error saving metrics to MongoDB for content {}: {}", content.getId(), e.getMessage());
            throw new PlatformOperationException("Error saving metrics to MongoDB: " + e.getMessage());
        }
    }

    // HELPER & CALCULATION methods

    // For exception platforms
    private PlatformMetricsDTO createEmptyMetricsDTO(Content content) {
        return PlatformMetricsDTO.builder()
                .contentId(content.getContentIdentifier())
                .platform(content.getPlatform())
                .views(0L)
                .likes(0L)
                .comments(0L)
                .shares(0L)
                .fetchedAt(LocalDateTime.now())
                .build();
    }

    private void updateContentMetrics(Content content, PlatformMetricsDTO metricsDTO) {
        content.setViews(metricsDTO.getViews());
        content.setLikes(metricsDTO.getLikes());
        content.setComments(metricsDTO.getComments());
        content.setShares(metricsDTO.getShares());
        content.setLastSyncedAt(LocalDateTime.now());
        contentRepository.save(content);
    }

    /* in the future
    private double calculateEngagementRate(PlatformMetricsDTO metricsDTO) {

        long totalEngagements = metricsDTO.getShares() + metricsDTO.getLikes() + metricsDTO.getComments();
        return metricsDTO.getViews() > 0
                ? (double) totalEngagements / metricsDTO.getViews() * 100
                : 0.0;
    } */

}
