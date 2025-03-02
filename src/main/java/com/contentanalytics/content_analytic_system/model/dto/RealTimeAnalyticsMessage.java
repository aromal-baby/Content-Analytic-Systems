package com.contentanalytics.content_analytic_system.model.dto;

import com.contentanalytics.content_analytic_system.service.RealTimeAnalyticsService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RealTimeAnalyticsMessage {

    private String platform;
    private String messageType;
    private LocalDateTime timestamp;
    private Map<String, Object> data;

    // Factory methods for common message types
    public static RealTimeAnalyticsMessage platformUpdate(String platform, Map<String, Object> data) {
        return new RealTimeAnalyticsMessage(
                platform,
                "PLATFORM_UPDATE",
                LocalDateTime.now(),
                data
        );
    }

    public static RealTimeAnalyticsMessage contentUpdate(String platform, Long contentId, Map<String, Object> data) {
        data.put("contentId", contentId);
        return new RealTimeAnalyticsMessage(
                platform,
                "CONTENT_UPDATE",
                LocalDateTime.now(),
                data
        );
    }
}
