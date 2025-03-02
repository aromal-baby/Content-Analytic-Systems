package com.contentanalytics.content_analytic_system.service;

import com.contentanalytics.content_analytic_system.config.YouTubeConfig;
import com.contentanalytics.content_analytic_system.exception.PlatformOperationException;
import com.contentanalytics.content_analytic_system.model.dto.YouTubeStatisticsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service

// YouTube integration
public class YouTubeIntegrationService {
    private final String apiKey;
    private final RestTemplate restTemplate;
    private final YouTubeConfig youTubeConfig;
    private final YouTubeAnalyticService youTubeAnalyticService;


    public YouTubeIntegrationService(@Value("${youtube.api.key}") String apiKey,
                                     RestTemplate restTemplate,
                                     YouTubeConfig youTubeConfig,
                                     YouTubeAnalyticService youTubeAnalyticService) {
        this.apiKey = apiKey;
        this.restTemplate = new RestTemplate();
        this.youTubeConfig = new YouTubeConfig();
        this.youTubeAnalyticService = youTubeAnalyticService;

    }

    //To fetch video stats
    public YouTubeAnalyticService.VideoStats getVideoStats(String videoId) {
        try {
            String url = String.format(
                    "https://www.googleapis.com/youtube/v3/videos?id=%s&key=%s&part=statistics",
                    videoId, apiKey
            );

            // For now, return dummy data
            YouTubeAnalyticService.VideoStats stats = new YouTubeAnalyticService.VideoStats();
            stats.setViewCount(1000L);
            stats.setLikeCount(100L);
            stats.setCommentCount(50L);
            stats.setShareCount(25L);
            stats.setFavoriteCount(75L);
            stats.setDuration("PT5M30S");

            return stats;

        } catch (Exception e) {
            log.error("Error fetching YouTube stats: ", e);
            throw new PlatformOperationException("Failed to fetch YouTube stats");
        }
    }
}
