package com.contentanalytics.content_analytic_system.config;

import com.contentanalytics.content_analytic_system.model.entity.Content;
import com.contentanalytics.content_analytic_system.model.enums.Platform;
import com.contentanalytics.content_analytic_system.repository.sql.IContentRepository;
import com.contentanalytics.content_analytic_system.service.ContentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Profile({"dev", "default"}) // only run in development or default profile
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);
    private final IContentRepository contentRepository;
    private final ContentService contentService;

    public DatabaseSeeder(IContentRepository contentRepository,
                          ContentService contentService) {

        this.contentRepository = contentRepository;
        this.contentService = contentService;

    }

    @Override
    public void run(String... args) {
        if (contentRepository.count() == 0) {
            log.info("Starting database seeding...");
            List<Content> contentsToSave = new ArrayList<>();

            // YouTube Tutorial
            Content youtubeContent1 = new Content();
            youtubeContent1.setTitle("Java Programming Masterclass");
            youtubeContent1.setPlatform(Platform.YOUTUBE);
            youtubeContent1.setContentIdentifier("JAVA123");
            youtubeContent1.setContentUrl("https://youtube.com/watch?v=JAVA123");
            youtubeContent1.setDescription("Complete Java programming course");
            youtubeContent1.setViews(25000L);
            youtubeContent1.setLikes(1800L);
            youtubeContent1.setShares(500L);
            youtubeContent1.setComments(320L);
            youtubeContent1.setStatus(Content.ContentStatus.ACTIVE);
            youtubeContent1.setSourceType(Content.SourceType.MANUAL);
            contentsToSave.add(youtubeContent1);

            // YouTube Entertainment
            Content youtubeContent2 = new Content();
            youtubeContent2.setTitle("Travel Vlog: Berlin Adventure");
            youtubeContent2.setPlatform(Platform.YOUTUBE);
            youtubeContent2.setContentIdentifier("VLOG456");
            youtubeContent2.setContentUrl("https://youtube.com/watch?v=VLOG456");
            youtubeContent2.setDescription("Exploring Berlin's hidden gems");
            youtubeContent2.setViews(15000L);
            youtubeContent2.setLikes(2200L);
            youtubeContent2.setShares(800L);
            youtubeContent2.setComments(445L);
            youtubeContent2.setStatus(Content.ContentStatus.ACTIVE);
            youtubeContent2.setSourceType(Content.SourceType.MANUAL);
            contentsToSave.add(youtubeContent2);

            // Medium Technical Article
            Content mediumContent1 = new Content();
            mediumContent1.setTitle("Mastering Spring Boot Development");
            mediumContent1.setPlatform(Platform.MEDIUM);
            mediumContent1.setContentIdentifier("SPRING789");
            mediumContent1.setContentUrl("https://medium.com/article/SPRING789");
            mediumContent1.setDescription("In-depth Spring Boot guide");
            mediumContent1.setViews(3500L);
            mediumContent1.setLikes(250L);
            mediumContent1.setShares(120L);
            mediumContent1.setStatus(Content.ContentStatus.ACTIVE);
            mediumContent1.setSourceType(Content.SourceType.MANUAL);
            contentsToSave.add(mediumContent1);

            // Medium Career Article
            Content mediumContent2 = new Content();
            mediumContent2.setTitle("Software Career Growth Tips");
            mediumContent2.setPlatform(Platform.MEDIUM);
            mediumContent2.setContentIdentifier("CAREER101");
            mediumContent2.setContentUrl("https://medium.com/article/CAREER101");
            mediumContent2.setDescription("Tips for software developers");
            mediumContent2.setViews(5200L);
            mediumContent2.setLikes(480L);
            mediumContent2.setShares(190L);
            mediumContent2.setStatus(Content.ContentStatus.ACTIVE);
            mediumContent2.setSourceType(Content.SourceType.MANUAL);
            contentsToSave.add(mediumContent2);


            // WordPress Blog Post
            Content wordpressContent = new Content();
            wordpressContent.setTitle("10 Advanced Java Techniques");
            wordpressContent.setPlatform(Platform.WORDPRESS);
            wordpressContent.setContentIdentifier("wp-java-techniques");
            wordpressContent.setContentUrl("https://techblog.com/advanced-java-techniques");
            wordpressContent.setDescription("Advanced techniques for Java developers");
            wordpressContent.setViews(3800L);
            wordpressContent.setLikes(210L);
            wordpressContent.setComments(45L);
            wordpressContent.setShares(95L);
            wordpressContent.setStatus(Content.ContentStatus.ACTIVE);
            wordpressContent.setSourceType(Content.SourceType.MANUAL);
            contentsToSave.add(wordpressContent);


            // Custom Website Content
            Content websiteContent = new Content();
            websiteContent.setTitle("Portfolio Project Showcase");
            websiteContent.setPlatform(Platform.CUSTOM_WEBSITE);
            websiteContent.setContentIdentifier("PORT123");
            websiteContent.setContentUrl("https://myportfolio.com/projects");
            websiteContent.setDescription("Showcase of development projects");
            websiteContent.setViews(1200L);
            websiteContent.setStatus(Content.ContentStatus.ACTIVE);
            mediumContent2.setSourceType(Content.SourceType.MANUAL);
            contentsToSave.add(mediumContent2);

            try {
                // Usse Content service for proper initialization where possible
                for (Content content : contentsToSave) {
                    try {
                        // Setting timeStamps since its manuall
                        if(content.getCreatedAt() == null) {
                            content.setCreatedAt(LocalDateTime.now());
                        }
                        if(content.getUpdatedAt() == null) {
                            content.setUpdatedAt(LocalDateTime.now());
                        }
                        contentRepository.save(content);
                        log.info("Saved Content: {}", content.getTitle());
                    } catch (Exception e) {
                        log.info("Error saving Content {}: {}", content.getTitle(), e.getMessage());
                    }
                }

                log.info("Database seeding completed successfully!");
            } catch (Exception e) {
                log.error("Error during database seeding: {}", e.getMessage());
                e.printStackTrace();
            }
        } else {
            log.info("Database already contains data - skipping seeding");
        }
    }
}