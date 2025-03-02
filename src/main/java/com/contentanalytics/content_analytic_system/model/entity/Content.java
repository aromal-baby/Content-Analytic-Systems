// Defining package where the class belongs
package com.contentanalytics.content_analytic_system.model.entity;

// Importing the enum
import com.contentanalytics.content_analytic_system.model.enums.Platform;
import jakarta.persistence.*;   // For JPA annotations (database mapping
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;     // For automatic getter/setter generation
import org.checkerframework.checker.units.qual.C;
import org.checkerframework.checker.units.qual.N;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;       // For date and time


@Data       // Lombok annotation to generate Getters and Setters, toString(), equals(), and hashcode() methods and required constructors
@Entity     // Marks the class as a JPA entity (will be a table in database)
@Table(name = "content")        //specifies the db table name

public class Content {

    // constructor for ensuring all fields are properly initialized
    public Content() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.sourceType = SourceType.MANUAL;
        this.status = ContentStatus.ACTIVE;
    }

    @Id     // Primary key
    @GeneratedValue(strategy  = GenerationType.IDENTITY)        //Automatically generates ID (auto-increment)
    private Long id;

    // Content tittle
    @NotBlank(message = "Title is required")
    @Column(nullable = false)
    private String title;

    // USing the simplified platform enum :: To specify the platform
    @NotNull(message = "Platform is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    // Platform-specific ID/URL
    @NotBlank(message = "Content identifier is required")
    @Column(name = "content_identifier", nullable = false)
    private String contentIdentifier;

    // Content link
    @NotBlank(message = "Content Url is required")
    @Column(name = "content_url", nullable = false)
    private String contentUrl;

    // Description about the video
    @Column(columnDefinition = "TEXT")
    private String description; // Optional so no nullable = false

    // Basic matrix - common across platforms
    @ColumnDefault("0")
    @Column(nullable = false)
    private long views = 0;

    @ColumnDefault("0")
    @Column(nullable = false)
    private long likes = 0;

    @ColumnDefault("0")
    @Column(nullable = false)
    private long comments = 0;  // Optional, is platform-specific

    @ColumnDefault("0")
    @Column(nullable = false)
    private long shares = 0;

    // Platform-specific metrics stored as JSON
    @Column(name = "platform_metrics", columnDefinition = "JSON")
    private String platformMetrics;

    // Source from where the content is stored into the memory
    @Column(name = "source_type")
    @Enumerated(EnumType.STRING)
    private SourceType sourceType = SourceType.MANUAL;

    // Content status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentStatus status = ContentStatus.ACTIVE;

    // Time when created
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Time when the content was last updated
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // To know when we last pulled the metrics
    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;


    // Status enum
    public enum ContentStatus {
        ACTIVE,
        INACTIVE,
        ARCHIVED,
        DELETED
    }

    // Source enum
    public enum SourceType {
        MANUAL,     // Manually added content
        URL_IMPORT, // Added via URL parsing
        API_IMPORT  // Added via API integration
    }



    @PrePersist
    protected void onCreate() {

        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();

        // Initialize metrics to 0 if they're null
        if (views < 0) views = 0L;
        if (likes < 0) likes = 0L;
        if (shares < 0) shares = 0L;
        if(comments < 0) comments = 0L;

        // Validate platform-specific defaults
        initializePlatformSpecificDefaults();
    }


    // Update method
    @PreUpdate
    protected void onUpdate() {
        // update the timestamp whenever the content is modified
        updatedAt = LocalDateTime.now();
    }


    private void initializePlatformSpecificDefaults() {

        // Validating metrics based on platform
        if (platform == null) return;

        switch (platform) {
            case YOUTUBE:
                if (views < 0) views = 0L;
                if (likes < 0) likes = 0L;
                if (comments < 0) comments = 0L;
                if (shares < 0) shares = 0L;
                break;

            case MEDIUM:
                if (views < 0) views = 0L;
                if (likes < 0) likes = 0L;
                if (shares < 0) shares = 0L;
                this.comments = 0L; // Medium doesn't have comments
                break;

            case CUSTOM_WEBSITE:
                // Custom website might only have views
                if (views < 0) views = 0L;
                this.likes = 0L;
                this.shares = 0L;
                this.comments = 0L;
                break;

            case WORDPRESS:
                if (views < 0) views = 0L;
                if (likes < 0) likes = 0L;
                if (shares < 0) shares = 0L;
                if (comments < 0) comments = 0L;
                break;

            default:
               if (platform.isBeta()) {
                    // For beta platforms, initialize all to 0
                   this.views = 0L;
                   this.likes = 0L;
                   this.shares = 0L;
                   this.comments = 0L;
            } else {
                throw new IllegalStateException("Unexpected value: " + platform);
            }
        }
    }
}
