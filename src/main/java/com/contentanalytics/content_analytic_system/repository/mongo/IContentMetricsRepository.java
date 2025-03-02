package com.contentanalytics.content_analytic_system.repository.mongo;

import com.contentanalytics.content_analytic_system.model.mongo.ContentMetrics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IContentMetricsRepository extends MongoRepository<ContentMetrics, String> {
    // To find metrics for specific content
    List<ContentMetrics> findByContentId(Long contentId);

    Optional<ContentMetrics> findTopByContentIdOrderByTimestampDesc(Long contentId);

    // To find metrics within a date range
    List<ContentMetrics> findByContentIdAndTimestampBetween(
            Long contentId,
            LocalDateTime start,
            LocalDateTime end
    );



    void deleteByContentId(Long attr0);
    List<ContentMetrics> findByContentIdOrderByTimestampDesc(long contentId);
}
