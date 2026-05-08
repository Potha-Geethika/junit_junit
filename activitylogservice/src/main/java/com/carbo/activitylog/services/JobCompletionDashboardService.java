package com.carbo.activitylog.services;

import com.carbo.activitylog.model.*;
import com.carbo.activitylog.repository.ActivityLogMongoDbRepository;
import com.carbo.activitylog.repository.JobMongoDbRepository;
import com.carbo.activitylog.model.Job;
import com.carbo.activitylog.repository.OrganizationMongoDbRepository;
import com.carbo.activitylog.utils.Constants;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


import static com.carbo.activitylog.utils.CommonUtils.resolveTimeZone;
import static com.carbo.activitylog.utils.ControllerUtil.getOrganizationId;
import static com.carbo.activitylog.utils.ControllerUtil.getOrganizationType;
import static com.carbo.activitylog.utils.ControllerUtil.getOrganizationName;

@Service
public class JobCompletionDashboardService {

    private final ActivityLogMongoDbRepository activityLogMongoDbRepository;
    private final JobMongoDbRepository jobMongoDbRepository;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm");
    private final OrganizationMongoDbRepository organizationMongoDbRepository;

    @Autowired
    public JobCompletionDashboardService(ActivityLogMongoDbRepository activityLogMongoDbRepository, JobService jobService, JobMongoDbRepository jobMongoDbRepository,
                                         OrganizationMongoDbRepository organizationMongoDbRepository) {
        this.activityLogMongoDbRepository = activityLogMongoDbRepository;
        this.jobMongoDbRepository = jobMongoDbRepository;

        this.organizationMongoDbRepository = organizationMongoDbRepository;
    }

    private Optional<Job> getJobById(HttpServletRequest request, String jobId) {
        String userOrgId = getOrganizationId(request);
        String orgType = "OPERATOR";
        try {
            orgType = getOrganizationType(request);
        } catch (Exception e) {}

        if ("OPERATOR".equalsIgnoreCase(orgType)) {
            return jobMongoDbRepository.findByIdAndSharedWithOrganizationId(jobId, userOrgId);
        } else {
            return jobMongoDbRepository.findByIdAndOrganizationId(jobId, userOrgId);
        }

    }

