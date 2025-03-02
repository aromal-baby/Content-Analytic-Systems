package com.contentanalytics.content_analytic_system.service;

import com.contentanalytics.content_analytic_system.model.entity.Content;
import com.contentanalytics.content_analytic_system.model.enums.Platform;
import com.contentanalytics.content_analytic_system.repository.sql.IContentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@Transactional
public class ContentAnalyticsService {

    @Autowired
    private IContentRepository contentRepository;

    @Autowired
    private YouTubeAnalyticService youTubeAnalyticService;

    // Basic CRUD operations
    public Content addContent(Content content) {
        content.setStatus(Content.ContentStatus.ACTIVE);
        return contentRepository.save(content);
    }

    // Platform performance analysis
    public Map<String,Object> analyzePlatformPerformance(){
        Map<String, Object> analysis = new HashMap<>();
        List <Object[]> metrics = contentRepository.getPlatformPerformanceMetrics();

        metrics.forEach(metric -> {
            Platform platform = (Platform) metric[0];
            Long count = (Long) metric[1];
            Double avgViews = (Double) metric[2];
            Double avgLikes = (Double) metric[3];

            Map<String, Object> platformMetrics = new HashMap<>();
            platformMetrics.put("totalContent", count);
            platformMetrics.put("avgViews", avgViews);
            platformMetrics.put("avgLikes", avgLikes);
            platformMetrics.put("engagementRate", avgViews / avgLikes);

            analysis.put(platform.getDisplayName(), platformMetrics);
        });

        return analysis;
    }

    //Content performance analytics
    public List<Content> findViralContent(Platform platform){

        //Defining viral threshold (eg :- top 10% of views)
        Long minViews = calculateViralThreshold(platform);
        return contentRepository.findViralContent(platform, minViews);
    }

    // Best posting time analysis
    public Map<Integer, Double> analyzeBestPostingTimes(Platform platform){
        Map<Integer, Double> hourlyPerformance = new HashMap<>();
        List <Object[]> bestHours = contentRepository.findBestPostingHours(platform);

        bestHours.forEach(hour -> {
            hourlyPerformance.put(
                    (Integer) hour[0],
                    (Double) hour[1]
            );
        });

        return hourlyPerformance;
    }

    // Analysing growth
    public Map<String, Long> analyzeGrowth(int daysBack) {
        LocalDateTime since = LocalDateTime.now().minusDays(daysBack);
        Map<String, Long> growthData = new HashMap<>();

        contentRepository.getContentGrowthOverTime(since)
                .forEach(data -> {
                    String date = data[0].toString();
                    Long count = (Long) data[1];
                    growthData.put(date, count);
                });

        return growthData;

    }

    //Health Check
    public List<Content> identifyContentNeedingAttention() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(7);
        return contentRepository.findContentNeedingAttention(threshold);
    }

    // Helping Methods
    private Long calculateViralThreshold(Platform platform) {
        List<Content> platformContent = contentRepository.findByPlatform(platform);
        if (platformContent.isEmpty()) return 1000l; // default threshold

        // Calculate 90th percentile of the views
        List <Long> viewCounts = platformContent.stream()
                .map(Content::getViews)
                .sorted()
                .toList();

        int index = (int) Math.ceil(0.9 * viewCounts.size()) - 1;
        return viewCounts.get(Math.max(index, 0));
    }

}
