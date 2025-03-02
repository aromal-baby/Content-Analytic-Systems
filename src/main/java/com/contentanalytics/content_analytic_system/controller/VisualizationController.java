package com.contentanalytics.content_analytic_system.controller;

import com.contentanalytics.content_analytic_system.model.dto.VisualizationDTO;
import com.contentanalytics.content_analytic_system.service.VisualizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/v1/visualizations")
public class VisualizationController {

    private final VisualizationService visualizationService;

    public VisualizationController(VisualizationService visualizationService) {
        this.visualizationService = visualizationService;
    }

    // Platform comparison chart data
    @GetMapping("/platform-comparison")
    public ResponseEntity<VisualizationDTO.ChartDataDTO> getPlatformComparisonChart() {
        log.info("Requesting platform comparison visualization");
        return ResponseEntity.ok(visualizationService.getPlatformComparisonChart());
    }

    // To get content timeline chart data
    @GetMapping("/content-timeline/{contentId}")
    public ResponseEntity<VisualizationDTO.ChartDataDTO> getContentTimelineChart(@PathVariable Long contentId) {
        log.info("Requesting content timeline visualization for contentId: {}", contentId);
        return ResponseEntity.ok(visualizationService.getContentTimelineChart(contentId));
    }

    // Platform distribution chart data
    @GetMapping("/platform-distribution")
    public ResponseEntity<VisualizationDTO.ChartDataDTO> getPlatformDistributionChart() {
        log.info("Requesting platform distribution visualization");
        return ResponseEntity.ok(visualizationService.getPlatformDistributionChart());
    }
}