    public PadActivitySummary getPadSummary(HttpServletRequest request, String jobId) {

        Optional<Job> jobOptional = getJobById(request, jobId);
        if (jobOptional.isEmpty()) {
            return new PadActivitySummary();
        }
        Job job = jobOptional.get();


        String dataOwnerOrgId = job.getOrganizationId();
        String organizationName;
        Optional<Organization> organization = organizationMongoDbRepository.findById(dataOwnerOrgId);
        if(ObjectUtils.isEmpty(organization)){
            organizationName = getOrganizationName(request);
        } else{
            organizationName = organization.get().getName();
        }

        List<ActivityLogEntry> activityLogEntryList = activityLogMongoDbRepository
                .findByOrganizationIdAndJobId(dataOwnerOrgId, jobId);

        List<ActivityLogEntry> scheduledTimeEntries = activityLogEntryList.stream()
                .filter(e -> e.getOpsActivity() != null && e.getOpsActivity().equals(Constants.SCHEDULED_TIME))
                .toList();

        List<ActivityLogEntry> nptEntries = activityLogEntryList.stream()
                .filter(e -> e.getOpsActivity() != null && !e.getOpsActivity().equals(Constants.SCHEDULED_TIME))
                .toList();

        List<ActivityLogEntry> organizationNptEntries = nptEntries.stream()
                .filter(e -> e.getOpsActivity().equals(organizationName + " " + Constants.NPT))
                .toList();

        List<ActivityLogEntry> nonOrgNptEntries = nptEntries.stream()
                .filter(e -> !e.getOpsActivity().equals(organizationName + " " + Constants.NPT))
                .toList();

        long maxStage = (long) activityLogEntryList.stream()
                .filter(e -> e.getStage() != null)
                .mapToDouble(ActivityLogEntry::getStage)
                .max()
                .orElse(0.0);

        long totalScheduledTime = scheduledTimeEntries.stream()
                .mapToLong(this::calculateDurationInMinutes)
                .sum();

        long totalPumpTime = scheduledTimeEntries.stream()
                .filter(e -> e.getEventOrNptCode() != null && e.getEventOrNptCode().equals(Constants.PUMP_TIME))
                .mapToLong(this::calculateDurationInMinutes)
                .sum();

        long totalNPT = nptEntries.stream()
                .mapToLong(this::calculateDurationInMinutes)
                .sum();

        long reqScheduledTime = totalScheduledTime - totalPumpTime;

        long totalActivityMinutes = activityLogEntryList.stream()
                .mapToLong(this::calculateDurationInMinutes)
                .sum();

        PadActivitySummary.PadActivityBreakdown padActivityBreakdown = buildPadActivityBreakdown(
                totalNPT, totalPumpTime, reqScheduledTime, totalActivityMinutes, maxStage);

        long totalOrganizationNPT = organizationNptEntries.stream()
                .mapToLong(this::calculateDurationInMinutes)
                .sum();

        long totalNonOrgNPT = nonOrgNptEntries.stream()
                .mapToLong(this::calculateDurationInMinutes)
                .sum();

        PadActivitySummary.NptByParty nptByParty = buildNptByParty(
                organizationName, totalNPT, totalOrganizationNPT, totalNonOrgNPT);

        Map<String, Long> organizationNptMap = organizationNptEntries.stream()
                .filter(e -> e.getEventOrNptCode() != null)
                .collect(Collectors.groupingBy(
                        ActivityLogEntry::getEventOrNptCode,
                        Collectors.summingLong(this::calculateDurationInMinutes)
                ));

        PadActivitySummary.ProFracNptBreakdown proFracNptBreakdown = buildProFracNptBreakdown(
                organizationNptMap, totalOrganizationNPT);

        Map<String, Long> nonOrgNptMap = nonOrgNptEntries.stream()
                .filter(e -> e.getEventOrNptCode() != null)
                .collect(Collectors.groupingBy(
                        ActivityLogEntry::getEventOrNptCode,
                        Collectors.summingLong(this::calculateDurationInMinutes)
                ));

        PadActivitySummary.NonProFracNptBreakdown nonProFracNptBreakdown = buildNonProFracNptBreakdown(
                nonOrgNptMap, totalNonOrgNPT);

        return PadActivitySummary.builder()
                .serviceOrganizationName(organizationName)
                .padActivityBreakdown(padActivityBreakdown)
                .nptByParty(nptByParty)
                .proFracNptBreakdown(proFracNptBreakdown)
                .nonProFracNptBreakdown(nonProFracNptBreakdown)
                .build();
    }


    private PadActivitySummary.ProFracNptBreakdown buildProFracNptBreakdown(
            Map<String, Long> nptMap,
            long totalOrganizationNPT) {

        List<PadActivitySummary.ProFracNptBreakdown.NptClassification> classifications = nptMap.entrySet().stream()
                .map(entry -> PadActivitySummary.ProFracNptBreakdown.NptClassification.builder()
                        .classification(entry.getKey())
                        .time(formatDuration(entry.getValue()))
                        .percentage(calculatePercentage(entry.getValue(), totalOrganizationNPT))
                        .build())
                .collect(Collectors.toList());

        return PadActivitySummary.ProFracNptBreakdown.builder()
                .totalProFracNpt(formatDuration(totalOrganizationNPT))
                .classifications(classifications)
                .build();
    }

    private PadActivitySummary.NonProFracNptBreakdown buildNonProFracNptBreakdown(
            Map<String, Long> nptMap,
            long totalNonOrgNPT) {

        List<PadActivitySummary.NonProFracNptBreakdown.NptClassification> classifications = nptMap.entrySet().stream()
                .map(entry -> PadActivitySummary.NonProFracNptBreakdown.NptClassification.builder()
                        .classification(entry.getKey())
                        .time(formatDuration(entry.getValue()))
                        .percentage(calculatePercentage(entry.getValue(), totalNonOrgNPT))
                        .build())
                .collect(Collectors.toList());

        return PadActivitySummary.NonProFracNptBreakdown.builder()
                .totalNonProFracNpt(formatDuration(totalNonOrgNPT))
                .classifications(classifications)
                .build();
    }

