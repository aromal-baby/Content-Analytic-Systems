package com.contentanalytics.content_analytic_system.service;

import com.contentanalytics.content_analytic_system.model.dto.VisualizationDTO;
import com.contentanalytics.content_analytic_system.model.entity.Content;
import com.contentanalytics.content_analytic_system.model.enums.Platform;
import com.contentanalytics.content_analytic_system.repository.sql.IContentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VisualizationService {

    private final IContentRepository contentRepository;
    private final AnalyticsService analyticsService;
    private final ContentMetricsService metricsService;

    private static final String[] CHART_COLORS = {
            "#36A2EB", "#FF6384", "#4BC0C0", "#FFCE56", "#9966FF",
            "#FF9F40", "#C9CBCF", "#7FC97F", "#BEAED4", "#FDC086"
    };

    public VisualizationService(IContentRepository contentRepository,
                                AnalyticsService analyticsService,
                                ContentMetricsService metricsService) {

        this.contentRepository = contentRepository;
        this.analyticsService = analyticsService;
        this.metricsService = metricsService;

    }

    // To generate platform performance comparison chart data
    public VisualizationDTO.ChartDataDTO getPlatformComparisonChart() {
        log.info("Generating Platform Comparison Chart data");

        // Getting data for all active platforms
        List<Platform> activePlatforms = Arrays.stream(Platform.values())
                .filter(p -> !p.isBeta())
                .collect(Collectors.toList());

        // Preparing chart data
        VisualizationDTO.ChartDataDTO chartData = new VisualizationDTO.ChartDataDTO();
        chartData.setTitle("Platform performance Comparison");
        chartData.setType("bar");

        // Setting platform names as labels
        List<String> platformNames = activePlatforms.stream()
                .map(Platform::getDisplayName)
                .collect(Collectors.toList());
        chartData.setLabels(platformNames);

        // Creating dataset for different metrics
        List<VisualizationDTO.DatasetDTO> datasets = new ArrayList<>();

        // View dataset
        VisualizationDTO.DatasetDTO viewsDataset = new VisualizationDTO.DatasetDTO();
        viewsDataset.setLabel("Total View");
        viewsDataset.setBackgroundColor(CHART_COLORS[0]);

        // Engagement dataset
        VisualizationDTO.DatasetDTO engagementDataset = new VisualizationDTO.DatasetDTO();
        engagementDataset.setLabel("Engagement Rate (%)");
        engagementDataset.setBackgroundColor(CHART_COLORS[1]);

        List<Number> viewsData = new ArrayList<>();
        List<Number> engagementData = new ArrayList<>();

        // Getting data for each platform
        for (Platform platform : activePlatforms) {
            Map<String, Object> platformAnalytics = analyticsService.getPlatformAnalytics(platform);
            viewsData.add((Number) platformAnalytics.getOrDefault("totalViews", 0));

            // Calculating engagement rate
            double engagementRate = 0.0;
            if(platformAnalytics.containsKey("totalViews") &&
                    (Long) platformAnalytics.get("totalViews") > 0) {

                long totalViews = (Long) platformAnalytics.get("totalViews");
                long totalEngagements = (Long) platformAnalytics.getOrDefault("totalLikes", 0L) +
                        (Long) platformAnalytics.getOrDefault("totalComments", 0L) +
                        (Long) platformAnalytics.getOrDefault("totalShares", 0L);

                engagementRate = (double) totalEngagements / totalViews * 100;

            }

            engagementData.add(Math.round(engagementRate * 100.0) / 100.0);
        }

        viewsDataset.setData(viewsData);
        engagementDataset.setData(engagementData);

        datasets.add(viewsDataset);
        datasets.add(engagementDataset);
        chartData.setDatasets(datasets);

        // Setting chart options
        VisualizationDTO.ChartOptionsDTO options = new VisualizationDTO.ChartOptionsDTO();
        VisualizationDTO.AxesDTO scales = new VisualizationDTO.AxesDTO();

        VisualizationDTO.AxisDTO yAxis = new VisualizationDTO.AxisDTO();
        yAxis.setTitle("Value");
        yAxis.setDisplay(true);

        scales.setY(yAxis);
        options.setScales(scales);
        options.setResponsive(true);

        chartData.setOptions(options);

        return chartData;
    }


    // Generating content performance timeline chart for a specific content
    public VisualizationDTO.ChartDataDTO getContentTimelineChart(Long contentId) {
        log.info("Generating timeline chart for content ID: {}", contentId);

        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Content not found"));

        // Getting historical metrics
        List<com.contentanalytics.content_analytic_system.model.mongo.ContentMetrics>
                metrics = metricsService.getMetrics(contentId);

        if (metrics.isEmpty()) {
            log.warn("No metrics found for content ID: {}", contentId);
            return createEmptyChart("Content Timeline - " + content.getTitle());
        }

        // Sorting by timestamp (Oldest first)
        metrics.sort(Comparator.comparing(com.contentanalytics.content_analytic_system.model.mongo.ContentMetrics::getTimestamp));

        // Format dates for labels
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM dd");
        List<String> dateLabels = metrics.stream()
                .map(m -> m.getTimestamp().format(formatter))
                .collect(Collectors.toList());

        // Preparing chart data
        VisualizationDTO.ChartDataDTO chartData = new VisualizationDTO.ChartDataDTO();
        chartData.setTitle("Content Performance - " + content.getTitle());
        chartData.setType("line");
        chartData.setLabels(dateLabels);

        // Creating dataset
        List<VisualizationDTO.DatasetDTO> datasets = new ArrayList<>();

        // Views dataset
        VisualizationDTO.DatasetDTO viewsDataset = createDataset(
                "View",
                CHART_COLORS[0],
                metrics.stream()
                        .map(m -> m.getMetrics().getOrDefault("views", 0L))
                        .collect(Collectors.toList())
        );

        // Likes dataset
        VisualizationDTO.DatasetDTO likesDataset = createDataset(
                "Likes",
                CHART_COLORS[1],
                metrics.stream()
                        .map(m -> m.getMetrics().getOrDefault("likes", 0L))
                        .collect(Collectors.toList())
        );

        datasets.add(viewsDataset);
        datasets.add(likesDataset);

        // Adding comments and shares if applicable
        if (content.getPlatform() != Platform.MEDIUM) {
            VisualizationDTO.DatasetDTO commentsDataset = createDataset(
                    "Comments",
                    CHART_COLORS[2],
                    metrics.stream()
                            .map(m -> m.getMetrics().getOrDefault("comments", 0L))
                            .collect(Collectors.toList())
            );
            datasets.add(commentsDataset);
        }

        if (content.getPlatform() != Platform.CUSTOM_WEBSITE) {
            VisualizationDTO.DatasetDTO sharesDataset = createDataset(
                    "Shares",
                    CHART_COLORS[3],
                    metrics.stream()
                            .map(m -> m.getMetrics().getOrDefault("shares", 0L))
                            .collect(Collectors.toList())
            );
            datasets.add(sharesDataset);
        }

        chartData.setDatasets(datasets);

        // Setting chart options
        VisualizationDTO.ChartOptionsDTO options = new VisualizationDTO.ChartOptionsDTO();
        options.setResponsive(true);

        VisualizationDTO.LegendDTO legend = new VisualizationDTO.LegendDTO();
        legend.setPosition("bottom");
        options.setLegend(legend);

        chartData.setOptions(options);

        return chartData;
    }

    // Generating platform distribution pie chart
    public VisualizationDTO.ChartDataDTO getPlatformDistributionChart() {
        log.info("Generating Platform Distribution Chart");

        // Getting content count by platform
        Map<Platform, Long> contentCounts = Arrays.stream(Platform.values())
                .filter(p -> !p.isBeta())
                .collect(Collectors.toMap(
                        p -> p,
                        p -> (long) contentRepository.findByPlatform(p).size()
                ));

        // Preparing chart data
        VisualizationDTO.ChartDataDTO chartData = new VisualizationDTO.ChartDataDTO();
        chartData.setTitle("Content Distribution by Platform");
        chartData.setType("pie");

        // Setting platform names as labels
        List<String> platformNames = contentCounts.keySet().stream()
                .map(Platform::getDisplayName)
                .collect(Collectors.toList());
        chartData.setLabels(platformNames);

        // Creating dataset
        List<VisualizationDTO.DatasetDTO> datasets = new ArrayList<>();
        VisualizationDTO.DatasetDTO dataset = new VisualizationDTO.DatasetDTO();

        // Setting data values
        List<Number> data = contentCounts.values().stream()
                .map(Number.class::cast)
                .collect(Collectors.toList());
        dataset.setData(data);

        // setting colors
        List<String> backgroundColors = new ArrayList<>();
        for (int i = 0; i < contentCounts.size(); i++) {
            backgroundColors.add(CHART_COLORS[i % CHART_COLORS.length]);
        }
        dataset.setBackgroundColor(String.join(",", backgroundColors));

        datasets.add(dataset);
        chartData.setDatasets(datasets);

        // Setting chart options
        VisualizationDTO.ChartOptionsDTO options = new VisualizationDTO.ChartOptionsDTO();
        options.setResponsive(true);

        VisualizationDTO.LegendDTO legend = new VisualizationDTO.LegendDTO();
        legend.setPosition("right");
        options.setLegend(legend);

        chartData.setOptions(options);

        return chartData;
    }

    // HELPER methods
    private VisualizationDTO.DatasetDTO createDataset(String label, String color, List<Long> data) {

        VisualizationDTO.DatasetDTO dataset = new VisualizationDTO.DatasetDTO();
        dataset.setLabel(label);
        dataset.setBackgroundColor(color);
        dataset.setBorderColor(color);
        dataset.setData(new ArrayList<>());
        dataset.setFill(false);
        dataset.setBorderWidth(2);

        return dataset;

    }

    private VisualizationDTO.ChartDataDTO createEmptyChart(String title) {
        VisualizationDTO.ChartDataDTO chartData = new VisualizationDTO.ChartDataDTO();
        chartData.setTitle(title);
        chartData.setType("line");
        chartData.setLabels(Collections.singletonList("No Data"));

        VisualizationDTO.DatasetDTO emptyDataset = new VisualizationDTO.DatasetDTO();
        emptyDataset.setLabel("No Data available");
        emptyDataset.setData(Collections.singletonList(0));
        emptyDataset.setBackgroundColor(CHART_COLORS[0]);

        chartData.setDatasets(Collections.singletonList(emptyDataset));
        return chartData;
    }
}
