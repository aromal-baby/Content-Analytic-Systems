package com.contentanalytics.content_analytic_system.model.dto;

import com.contentanalytics.content_analytic_system.model.enums.Platform;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContentIdentifierDTO {

    private Platform platform;
    private String contentIdentifier;
    private String contentUrl;

}