    private PadActivitySummary.NptByParty buildNptByParty(
            String organizationName,
            long totalNPT,
            long totalOrganizationNPT,
            long totalNonOrgNPT) {

        List<PadActivitySummary.NptByParty.NptBreakdown> breakdown = new ArrayList<>();

        if (totalOrganizationNPT > 0) {
            breakdown.add(PadActivitySummary.NptByParty.NptBreakdown.builder()
                    .classification(organizationName + " " + Constants.NPT)
                    .time(formatDuration(totalOrganizationNPT))
                    .percentage(calculatePercentage(totalOrganizationNPT, totalNPT))
                    .build());
        }

        if (totalNonOrgNPT > 0) {
            breakdown.add(PadActivitySummary.NptByParty.NptBreakdown.builder()
                    .classification("Non-" + organizationName + " " + Constants.NPT)
                    .time(formatDuration(totalNonOrgNPT))
                    .percentage(calculatePercentage(totalNonOrgNPT, totalNPT))
                    .build());
        }

        return PadActivitySummary.NptByParty.builder()
                .totalNpt(formatDuration(totalNPT))
                .breakdown(breakdown)
                .build();
    }


    private PadActivitySummary.PadActivityBreakdown buildPadActivityBreakdown(
            long totalNPT,
            long totalPumpTime,
            long reqScheduledTime,
            long totalActivityMinutes,
            long maxStage) {

        List<PadActivitySummary.PadActivityBreakdown.ActivityTotal> padTotals = List.of(
                PadActivitySummary.PadActivityBreakdown.ActivityTotal.builder()
                        .activity("Pump Time")
                        .time(formatDuration(totalPumpTime))
                        .percentage(calculatePercentage(totalPumpTime, totalActivityMinutes))
                        .build(),
                PadActivitySummary.PadActivityBreakdown.ActivityTotal.builder()
                        .activity("NPT")
                        .time(formatDuration(totalNPT))
                        .percentage(calculatePercentage(totalNPT, totalActivityMinutes))
                        .build(),
                PadActivitySummary.PadActivityBreakdown.ActivityTotal.builder()
                        .activity("Scheduled Time")
                        .time(formatDuration(reqScheduledTime))
                        .percentage(calculatePercentage(reqScheduledTime, totalActivityMinutes))
                        .build()
        );
        List<PadActivitySummary.PadActivityBreakdown.ActivityPerStage> perStage = new ArrayList<>();
        if (maxStage > 0) {
            perStage.add(PadActivitySummary.PadActivityBreakdown.ActivityPerStage.builder()
                    .activity("Pump Time")
                    .time(formatDuration(totalPumpTime / maxStage))
                    .build());

            perStage.add(PadActivitySummary.PadActivityBreakdown.ActivityPerStage.builder()
                    .activity("NPT")
                    .time(formatDuration(totalNPT / maxStage))
                    .build());

            perStage.add(PadActivitySummary.PadActivityBreakdown.ActivityPerStage.builder()
                    .activity("Scheduled Time")
                    .time(formatDuration(reqScheduledTime / maxStage))
                    .build());
        }

        return PadActivitySummary.PadActivityBreakdown.builder()
                .totalActivityhours(formatDuration(totalActivityMinutes))
                .padTotals(padTotals)
                .perStage(perStage)
                .build();


    }

    private double calculatePercentage(long value, long total) {
        if (total == 0) {
            return 0.0;
        }
        return Math.round((value * 100.0 / total) * 100.0) / 100.0; // Round to 2 decimal places
    }

    private long calculateDurationInMinutes(ActivityLogEntry entry) {
        LocalDateTime start = LocalDateTime.parse(entry.getStart(), formatter);
        LocalDateTime end = LocalDateTime.parse(entry.getEnd(), formatter);
        return Duration.between(start, end).toMinutes();
    }

    private String formatDuration(long totalMinutes) {
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;
        return String.format("%d:%02d", hours, minutes);
    }

