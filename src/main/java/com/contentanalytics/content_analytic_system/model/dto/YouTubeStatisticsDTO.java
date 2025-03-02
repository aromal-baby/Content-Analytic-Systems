package com.contentanalytics.content_analytic_system.model.dto;


import lombok.Data;
import java.time.LocalDateTime;

@Data
public class YouTubeStatisticsDTO {

    private Long viewCount;
    private Long likeCount;
    private Long commentCount;
    private Long shareCount;
    private String videoId;
    private String title;
    private Long favoriteCount;
    private String duration;
    private LocalDateTime fetchedAt;

}
