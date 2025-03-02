package com.contentanalytics.content_analytic_system.model.enums;

import lombok.Getter;

@Getter
public enum Platform {
    // Currently Implementable
    YOUTUBE("YouTube"),
    CUSTOM_WEBSITE("Custom Website"),
    MEDIUM("Medium"),
    WORDPRESS("WordPress"),

    // Future implementation
    INSTAGRAM_BETA("Instagram (coming Soon)"),
    TWITTER_BETA("Twitter (coming Soon)"),
    LINKEDIN_BETA("LinkedIn (Coming Soon))"),
    TIKTOK_BETA("TikTok(Coming Soon)");

    private final String displayName;

    Platform(String displayName) {
        this.displayName = displayName;
    }

    public boolean isBeta() {
        return this.name().endsWith("_BETA");
    }
}
