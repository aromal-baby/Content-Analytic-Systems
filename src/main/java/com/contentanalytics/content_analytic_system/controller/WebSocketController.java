package com.contentanalytics.content_analytic_system.controller;

import com.contentanalytics.content_analytic_system.model.dto.RealTimeAnalyticsMessage;
import com.contentanalytics.content_analytic_system.model.enums.Platform;
import com.contentanalytics.content_analytic_system.service.RealTimeAnalyticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
@Slf4j
public class WebSocketController {

    private final RealTimeAnalyticsService realTimeAnalyticsService;

    public WebSocketController(RealTimeAnalyticsService realTimeAnalyticsService) {
        this.realTimeAnalyticsService = realTimeAnalyticsService;
    }

    @MessageMapping("/analytics/subscribe/content")
    public void subscribeToContentUpdate(Map<String, Long> message) {

        Long contentId = message.get("contentId");
        log.info("Client subscribed to content updates for ID: {} ", contentId);

        // Immediate trigger
        realTimeAnalyticsService.triggerContentUpdate(contentId);
    }

    @MessageMapping("/analytics/subscribe/platform")
    public void subscribeToPlatformUpdate(Map<String, String> message) {

        String platformName = message.get("platform");
        log.info(" Client subscribed to platform updates for: {}", platformName);

        try {

            Platform platform = Platform.valueOf(platformName.toUpperCase());
            // Triggering an update for this platform immediately
            realTimeAnalyticsService.triggerPlatformUpdate(platform);

        } catch  (IllegalArgumentException e) {
            log.error("Invalid platform name received: {}", platformName);
        }
    }

    @MessageMapping("/analytics/request/update")
    @SendTo("/topic/analytics/admin")
    public RealTimeAnalyticsMessage requestManualUpdate(Map<String, Object> message) {
        log.info("Manual update request by client");

        // Creating response message
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("message", "Manual update triggered");
        responseData.put("status", "success");

        return new RealTimeAnalyticsMessage(
                "SYSTEM",
                "ADMIN_NOTIFICATION",
                java.time.LocalDateTime.now(),
                responseData
        );
    }
}
