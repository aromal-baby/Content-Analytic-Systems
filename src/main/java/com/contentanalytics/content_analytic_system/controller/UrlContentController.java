package com.contentanalytics.content_analytic_system.controller;

import com.contentanalytics.content_analytic_system.model.dto.ContentIdentifierDTO;
import com.contentanalytics.content_analytic_system.model.dto.PlatformMetricsDTO;
import com.contentanalytics.content_analytic_system.model.entity.Content;
import com.contentanalytics.content_analytic_system.service.ContentMetricsService;
import com.contentanalytics.content_analytic_system.service.ContentService;
import com.contentanalytics.content_analytic_system.service.PlatformIntegrationManager;
import com.contentanalytics.content_analytic_system.service.UrlParserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/v1/url-content")
public class UrlContentController {

    private final UrlParserService urlParseService;
    private final ContentService contentService;
    private final PlatformIntegrationManager platformIntegrationManager;

    public UrlContentController (UrlParserService urlParseService,
                                 PlatformIntegrationManager platformIntegrationManager,
                                 ContentService contentService) {

        this.urlParseService = urlParseService;
        this.platformIntegrationManager = platformIntegrationManager;
        this.contentService = contentService;
    }

    //Creating content and fetching analytics from a URL
    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeContentUrl(@RequestBody Map<String, String> request) {

        String url = request.get("url");
        String title = request.get("title");

        log.info("Analyzing content URL: {}", url);

        //Parsing the URL to determine platform and content identifier
        ContentIdentifierDTO identifier = urlParseService.parseUrl(url);

        // Creating new content entity
        Content content = new Content();
        content.setTitle(title != null ? title : "Content from" + identifier.getPlatform());
        content.setPlatform(identifier.getPlatform());
        content.setContentIdentifier(identifier.getContentIdentifier());
        content.setContentUrl(identifier.getContentUrl());

        // Saving the newly added content
        Content savedContent = contentService.addContent(content);

        // Fetching the metrics from it
        PlatformMetricsDTO metrics = platformIntegrationManager.getContentMetrics(
                identifier.getContentIdentifier(),
                identifier.getPlatform()
        );

        //Build and return response
        Map<String, Object> response = new HashMap<>();
        response.put("content", savedContent);
        response.put("metrics", metrics);

        return ResponseEntity.ok(response);

    }


    // Extracting info from a URL without saving
    @GetMapping("/parse")
    public ResponseEntity<ContentIdentifierDTO> parseUrl(@RequestParam String url) {
        log.info("Parsing URL: {}", url);
        ContentIdentifierDTO identifier = urlParseService.parseUrl(url);
        return ResponseEntity.ok(identifier);
    }


    // Analyzing a URL and returning metrics without saving to the database
    @GetMapping("/preview")
    public ResponseEntity<PlatformMetricsDTO> previewUrl(@RequestParam String url) {
        log.info("Previewing URL: {}", url);

        ContentIdentifierDTO identifier = urlParseService.parseUrl(url);
        PlatformMetricsDTO metrics = platformIntegrationManager.getContentMetrics(
                identifier.getContentIdentifier(),
                identifier.getPlatform()
        );

        return ResponseEntity.ok(metrics);
    }
}