    public List<StagePerDay> getStagesPerDay(HttpServletRequest request, String jobId) {
        // CHANGE: Removed direct assignment from request
        // String organizationId = getOrganizationId(request);

        List<StagePerDay> stagesPerDay = new ArrayList<>();

        // CHANGE: Fetch Job securely
        Optional<Job> jobOptional = getJobById(request, jobId);
        if (jobOptional.isEmpty()) {
            return stagesPerDay;
        }
        Job job = jobOptional.get();

        // CHANGE: Use Job's Org ID (Service Company)
        String dataOwnerOrgId = job.getOrganizationId();

        List<ActivityLogEntry> activityLogEntries =
                activityLogMongoDbRepository.findByOrganizationIdAndJobId(dataOwnerOrgId, jobId);

        if (ObjectUtils.isEmpty(activityLogEntries)) {
            return stagesPerDay;
        }

        int maxDay = activityLogEntries.stream()
                .mapToInt(ActivityLogEntry::getDay)
                .max()
                .orElse(0);

        Map<Integer, List<ActivityLogEntry>> activityLogEntryPerDayMap = activityLogEntries.stream()
                .filter(ActivityLogEntry::getComplete)
                .collect(Collectors.groupingBy(ActivityLogEntry::getDay));

        int targetStagePerDay = job.getTargetStagesPerDay();
        long jobStartDate = job.getStartDate();  // epoch millis

        // =============================
        //       TIME ZONE HANDLING
        // =============================

        ZoneId zoneId = resolveTimeZone(request);

        // Convert start date to local date using zone
        LocalDate startDate = Instant.ofEpochMilli(jobStartDate)
                .atZone(zoneId)
                .toLocalDate();

        // Build day-wise data
        for (int day = 1; day <= maxDay; day++) {

            LocalDate currentDate = startDate.plusDays(day - 1);

            int completedStages = activityLogEntryPerDayMap
                    .getOrDefault(day, Collections.emptyList())
                    .size();

            StagePerDay stagePerDay = new StagePerDay();
            stagePerDay.setDay(day);
            stagePerDay.setDate(currentDate.toString());
            stagePerDay.setCompletedStagesPerDay(completedStages);
            stagePerDay.setTargetStagesPerDay(targetStagePerDay);

            stagesPerDay.add(stagePerDay);
        }

        return stagesPerDay;
    }


    public List<PumpHoursPerDay> getPumpHoursPerDay(HttpServletRequest request, String jobId) {

        // CHANGE: Removed direct assignment from request
        // String organizationId = getOrganizationId(request);
        List<PumpHoursPerDay> response = new ArrayList<>();

        // CHANGE: Fetch Job securely
        Optional<Job> jobOptional = getJobById(request, jobId);
        if (jobOptional.isEmpty()) {
            return response;
        }
        Job job = jobOptional.get();
        // CHANGE: Use Job's Org ID
        String dataOwnerOrgId = job.getOrganizationId();

        // Fetch Activity Log Entries
        List<ActivityLogEntry> logs =
                activityLogMongoDbRepository.findByOrganizationIdAndJobId(dataOwnerOrgId, jobId);

        if (ObjectUtils.isEmpty(logs)) {
            return response;
        }

        // Filter: completed + Pump Time only
        List<ActivityLogEntry> filteredEntries = logs.stream()
                .filter(e -> "Pump Time".equalsIgnoreCase(e.getEventOrNptCode()))
                .toList();

        if (filteredEntries.isEmpty()) {
            return response;
        }

        // Determine max day (from filtered entries only)
        int maxDay = filteredEntries.stream()
                .mapToInt(ActivityLogEntry::getDay)
                .max()
                .orElse(0);

        // Group logs by day
        Map<Integer, List<ActivityLogEntry>> logsByDay =
                filteredEntries.stream().collect(Collectors.groupingBy(ActivityLogEntry::getDay));

        // Pump Target from job setup (float hours → minutes)
        float targetPumpHours = job.getTargetDailyPumpTime();   // e.g. 1.5 → 1 hr 30 min
        int pumpTargetMinutes = Math.round(targetPumpHours * 60);

        // Timezone from header
        ZoneId zoneId = resolveTimeZone(request);

        // Convert job start date → LocalDate
        LocalDate jobStartDate = Instant.ofEpochMilli(job.getStartDate())
                .atZone(zoneId)
                .toLocalDate();

        // Build result day-by-day
        for (int day = 1; day <= maxDay; day++) {

            LocalDate date = jobStartDate.plusDays(day - 1);

            // SUM total pump milliseconds for the day
            long totalPumpMillis = logsByDay.getOrDefault(day, Collections.emptyList())
                    .stream()
                    .mapToLong(ActivityLogEntry::getMillisecondsSpan)
                    .sum();

            int totalPumpMinutes = (int) (totalPumpMillis / 60000);  // ms → minutes

            PumpHoursPerDay ph = new PumpHoursPerDay();
            ph.setDay(day);
            ph.setDate(date.toString());
            // Format actual (completed) pump time
            ph.setCompletedPumpHoursPerDay(formatMinutes(totalPumpMinutes));  // HH:mm
            // Format target pump time
            ph.setTargetPumpHoursPerDay(formatMinutes(pumpTargetMinutes));    // HH:mm

            response.add(ph);
        }

        return response;
    }

