package com.contentanalytics.content_analytic_system.service;


import com.contentanalytics.content_analytic_system.model.dto.PlatformMetricsDTO;
import com.contentanalytics.content_analytic_system.model.entity.Content;
import com.contentanalytics.content_analytic_system.model.enums.Platform;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@EnableScheduling
public class MetricsSynchronizationService {

    private final PlatformIntegrationManager platformManager;
    private final ContentService contentService;
    private final ContentMetricsService metricsService;

    public MetricsSynchronizationService(PlatformIntegrationManager platformManager,
                                         ContentService contentService,
                                         ContentMetricsService metricsService) {

        this.platformManager = platformManager;
        this.contentService = contentService;
        this.metricsService = metricsService;

    }

    // Scheduled syncing for all content
    @Scheduled(fixedRate = 300000)  // Every 5 minutes
    public void syncAllContentMetrics() {
         log.info("Starting scheduled metrics sync");

         try {
             List<Content> activeContent = contentService.getAllContents();

             for (Content content : activeContent) {
                 syncContentMetrics(content.getId(), content.getPlatform());
             }

             log.info("Completed metrics synchronization for {} items", activeContent.size());
         } catch (Exception e) {
             log.error("Error during metrics metrics synchronization: {}", e.getMessage());
         }
    }


    //Sync specific content
    public void syncContentMetrics(Long contentId, Platform platform) {

        try {
            PlatformMetricsDTO metrics = platformManager.getContentMetrics(
                    contentId.toString(),
                    platform
            );

            // Updating mongoDB metrics
            metricsService.updateMetrics(contentId, metrics);
            log.debug("Synced metrics for content {}", contentId);
        } catch (Exception e) {
            log.error("Failed to sync metrics for content {}: {}", contentId, e.getMessage());
        }
    }

}
