package com.contentanalytics.content_analytic_system.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MediumStatisticsDTO {

    private Long views;
    private Long reads;
    private Long claps;
    private Long responses; //Comments
    private String postId;
    private String title;
    private LocalDateTime fetchedAt;

}
