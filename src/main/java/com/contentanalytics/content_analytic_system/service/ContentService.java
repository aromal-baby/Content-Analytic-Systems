package com.contentanalytics.content_analytic_system.service;

import com.contentanalytics.content_analytic_system.exception.ContentNotFoundException;
import com.contentanalytics.content_analytic_system.exception.PlatformOperationException;
import com.contentanalytics.content_analytic_system.model.dto.PlatformMetricsDTO;
import com.contentanalytics.content_analytic_system.model.entity.Content;
import com.contentanalytics.content_analytic_system.model.enums.Platform;
import com.contentanalytics.content_analytic_system.model.mongo.ContentMetrics;
import com.contentanalytics.content_analytic_system.repository.sql.IContentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service    // Marks this as a service class for Spring
@Slf4j      // To add logging capability
@Transactional  // Manages database transactions
public class ContentService {

    private final IContentRepository contentRepository;
    private final ContentMetricsService metricsService;
    private final PlatformMetricsService platformMetricsService;

    // Constructor injection
    public ContentService(IContentRepository contentRepository,
                          ContentMetricsService metricsService,
                          PlatformMetricsService platformMetricsService) {
        this.contentRepository = contentRepository;
        this.metricsService = metricsService;
        this.platformMetricsService = platformMetricsService;
    }

    // CRUD OPERATIONS

    // Adding new contents to the system -- CREATE
    public Content addContent(Content content) {
        // Checking if content is valid
        validateContent(content);
        log.info("Creating new content: {}", content.getTitle());

        try {
            // Saving content to MySQL
            Content savedContent = contentRepository.save(content);

            // Initializing metrics in MongoDB
            platformMetricsService.updatePlatformMetrics(savedContent);

            return savedContent;

        } catch (Exception e) {
            // Log error and throw custom exception
            log.error("Error creating content: ", e);
            throw new RuntimeException("Failed to creat e content: " + e.getMessage());
        }
    }


    // Retrieving content by its ID -- READ
    public Content getContent(Long id) {
        return contentRepository.findById(id)
                .orElseThrow(() -> new ContentNotFoundException("Content not found with ID: " + id));
    }

    // Get all content -- READ
    public List<Content> getAllContents() {
        return contentRepository.findAll();
    }

    // Getting all content for a specific platform -- READ
    public List<Content> getContentByPlatform(Platform platform) {
        return contentRepository.findByPlatform(platform);
    }


    // Update content and its metrics -- UPDATE
    public Content updateContent(Long id, Content content) {
        log.info("Updating content with id: {}", id);
        return contentRepository.findById(id)
                .map(existingContent -> {
                    // Update basic content
                    existingContent.setTitle(content.getTitle());
                    existingContent.setDescription(content.getDescription());
                    existingContent.setContentUrl(content.getContentUrl());
                    existingContent.setStatus(content.getStatus());

                    // Updating metrics
                    existingContent.setViews(content.getViews());
                    existingContent.setLikes(content.getLikes());
                    existingContent.setShares(content.getShares());
                    existingContent.setComments(content.getComments());

                    // Updating MongoDB metrics
                    Map<String, Long> updatedMetrics = new HashMap<>();
                    updatedMetrics.put("views", content.getViews());
                    updatedMetrics.put("likes", content.getLikes());
                    updatedMetrics.put("shares", content.getShares());
                    updatedMetrics.put("comments", content.getComments());

                    metricsService.updateMetrics(id, updatedMetrics);

                    return contentRepository.save(existingContent);
                })
                .orElseThrow(() -> new ContentNotFoundException("Content not found with ID: " + id));
    }


    // Deleting content and its metrics -- DELETE
    public void deleteContent(Long id) {
        log.info("Deleting content with id: {}", id);
        if (!contentRepository.existsById(id)) {
            throw new ContentNotFoundException("Content not found with ID: " + id);
        }
        contentRepository.deleteById(id);
        metricsService.deleteMetrics(id);
    }

    // METRICS OPERATIONS

    // Get content with detailed metrics
    public Map<String, Object> getContentMetrics(Long id) {
        Content content = getContent(id);
        List<ContentMetrics> metrics = metricsService.getMetrics(id);

        Map<String, Object> combined = new HashMap<>();
        combined.put("content", content);
        combined.put("metrics", metrics);

        return combined;
    }


    public Content updateMetrics(Long id) {
        // First find the content
        Content content = getContent(id);
        try {
            // Update metrics based on platform type
            PlatformMetricsDTO updateMetrics = platformMetricsService.updatePlatformMetrics(content);
            content.setViews(updateMetrics.getViews());
            content.setLikes(updateMetrics.getLikes());
            content.setShares(updateMetrics.getShares());
            content.setComments(updateMetrics.getComments());
            // Update last sync time and save
            content.setLastSyncedAt(LocalDateTime.now());
            return contentRepository.save(content);
        } catch (Exception e) {
            log.error("Error updating metrics for content {}: {}", id, e.getMessage());
            throw new PlatformOperationException("Failed to update metrics: " + e.getMessage());
        }
    }


    // HELPER OPERATIONS

    //Validates content before saving
    private void validateContent(Content content) {
        if (content.getPlatform() == null) {
            throw new PlatformOperationException("Platform must be provided");
        }
        if (content.getContentIdentifier() == null || content.getContentIdentifier().trim().isEmpty()) {
            throw new PlatformOperationException("Content identifier must be provided");
        }
    }
}