    private String formatMinutes(int minutes) {
        int hrs = minutes / 60;
        int mins = minutes % 60;
        return String.format("%02d:%02d", hrs, mins);
    }

    public List<PumpHoursPerStage> getPumpHoursPerStageFromLogs(HttpServletRequest request, String jobId, String wellName) {

        // CHANGE: Removed direct assignment
        // String organizationId = getOrganizationId(request);

        // CHANGE: Fetch Job securely
        Optional<Job> jobOptional = getJobById(request, jobId);
        if (jobOptional.isEmpty()) {
            return new ArrayList<>();
        }
        Job job = jobOptional.get();
        // CHANGE: Use Job's Org ID
        String dataOwnerOrgId = job.getOrganizationId();

        // Fetch Activity Log Entries
        List<ActivityLogEntry> logs =
                activityLogMongoDbRepository.findByOrganizationIdAndJobIdAndWell(dataOwnerOrgId, jobId, wellName);

        if (ObjectUtils.isEmpty(logs)) {
            return new ArrayList<>();
        }

        // Filter: Pump Time only
        List<ActivityLogEntry> pumpTimeLogs = logs.stream()
                .filter(e -> "Pump Time".equalsIgnoreCase(e.getEventOrNptCode()))
                .filter(e -> e.getStage() != null) // Only entries with stage
                .toList();

        if (pumpTimeLogs.isEmpty()) {
            return new ArrayList<>();
        }

        // Group by stage and sum up milliseconds
        Map<Float, Long> totalMillisByStage = pumpTimeLogs.stream()
                .collect(Collectors.groupingBy(
                        ActivityLogEntry::getStage,
                        Collectors.summingLong(ActivityLogEntry::getMillisecondsSpan)
                ));

        // Convert to response objects
        List<PumpHoursPerStage> response = totalMillisByStage.entrySet().stream()
                .map(entry -> {
                    Float stage = entry.getKey();
                    long totalMillis = entry.getValue();
                    int totalMinutes = (int) (totalMillis / 60000); // Convert to minutes

                    return PumpHoursPerStage.builder()
                            .stage(stage)
                            .totalPumpTimeMinutes(totalMinutes)
                            .totalPumpTimeFormatted(formatMinutes(totalMinutes)) // Optional: "HH:mm" format
                            .build();
                })
                .sorted(Comparator.comparing(phs -> {
                    try {
                        return phs.getStage();
                    } catch (NumberFormatException e) {
                        return 0f;
                    }
                }))
                .collect(Collectors.toList());

        return response;
    }

    public ServiceOrganizationDetails getServiceOrganization(HttpServletRequest request, String jobId) {

        String organizationType = getOrganizationType(request);
        String organizationId = getOrganizationId(request);

        ServiceOrganizationDetails details = new ServiceOrganizationDetails();

        if ("OPERATOR".equalsIgnoreCase(organizationType)) {

            jobMongoDbRepository
                    .findByIdAndSharedWithOrganizationId(jobId, organizationId)
                    .ifPresent(job -> {
                        details.setOrganizationId(job.getOrganizationId());

                        organizationMongoDbRepository
                                .findById(job.getOrganizationId())
                                .ifPresent(org ->
                                        details.setOrganizationName(org.getName())
                                );
                    });

        } else {
            details.setOrganizationId(organizationId);
            details.setOrganizationName(getOrganizationName(request));
        }

        return details;
    }

}