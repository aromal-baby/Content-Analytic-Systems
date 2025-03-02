package com.contentanalytics.content_analytic_system.repository.sql;

import com.contentanalytics.content_analytic_system.model.entity.Content;
import com.contentanalytics.content_analytic_system.model.enums.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface IContentRepository extends JpaRepository<Content, Long> {
    static Content addContent(Content content) {
        return content;
    }

    // Finding the content platform
    List <Content> findByPlatform(Platform platform);
    // Finding content by status
    List <Content> findByStatus(Content.ContentStatus status);

    // Find top performing content
    @Query("SELECT c FROM Content c WHERE c.platform = ?1 AND c.views> ?2")
    List <Content> findViralContent(Platform platform, Long minViews);

    //Platform metrics
    @Query("SELECT c.platform, COUNT(c), AVG(c.views), AVG(c.likes) " +
           "FROM Content c GROUP BY c.platform")
    List <Object[]> getPlatformPerformanceMetrics();

    //Best posting hours
    @Query("SELECT FUNCTION('HOUR', c.createdAt) as hour, AVG(c.views) as avgViews " +
           "FROM Content c WHERE c.platform = ?1 GROUP BY FUNCTION ('HOUR', c.createdAt)" +
           "ORDER BY avgViews DESC")
    List <Object[]> findBestPostingHours(Platform platform);

    // Engagement analytics
    @Query("SELECT c FROM Content c WHERE c.platform = ?1 " +
            "AND (c.likes * 1.0 / NULLIF(c.views, 0)) > ?2")
    List <Object[]> findHighEngagementContent(Platform platform, Double minEngagementRate);

    //Growth analytics
    @Query("SELECT DATE(c.createdAt) as date, COUNT(c) " +
           "FROM Content c WHERE c.createdAt >= ?1 " +
           "GROUP BY DATE(c.createdAt)" )
    List <Object[]> getContentGrowthOverTime(LocalDateTime since);

    // Comparing Performance
    @Query("SELECT c FROM Content c WHERE c.platform = ?1 " +
           "AND  c.views > (SELECT  AVG(c2.views) FROM Content c2 WHERE c2.platform = ?1)" )
    List <Object[]> findAboveAverageContent(Platform platform);

    // Analysing trends
    @Query("SELECT c FROM Content c WHERE c.createdAt >= ?1 " +
           "ORDER BY (c.views + c.likes + c.shares) DESC")
    List <Content> findTrendingContent(LocalDateTime since);

    // Content health
    @Query("SELECT c FROM Content c WHERE c.lastSyncedAt < ?1 " +
           "OR c.views = 0 OR c.likes = 0 ")
    List <Content> findContentNeedingAttention(LocalDateTime threshold);

    // Search content by title
    List <Content> findByTitleContainingIgnoreCase(String titlePart);

    // To find content created within date range
    List <Content> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);


}
