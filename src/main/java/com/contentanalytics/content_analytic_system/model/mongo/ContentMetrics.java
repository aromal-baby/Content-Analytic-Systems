package com.contentanalytics.content_analytic_system.model.mongo;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.annotation.Id;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@Document(collection = "content_metrics")   // Marking this as MongoDB document
public class ContentMetrics {
    @Id     // Mongo document ID
    private String id;
    private long contentId; // Reference to MySQL content entity
    private LocalDateTime timestamp;    // When the metrics were recorded
    private Map<String, Long> metrics = new HashMap<>();    // Basic analytic metrics like: views, likes, etc..
    private Map<String, Object> platformData = new HashMap<>();    // For additional metrics; platform-specific
    @Field("Engagement_metrics")    // Engagement metrics (Ratios & Percentage)
    private Map<String, Double> engagementMetrics = new HashMap<>();
    private Map<String, Integer> demographicData = new HashMap<>(); // Audience demographics (age groups, gender etc.)
    private Map<String, Integer> geographicData = new HashMap<>();  // Geographic distribution of audience


    // Initializing maps in constructor
    public ContentMetrics() {
        this.metrics = new HashMap<>();
        this.platformData = new HashMap<>();
        this.engagementMetrics = new HashMap<>();
        this.demographicData = new HashMap<>();
        this.geographicData = new HashMap<>();
    }

    // HELPER method to get platform data (backward compatibility)
    public Map<String, Object> getAdditionalMetrics() {
        return this.platformData;
    }

    // To set
    public void setPlatformData(Map<String, Object> platformData) {
        this.platformData = platformData != null? platformData : new HashMap<>();
    }

}
