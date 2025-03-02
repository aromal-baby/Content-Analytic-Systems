package com.contentanalytics.content_analytic_system.controller;

import com.contentanalytics.content_analytic_system.model.entity.Content;
import com.contentanalytics.content_analytic_system.model.enums.Platform;
import com.contentanalytics.content_analytic_system.service.AnalyticsService;
import com.contentanalytics.content_analytic_system.service.ContentAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController // Handling content analytic requests
@RequestMapping("/api/v1/analytics") // Base Url
public class ContentAnalyticsController {

    //inject the analytics service to handle business logic
    private final ContentAnalyticsService contentAnalyticsService;
    private final AnalyticsService analyticsService;

    public ContentAnalyticsController(ContentAnalyticsService contentAnalyticsService,
                                      AnalyticsService analyticsService) {
        this.contentAnalyticsService = contentAnalyticsService;
        this.analyticsService = analyticsService;
    }


    // Controller for getting performance metrics across all platforms
    @GetMapping("/platform-performance")
    public ResponseEntity<Map<String, Object>> getPlatformPerformance() {
        return ResponseEntity.ok(contentAnalyticsService.analyzePlatformPerformance());
    }


    // Viral content for specific platform
    @GetMapping("/viral/{platform}")
    public ResponseEntity<List<Content>> getViralContent(
            @PathVariable Platform platform) {
        return ResponseEntity.ok(contentAnalyticsService.findViralContent(platform));
    }


    // Best post time analyzing controller
    @GetMapping("/best-times/{platform}")
    public ResponseEntity<Map<Integer, Double>> getBestPostingTimes(
            @PathVariable Platform platform) {
        return ResponseEntity.ok(contentAnalyticsService.analyzeBestPostingTimes(platform));
    }


    // Grow analyzing controller
    @GetMapping("/growth")
    public ResponseEntity<Map<String, Long>> getGrowthAnalysis(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(contentAnalyticsService.analyzeGrowth(days));
    }


    // To identify the content need attention
    @GetMapping("/health-check")
    public ResponseEntity<List<Content>> getContentNeedingAttention() {
        return ResponseEntity.ok(contentAnalyticsService.identifyContentNeedingAttention());
    }


    // Content-specific analytics
    @GetMapping("/content/{id}")
    public ResponseEntity<Map<String, Object>> getContentAnalytics(@PathVariable Long id) {
        return ResponseEntity.ok(analyticsService.getContentPerformance(id));
    }


    // Overall analytics
    @GetMapping("/overall")
    public ResponseEntity<Map<String, Object>> getOverallAnalytics() {
        return ResponseEntity.ok(analyticsService.getOverallAnalytics());
    }
}
