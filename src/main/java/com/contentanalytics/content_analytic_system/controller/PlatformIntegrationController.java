package com.contentanalytics.content_analytic_system.controller;


import com.contentanalytics.content_analytic_system.model.dto.MediumStatisticsDTO;
import com.contentanalytics.content_analytic_system.model.dto.PlatformMetricsDTO;
import com.contentanalytics.content_analytic_system.model.enums.Platform;
import com.contentanalytics.content_analytic_system.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Cacheable(value = "platformStatus", key = "#platform")
@RestController
@Slf4j
@RequestMapping("/api/v1/platforms")
public class PlatformIntegrationController {

    private final PlatformIntegrationManager platformIntegrationManager;
    private final MediumIntegrationService mediumIntegrationService;
    private final WordPressIntegrationService wordPressIntegrationService;
    private final YouTubeIntegrationService youTubeIntegrationService;

    public PlatformIntegrationController(
            PlatformIntegrationManager platformIntegrationManager,
            YouTubeIntegrationService youTubeService,
            MediumIntegrationService mediumIntegrationService,
            WordPressIntegrationService wordPressIntegrationService, YouTubeIntegrationService youTubeIntegrationService) {

        this.platformIntegrationManager = platformIntegrationManager;
        this.mediumIntegrationService = mediumIntegrationService;
        this.wordPressIntegrationService = wordPressIntegrationService;
        this.youTubeIntegrationService = youTubeIntegrationService;
    }


    // Unified endpoint for getting metrics from any platform
    @GetMapping("/{platform}/{contentId}/metrics")
    public ResponseEntity<PlatformMetricsDTO> getContentMetrics(
            @PathVariable Platform platform,
            @PathVariable String contentId) {

        log.info("Fetching metrics for content {} from {}", contentId, platform);
        PlatformMetricsDTO metrics = platformIntegrationManager.getContentMetrics(contentId, platform);
        return ResponseEntity.ok(metrics);
    }


    // YouTube specific endpoint
    @GetMapping("/youtube/{videoId}/stats")
    public ResponseEntity<YouTubeAnalyticService.VideoStats> getYouTubeVideoStats(@PathVariable String videoId) {

        log.info("Fetching YouTube stats for video {}", videoId);
        YouTubeAnalyticService.VideoStats stats = youTubeIntegrationService.getVideoStats(videoId);
        return ResponseEntity.ok(stats);
    }


    // Medium specific endpoint
    @GetMapping("/medium/{postId}/stats")
    public ResponseEntity<MediumStatisticsDTO> getPostStatistics(@PathVariable String postId) {

        log.info("Fetching Medium stats for post {}", postId);
        MediumStatisticsDTO stats = mediumIntegrationService.getPostStats(postId);
        return ResponseEntity.ok(stats);
    }
    // Blog
    @GetMapping("/wordpress/blog/{blogUrl}/stats")
    public ResponseEntity<Map<String, Object>> getWordPressBlogStats(@PathVariable String blogUrl) {
        log.info("Fetching WordPress stats for blog {}", blogUrl);
        Map<String, Object> stats = wordPressIntegrationService.getBlogStatistics(blogUrl);
        return ResponseEntity.ok(stats);
    }


    // Endpoint for checking platform availability and status
    @GetMapping("/{platform}/status")
    public ResponseEntity<Map<String, Object>> getPlatformStatus(@PathVariable Platform platform) {
        log.info("Fetching Platform status for platform {}", platform);

        Map<String, Object> status = new HashMap<>();
        status.put("platform", platform);
        status.put("available", !platform.isBeta());
        status.put("status", platform.isBeta() ? "BETA" : "ACTIVE");
        status.put("displayName", platform.getDisplayName());

        return ResponseEntity.ok(status);
    }

    // Endpoint to list all available platforms
    @GetMapping("/list")
    public ResponseEntity<List<Map<String, Object>>> listAllPlatforms(
            @RequestParam(required = false, defaultValue = "false") boolean includeBeta) {
        log.info("Listing all platforms, includeBeta = {}", includeBeta);

        List<Map<String, Object>> platforms = Arrays.stream(Platform.values())
                .filter(p -> includeBeta || !p.isBeta())
                .map(p -> {
                    Map<String, Object> platformInfo = new HashMap<>();
                    platformInfo.put("name", p.name());
                    platformInfo.put("displayName", p.getDisplayName());
                    platformInfo.put("isBeta", p.isBeta());
                    return platformInfo;
                })
                .collect(Collectors.toList());

        return  ResponseEntity.ok(platforms);

    }
}
