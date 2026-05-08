package com.carbo.activitylog.model;

import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ActivityLogSummary {

    public static final String RIG_UP_OR_DOWN_CODE = "Rig Up/Rig Down";
    public static final String PUMP_TIME_CODE = "Pump Time";
    public static final float TOTAL_MILLI_SEC_IN_AN_HOUR = 60 * 60 * 1000;

    private List<ActivityDay> activityDays = new ArrayList<>();
    private Float scheduledTimeHours = 0.0f;
    private Float pumpTimeHours = 0.0f;
    private Float nptHours = 0.0f;
    private Float organizationNptHours = 0.0f;
    private Map<String, List<Float>> completedStagesByWell = new HashMap<>();
    private Long avgPumpTimeDailyMilliSec = 0l;

    private String organizationName;
    private Long startDate;

    public ActivityLogSummary() {}

    public ActivityLogSummary(List<ActivityLogEntry> all, Integer curDay, String organizationName) {
        Set<Integer> allDays = all.stream().map(each -> each.getDay()).collect(Collectors.toSet());
        List<Integer> dayNumbers = asSortedList(allDays);

        activityDays = dayNumbers.stream().map(each -> createDay(all, each,false)).collect(Collectors.toList());
        activityDays = asSortedList(activityDays);

        scheduledTimeHours = all.stream().filter(each -> each.getOpsActivity().equals("Scheduled Time"))
                .map(each -> each.getMillisecondsSpan())
                .reduce(0L, Long::sum)/3600000.0f;
        pumpTimeHours = all.stream().filter(each -> each.getEventOrNptCode().equals("Pump Time"))
                .map(each -> each.getMillisecondsSpan())
                .reduce(0L, Long::sum)/3600000.0f;
        nptHours = all.stream().filter(each -> !each.getOpsActivity().equals("Scheduled Time"))
                .map(each -> each.getMillisecondsSpan())
                .reduce(0L, Long::sum)/3600000.0f;
        organizationNptHours = all.stream().filter(each -> each.getOpsActivity().equals(organizationName + " NPT"))
                .map(each -> each.getMillisecondsSpan())
                .reduce(0L, Long::sum)/3600000.0f;

        if (curDay != null) {
            avgPumpTimeDailyMilliSec = getAvgPumpTimeDailyMilliSec(curDay, all);
        }

        List<ActivityLogEntry> completed = all.stream()
                .filter(each -> each.getComplete())
                .collect(Collectors.toList());
        completed.forEach(each -> {
            addCompletedStage(each.getWell(), each.getStage());
        });
    }

    public ActivityLogSummary(List<ActivityLogEntry> all, Integer curDay, String organizationName, Long startDate, boolean isSimulFracJob) {
        Set<Integer> allDays = all.stream().map(each -> each.getDay()).collect(Collectors.toSet());
        List<Integer> dayNumbers = asSortedList(allDays);

        activityDays = dayNumbers.stream().map(each -> createDay(all, each,isSimulFracJob)).collect(Collectors.toList());
        activityDays = asSortedList(activityDays);

        scheduledTimeHours = all.stream().filter(each -> each.getOpsActivity().equals("Scheduled Time"))
                .map(each -> each.getMillisecondsSpan())
                .reduce(0L, Long::sum)/3600000.0f;
        pumpTimeHours = all.stream().filter(each -> each.getEventOrNptCode().equals("Pump Time"))
                .map(each -> each.getMillisecondsSpan())
                .reduce(0L, Long::sum)/3600000.0f;
        nptHours = all.stream().filter(each -> !each.getOpsActivity().equals("Scheduled Time"))
                .map(each -> each.getMillisecondsSpan())
                .reduce(0L, Long::sum)/3600000.0f;
        organizationNptHours = all.stream().filter(each -> each.getOpsActivity().equals(organizationName + " NPT"))
                .map(each -> each.getMillisecondsSpan())
                .reduce(0L, Long::sum)/3600000.0f;
        this.startDate = startDate;

        if (curDay != null) {
            avgPumpTimeDailyMilliSec = getAvgPumpTimeDailyMilliSec(curDay, all);
        }

        List<ActivityLogEntry> completed = all.stream()
                .filter(each -> each.getComplete())
                .collect(Collectors.toList());
        completed.forEach(each -> {
            addCompletedStage(each.getWell(), each.getStage());
        });
    }

    private long getAvgPumpTimeDailyMilliSec(int curDay, List<ActivityLogEntry> allActivityLogEntries) {
        List<ActivityLogEntry> filtered = allActivityLogEntries.stream().filter(each -> each.getDay() <= curDay).collect(Collectors.toList());
        Float totalPumpTimeHours = getTotalTimeOfActivityMilliSec(filtered, PUMP_TIME_CODE)/TOTAL_MILLI_SEC_IN_AN_HOUR;

        Float totalRigUpRigDownHours = getTotalTimeOfActivityMilliSec(filtered, RIG_UP_OR_DOWN_CODE)/TOTAL_MILLI_SEC_IN_AN_HOUR;

        Float hours = totalPumpTimeHours/((24 * curDay - totalRigUpRigDownHours)/24);
        return (long) (hours * TOTAL_MILLI_SEC_IN_AN_HOUR);
    }

    private long getTotalTimeOfActivityMilliSec(List<ActivityLogEntry> allActivityLogEntries, String activityCode) {
        long totalTime = 0l;
        for (ActivityLogEntry activity: allActivityLogEntries) {
            String curCode = activity.getSubNptCode().isEmpty() ? activity.getEventOrNptCode() : activity.getSubNptCode();
            if (curCode.equals(activityCode)) {
                totalTime += activity.getMillisecondsSpan();
            }
        }

        return totalTime;
    }

    private void addCompletedStage(String wellName, Float stage) {
        if (completedStagesByWell.get(wellName) == null) {
            completedStagesByWell.put(wellName, new ArrayList<Float>() {{
                add(stage);
            }});
        }
        else {
            completedStagesByWell.get(wellName).add(stage);
        }
    }

    public List<ActivityDay> getActivityDays() {
        return activityDays;
    }

    public Float getScheduledTimeHours() {
        return scheduledTimeHours;
    }

    public Float getNptHours() {
        return nptHours;
    }

    public Float getOrganizationNptHours() {
        return organizationNptHours;
    }

    public Float getPumpTimeHours() {
        return pumpTimeHours;
    }

    public Long getAvgPumpTimeDailyMilliSec() {
        return avgPumpTimeDailyMilliSec;
    }

    public Map<String, List<Float>> getCompletedStagesByWell() {
        return completedStagesByWell;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    private static <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
        List<T> list = new ArrayList<>(c);
        java.util.Collections.sort(list);
        return list;
    }

    private static ActivityDay createDay (List<ActivityLogEntry> all, Integer day,boolean isSimulFracJob) {
        Stream<ActivityLogEntry> allActivityLogEntriesForDay = all.stream().filter(each -> each.getDay().equals(day));
        double totalHours;
        if (isSimulFracJob){
          totalHours = allActivityLogEntriesForDay.map(each -> each.getMillisecondsSpan()).reduce(0L, (a, b) -> a + b)/3.6e+6;
        }
        else {
            totalHours = allActivityLogEntriesForDay
                    // First, filter out invalid logs
                    .filter(each -> each.getStart() != null && each.getEnd() != null)
                    // Use a map with composite key (start + end) to ensure unique time ranges
                    .collect(Collectors.toMap(
                            each -> each.getStart() + "-" + each.getEnd(), // composite key
                            each -> each.getMillisecondsSpan(),            // store duration
                            (existing, replacement) -> existing            // if duplicate, keep one
                    ))
                    .values()
                    .stream()
                    .reduce(0L, Long::sum) / 3.6e+6;
        }
        return new ActivityDay(day, totalHours);
    }

    private static Optional<ActivityDay> findIncompleteDay (List<ActivityDay> activityDays) {
        return activityDays.stream().filter(each -> each.getTotalHours() < 24).findFirst();
    }
}
