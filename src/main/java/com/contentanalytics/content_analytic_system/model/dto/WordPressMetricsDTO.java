package com.contentanalytics.content_analytic_system.model.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
public class WordPressMetricsDTO {

    private Long views;
    private Long comments;
    private Long likes;
    private Long shares;
    private String postId;
    private String title;
    private LocalDateTime fetchedAt;
    private Map<String, Object> additionalMetrics = new HashMap<>();

    // Heper methods to get additional metrics
    public Map<String, Object> getAdditionalMetrics() {
        return this.additionalMetrics;
    }

    //Helper method to set additional metrics
    public void setAdditionalMetrics(Map<String, Object> additionalMetrics) {
        this.additionalMetrics = additionalMetrics != null ? additionalMetrics : new HashMap<>();
    }

}
