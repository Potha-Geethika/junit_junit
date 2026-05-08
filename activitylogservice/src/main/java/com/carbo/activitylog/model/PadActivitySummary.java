package com.carbo.activitylog.model;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PadActivitySummary {

    private String serviceOrganizationName;
    private PadActivityBreakdown padActivityBreakdown;
//    private TotalActivityHours totalActivityHours;
    private NptByParty nptByParty;
    private ProFracNptBreakdown proFracNptBreakdown;
    private NonProFracNptBreakdown nonProFracNptBreakdown;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PadActivityBreakdown {
        private String totalActivityhours;
        private List<ActivityTotal> padTotals;
        private List<ActivityPerStage> perStage;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ActivityTotal {
            private String activity;
            private String time;
            private Double percentage;
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ActivityPerStage {
            private String activity;
            private String time;
        }
    }

//    @Data
//    @Builder
//    @NoArgsConstructor
//    @AllArgsConstructor
//    public static class TotalActivityHours {
//        private Long totalHours;
//        private List<ActivityBreakdown> breakdown;
//
//        @Data
//        @Builder
//        @NoArgsConstructor
//        @AllArgsConstructor
//        public static class ActivityBreakdown {
//            private String activity;
//            private Long time;
//            private Double percentage;
//        }
//    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NptByParty {
        private String totalNpt;
        private List<NptBreakdown> breakdown;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class NptBreakdown {
            private String classification;
            private String time;
            private Double percentage;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProFracNptBreakdown {
        private String totalProFracNpt;
        private List<NptClassification> classifications;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class NptClassification {
            private String classification;
            private String time;
            private Double percentage;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NonProFracNptBreakdown {
        private String totalNonProFracNpt;
        private List<NptClassification> classifications;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class NptClassification {
            private String classification;
            private String time;
            private Double percentage;
        }
    }
}