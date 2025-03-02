package com.contentanalytics.content_analytic_system.model.dto;

import lombok.Data;

import java.util.List;

public class VisualizationDTO {

    @Data
    public static class ChartDataDTO {

        private String title;
        private String type;    // lien, bar, pie, etc..(graphical rep)
        private List<String> labels;
        private List<DatasetDTO> datasets;
        private ChartOptionsDTO options;

        // SETTERS & GETTERS
        public void setDatasets(List<DatasetDTO> datasets) {
            this.datasets = datasets;
        }
    }

    @Data
    public static class DatasetDTO {

        private String label;
        private List<Number> data;
        private String backgroundColor;
        private String borderColor;
        private boolean fill = false;
        private int borderWidth;

    }

    @Data
    public static class ChartOptionsDTO {

        private AxesDTO scales;
        private boolean responsive = true;
        private LegendDTO legend;
        private TooltipDTO tooltip;

    }

    @Data
    public static class AxesDTO {

        private AxisDTO x;
        private AxisDTO y;

    }

    @Data
    public static class AxisDTO {

        private String title;
        private boolean display = true;
        private String position = "top";

    }

    @Data
    public static class LegendDTO {

        private boolean display = true;
        private String position = "top";

    }

    @Data
    public static class TooltipDTO {

        private boolean enabled = true;
        private String mode = "index";

    }

}