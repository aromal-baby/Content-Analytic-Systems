package com.contentanalytics.content_analytic_system.service;

import com.contentanalytics.content_analytic_system.model.dto.RealTimeAnalyticsMessage;
import com.contentanalytics.content_analytic_system.model.entity.Content;
import com.contentanalytics.content_analytic_system.model.enums.Platform;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@EnableScheduling
public class RealTimeAnalyticsService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ContentService contentService;
    private final AnalyticsService analyticsService;

    //To store last sent metrics to detect change
    private final Map<Long, Map<String, Object>> lastContentMetrics = new HashMap<>();
    private final Map<Platform, Map<String, Object>> lastPlatformMetrics = new HashMap<>();

    public RealTimeAnalyticsService(SimpMessagingTemplate messagingTemplate,
                                    ContentService contentService,
                                    AnalyticsService analyticsService) {
        this.messagingTemplate = messagingTemplate;
        this.contentService = contentService;
        this.analyticsService = analyticsService;
    }


    // Sending real-time platform updates to clients
    @Scheduled(fixedRate = 60000)   // Every minute
    public void sendPlatformAnalyticsUpdates() {
        try {
            //Getting latest analytics
            for (Platform p : Platform.values()) {

                //Skipping beta platforms
                if (p.isBeta()) continue;

                Map<String, Object> analytics = analyticsService.getPlatformAnalytics(p);

                // To check if analytics have changed
                if (metricsChanged(p, analytics)) {
                    // Sending message using the standardized format
                    RealTimeAnalyticsMessage message = RealTimeAnalyticsMessage.platformUpdate(
                            p.name(),
                            analytics
                    );

                    messagingTemplate.convertAndSend(
                            "/topic/analytics/" + p.name().toLowerCase(),
                            analytics
                    );

                    // Updating last metrics
                    lastPlatformMetrics.put(p, new HashMap<>(analytics));

                }
            }
        } catch (Exception e) {
            log.error("Error while sending analytics updates", e);
        }
    }

    // Sending updates for individual content items
    @Scheduled(fixedRate = 120000) //--Every 2 minutes
    public void sendContentAnalyticsUpdates() {
        try {
            List<Content> allContent = contentService.getAllContents();

            for (Content c : allContent) {
                Map<String, Object> metrics = analyticsService.getContentPerformance(c.getId());

                // Checking if metrics have changed
                if(contentMetricsChanged(c.getId(), metrics)) {
                    // Sending message
                    RealTimeAnalyticsMessage message = RealTimeAnalyticsMessage.contentUpdate(
                            c.getPlatform().name(),
                            c.getId(),
                            metrics
                    );

                    // Sending to content-specific topic
                    messagingTemplate.convertAndSend(
                            "/topic/analytics/content/" + c.getId(),
                            message
                    );
                    // Also to platform-wide content update topics
                    messagingTemplate.convertAndSend(
                            "/topic/analytics/platform/" + c.getPlatform().name().toLowerCase() + "/contents",
                            message
                    );

                    // Updating last metrics
                    lastContentMetrics.put(c.getId(), new HashMap<>(metrics));

                }
            }

        } catch(Exception e) {
            log.error("Error while sending content analytics updates", e);
        }
    }

    // Manual trigger methods for immediate updates
    public void triggerContentUpdate(Long contentId) {
        try {
            Content content = contentService.getContent(contentId);
            Map<String, Object> metrics = analyticsService.getContentPerformance(contentId);

            RealTimeAnalyticsMessage message = RealTimeAnalyticsMessage.contentUpdate(
                    content.getPlatform().name(),
                    contentId,
                    metrics
            );

            messagingTemplate.convertAndSend(
                    "/topic/analytics/content/" + contentId,
                    message
            );

            lastContentMetrics.put(contentId, new HashMap<>(metrics));
            log.info("Triggered real-time update for Content ID: {} ", contentId);
        } catch(Exception e) {
            log.error("Error triggering content update for ID: {} ", contentId, e);
        }
    }

    public void triggerPlatformUpdate(Platform platform) {
        try {
            Map<String, Object> analytics = analyticsService.getPlatformAnalytics(platform);

            RealTimeAnalyticsMessage message = RealTimeAnalyticsMessage.platformUpdate(
                    platform.name(),
                    analytics
            );

            messagingTemplate.convertAndSend(
                    "/topic/analytics/platform" + platform.name().toLowerCase(),
                    message
            );

            lastPlatformMetrics.put(platform, new HashMap<>(analytics));
            log.info("Triggered real-time update for Platform: {} ", platform);
        } catch (Exception e) {
            log.error("Error triggering platform update for: {} ", platform, e);
        }
    }


    //HELPER Methods
    private boolean metricsChanged(Platform platform, Map<String, Object> currentMetrics) {
        if (!lastPlatformMetrics.containsKey(platform)) {
            return true;
        }

        Map<String, Object> lastMetrics = lastPlatformMetrics.get(platform);
        return !lastMetrics.equals(currentMetrics);
    }

    private boolean contentMetricsChanged(Long contentId, Map<String, Object> currentMetrics) {
        if (!lastContentMetrics.containsKey(contentId)) {
            return true;
        }

        Map<String, Object> lastMetrics = lastContentMetrics.get(contentId);

        // Compare only key metrics to avoid sending updates for timestamp changes
        return !compareKeyMetrics(lastMetrics, currentMetrics);
    }

    private boolean compareKeyMetrics(Map<String, Object> last, Map<String, Object> current) {
        String[] keyMetrics = {"views", "likes", "comments", "shares", "engagementRate"};

        for (String key : keyMetrics) {
            if (last.containsKey(key) && current.containsKey(key)) {
                if (!last.get(key).equals(current.get(key))) {
                    return false;
                }
            } else if (last.containsKey(key) || current.containsKey(key)) {
                return false;
            }
        }

        return true;
    }

}
