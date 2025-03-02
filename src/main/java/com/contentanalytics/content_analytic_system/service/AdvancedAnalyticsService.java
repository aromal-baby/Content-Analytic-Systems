package com.contentanalytics.content_analytic_system.service;

import com.contentanalytics.content_analytic_system.model.entity.Content;
import com.contentanalytics.content_analytic_system.model.enums.Platform;
import com.contentanalytics.content_analytic_system.model.mongo.ContentMetrics;
import com.contentanalytics.content_analytic_system.repository.mongo.IContentMetricsRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class AdvancedAnalyticsService {

    private final MongoTemplate mongoTemplate;
    private final IContentMetricsRepository metricsRepository;
    private final ContentService contentService;

    public AdvancedAnalyticsService(MongoTemplate mongoTemplate,
                                    IContentMetricsRepository metricsRepository,
                                    ContentService contentService) {
        this.mongoTemplate = mongoTemplate;
        this.metricsRepository = metricsRepository;
        this.contentService = contentService;
    }


    //To get hourly engagement patterns
    public Map<String, Object> getHourlyEngagementPattern(Platform platform) {
        TypedAggregation<ContentMetrics> aggregation = Aggregation.newAggregation(
                ContentMetrics.class,
                Aggregation.match(Criteria.where("platform").is(platform)),
                Aggregation.project()
                    .andExpression("hour(timestamp)").as("hour")
                    .and("metrics.view").as("views"),
                Aggregation.group("hour")
                        .avg("views").as("averageViews"),
                Aggregation.sort(Sort.Direction.ASC, "hour")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(
                aggregation,
                "content_metrics",
                Document.class
        );

        return processAggregationResults(results);
    }


    //Getting content performance prediction
    public Map<String, Object> predictContentPerformance(Long contentId) {
        Content content = contentService.getContent(contentId);
        List<ContentMetrics> historicalMetrics = metricsRepository
                .findByContentIdOrderByTimestampDesc(contentId);


        // Calculate growth rates
        double viewsGrowthRate = calculateGrowthRate(historicalMetrics, "views");
        double likesGrowthRate = calculateGrowthRate(historicalMetrics, "Likes");

        // Predicting next 7 days
        Map<String, Object> predictions = new HashMap<>();
        predictions.put("currentViews", content.getViews());
        predictions.put("predictedViews7Days",
                predictMetrics(content.getViews(), viewsGrowthRate, 7));
        predictions.put("predictedLikes7Days",
                predictMetrics(content.getLikes(), likesGrowthRate, 7));


        return predictions;
    }


    // Calculating audience retention
    public Map<String, Object> calculateAudienceRetention(Platform platform) {
        TypedAggregation<ContentMetrics> aggregation = Aggregation.newAggregation(
                ContentMetrics.class,
                Aggregation.match(Criteria.where("platform").is(platform)),
                Aggregation.group("contentId")
                        .first("metrics.views").as("initialViews")
                        .last("metrics.views").as("currentViews"),
                Aggregation.project()
                        .andExpression("(currentViews / initialViews) * 100")
                        .as("retentionRate")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(
                 aggregation,
                "content_metrics",
                Document.class
        );

        return processRetentionResults(results);
    }

    // HELPER METHODS
    private double calculateGrowthRate(List<ContentMetrics> metrics, String metricName) {
        // Should implement the growth rate
        if (metrics == null || metrics.size() < 2) {
            return 0;
        }
        // implementation of growth rate calc
        return 0.0; // Placeholder
    }

    private long predictMetrics(long currentValue, double growthRate, int days) {
        // Simple linear prediction
        return (long) (currentValue * (1 + (growthRate * days)));
    }

    private Map<String, Object> processAggregationResults(AggregationResults<Document> results) {
        Map<String, Object> processed = new HashMap<>();
        results.forEach(document -> {
            processed.put(
                    document.get("_id").toString(),
                    document.get("averageViews")
            );
        });

        return processed;
    }

    private Map<String, Object> processRetentionResults(AggregationResults<Document> results) {
        Map<String, Object> retention = new HashMap<>();
        double averageRetention = results.getMappedResults().stream()
                .mapToDouble(doc -> doc.getDouble("retentionRate"))
                .average()
                .orElse(0.0);
        retention.put("averageRetention", averageRetention);
        return retention;
    }
}
