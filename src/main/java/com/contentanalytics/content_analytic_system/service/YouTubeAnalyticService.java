package com.contentanalytics.content_analytic_system.service;

import com.contentanalytics.content_analytic_system.config.YouTubeConfig;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import lombok.Data;

@Service
public class YouTubeAnalyticService {


    private YouTubeConfig youTubeConfig;
    private final RestTemplate restTemplate;
    private final String BASE_URL = "https://www.googleapis.com/youtube/v3";

    public YouTubeAnalyticService(YouTubeConfig youTubeConfig, RestTemplate restTemplate) {
        this.youTubeConfig = youTubeConfig;
        this.restTemplate = new RestTemplate();
    }

    @Data
    public static class VideoStats {
        private long viewCount;
        private long likeCount;
        private long commentCount;
        private long shareCount;
        private long favoriteCount;
        private String duration;

        // Ensuring proper getter naming conventions
        public long getViewCount() {
            return viewCount;
        }

        public long getLikeCount() {
            return likeCount;
        }

        public long getCommentCount() {
            return commentCount;
        }

        public long getShareCount() {
            return shareCount;
        }

        public long getFavoriteCount() {
            return favoriteCount;
        }

        public String getDuration() {
            return duration;
        }

        // Setter
        public void setViewCount(long viewCount) {
            this.viewCount = viewCount;
        }

        public void setLikeCount(long likeCount) {
            this.likeCount = likeCount;
        }

        public void setCommentCount(long commentCount) {
            this.commentCount = commentCount;
        }

        public void setShareCount(long shareCount) {
            this.shareCount = shareCount;
        }

        public void setFavoriteCount(long favoriteCount) {
            this.favoriteCount = favoriteCount;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }
    }

    @Data
    public static class VideoStatsResponse {
        private VideoStats statistics;

        public VideoStats getStats() {

            return statistics;
        }
    }

    public VideoStats getVideoStats(String videoId) {

        VideoStats stats = new VideoStats();
        // Dummy data
        stats.setViewCount(1000L);
        stats.setLikeCount(150L);
        stats.setCommentCount(75L);
        stats.setShareCount(105L);
        stats.setFavoriteCount(100L);
        stats.setDuration(videoId);
        return stats;
    }
}
