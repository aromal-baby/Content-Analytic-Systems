package com.contentanalytics.content_analytic_system.service;

import com.contentanalytics.content_analytic_system.exception.ContentNotFoundException;
import com.contentanalytics.content_analytic_system.exception.PlatformOperationException;
import com.contentanalytics.content_analytic_system.model.dto.PlatformMetricsDTO;
import com.contentanalytics.content_analytic_system.model.mongo.ContentMetrics;
import com.contentanalytics.content_analytic_system.repository.mongo.IContentMetricsRepository;
import com.contentanalytics.content_analytic_system.repository.sql.IContentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ContentMetricsService {

    private final IContentRepository contentRepository;
    private final IContentMetricsRepository metricsRepository;

    public ContentMetricsService(IContentMetricsRepository metricsRepository,
                                 IContentRepository contentRepository) {

        this.metricsRepository = metricsRepository;
        this.contentRepository = contentRepository;

    }


    //Save new metrics
    public ContentMetrics saveMetrics(Long contentId, PlatformMetricsDTO metricsDTO) {
        log.info("Saving metrics for content {}", contentId);
        try {
            // Creating MongoDB document
            ContentMetrics metrics = new ContentMetrics();
            metrics.setContentId(contentId);
            metrics.setMetrics(createMetricsMap(metricsDTO));
            metrics.setPlatformData(metricsDTO.getAdditionalMetrics());
            metrics.setTimestamp(LocalDateTime.now());

            // Saving to MongoDB
            ContentMetrics savedMetrics = metricsRepository.save(metrics);

            // Update basic MySQL metrics
            updateMySQLMetrics(contentId, metricsDTO);

            return savedMetrics;
        } catch (Exception e) {
            log.error("Error saving metrics for content {}", contentId, e);
            throw new PlatformOperationException("Failed to save metrics");
        }
    }

    // Update metrics
    public ContentMetrics updateMetrics(Long contentId, PlatformMetricsDTO metricsDTO) {

        log.info("Updating metrics for content {}", contentId);
        try{
            // To find existing metrics
            ContentMetrics existingMetrics = metricsRepository
                    .findTopByContentIdOrderByTimestampDesc(contentId)
                    .orElse(new ContentMetrics());

            // Updating..
            existingMetrics.setContentId(contentId);
            existingMetrics.setMetrics(createMetricsMap(metricsDTO));
            existingMetrics.setPlatformData(metricsDTO.getAdditionalMetrics());
            existingMetrics.setTimestamp(LocalDateTime.now());

            //Saving.. MongoDB
            ContentMetrics updatedMetrics = metricsRepository.save(existingMetrics);

            //Updating.. MySQL
            updateMySQLMetrics(contentId, metricsDTO);

            // Returning updated metrics
            return updatedMetrics;

        } catch (Exception e) {
            log.error("Error updating metrics for content {}", contentId, e);
            throw new PlatformOperationException("Failed to update metrics");
        }
    }

    // Adding an overload method for mongoDB
    public ContentMetrics updateMetrics (Long contentId, Map<String, Long> metricsMap) {
        log.info("Updating metrics for content {} using Map", contentId);

        try{
            // finding existing metrics
            ContentMetrics existingMetrics = metricsRepository
                    .findTopByContentIdOrderByTimestampDesc(contentId)
                    .orElse(new ContentMetrics());

            // Updating..
            existingMetrics.setContentId(contentId);
            existingMetrics.setMetrics(metricsMap);
            existingMetrics.setTimestamp(LocalDateTime.now());

            //Saving.. MongoDB
            ContentMetrics updatedMetrics = metricsRepository.save(existingMetrics);

            //updating mysql content entity
            updateContentEntity(contentId, metricsMap);

            return updatedMetrics;

        } catch (Exception e) {
            log.error("Error updating metrics for content {}", contentId, e);
            throw new PlatformOperationException("Failed to update metrics");
        }
    }


    // HELPER methods


    // Update content entity from a map
    private void updateContentEntity (Long contentId, Map<String, Long> metrics) {

        contentRepository.findById(contentId).ifPresent(content -> {
            if(metrics.containsKey("views")) content.setViews(metrics.get("views"));
            if(metrics.containsKey("likes")) content.setLikes(metrics.get("likes"));
            if(metrics.containsKey("comments")) content.setComments(metrics.get("comments"));
            if(metrics.containsKey("shares")) content.setShares(metrics.get("shares"));
            content.setLastSyncedAt(LocalDateTime.now());
            contentRepository.save(content);
        });
    }

    //To create metrics map
    private Map<String, Long> createMetricsMap(PlatformMetricsDTO metricsDTO) {
        Map<String, Long> metrics = new HashMap<>();
        metrics.put("views", metricsDTO.getViews());
        metrics.put("likes", metricsDTO.getLikes());
        metrics.put("comments", metricsDTO.getComments());
        metrics.put("shares", metricsDTO.getShares());
        return metrics;
    }

    // To update MySQL metrics
    private void updateMySQLMetrics(Long contentId, PlatformMetricsDTO metricsDTO) {
        contentRepository.findById(contentId).ifPresent(content -> {
            content.setViews(metricsDTO.getViews());
            content.setLikes(metricsDTO.getLikes());
            content.setComments(metricsDTO.getComments());
            content.setShares(metricsDTO.getShares());
            content.setLastSyncedAt(LocalDateTime.now());
            contentRepository.save(content);
        });
    }

    //to get latest metrics
    public ContentMetrics getLatestMetrics(Long contentId) {
        return metricsRepository
                .findTopByContentIdOrderByTimestampDesc(contentId)
                .orElseThrow(() -> new ContentNotFoundException("No metrics found for content: " + contentId));
    }

    // Delete metrics
    public void deleteMetrics(Long contentId) {
        metricsRepository.deleteByContentId(contentId);
    }

    public List<ContentMetrics> getMetrics(Long contentId) {
        return metricsRepository.findByContentId(contentId);
    }

}
