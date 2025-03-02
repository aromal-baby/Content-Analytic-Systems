package com.contentanalytics.content_analytic_system.model.dto;

import com.contentanalytics.content_analytic_system.model.enums.Platform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformMetricsDTO {

    private String contentId;
    private Platform platform;
    private Long views = 0L;
    private Long comments = 0L;
    private Long likes = 0L;
    private Long shares = 0L;
    private LocalDateTime fetchedAt;


    // Custom getters and setters beyond lombok is providing - if needed

    @Builder.Default
    private Map<String, Object> platformData = new HashMap<>();

    public Map<String, Object> getAdditionalMetrics() {
        return this.platformData;
    }

    public void setAdditionalMetrics(Map<String, Object> metrics) {
        this.platformData = metrics != null ? metrics : new HashMap<>();
    }
}
