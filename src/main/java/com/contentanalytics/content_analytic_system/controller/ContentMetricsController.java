package com.contentanalytics.content_analytic_system.controller;

import com.contentanalytics.content_analytic_system.model.mongo.ContentMetrics;
import com.contentanalytics.content_analytic_system.service.ContentAnalyticsService;
import com.contentanalytics.content_analytic_system.service.ContentMetricsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/metrics")
public class ContentMetricsController {

    // Instead of using autowired method
    private final ContentMetricsService metricsService;

    public ContentMetricsController(ContentMetricsService metricsService) {

        this.metricsService = metricsService;

    }

    // To get new content for a specific content
    @GetMapping("/{contentId}")
    public ResponseEntity<List<ContentMetrics>> getMetrics(@PathVariable Long contentId) {
        List<ContentMetrics> metrics = metricsService.getMetrics(contentId);
        return ResponseEntity.ok(metrics);
    }

    // For getting the latest
    @GetMapping("/{contentId}/latest")
    public ResponseEntity<ContentMetrics> getLatestMetrics(@PathVariable Long contentId) {
        ContentMetrics metrics = metricsService.getLatestMetrics(contentId);
        return ResponseEntity.ok(metrics);
    }

    // For deleting
    @DeleteMapping("/{contentId}")
    public ResponseEntity<Void> deleteMetrics(@PathVariable Long contentId) {
        metricsService.deleteMetrics(contentId);
        return ResponseEntity.ok().build();
    }
}
