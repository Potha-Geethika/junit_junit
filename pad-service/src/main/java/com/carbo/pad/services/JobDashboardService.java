package com.carbo.pad.services;

import com.carbo.pad.utils.ActivityLogUtil;
import com.carbo.pad.model.*;
import com.carbo.pad.model.Error;
import com.carbo.pad.repository.JobMongoDbRepository;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.carbo.pad.utils.ActivityLogUtil.round;
import static com.carbo.pad.utils.ControllerUtil.getOrganization;
import static com.carbo.pad.utils.ControllerUtil.getOrganizationId;
import static com.carbo.pad.utils.ControllerUtil.getOrganizationType;

@Service
@Slf4j
public class JobDashboardService {

    private final MongoTemplate mongoTemplate;
    private final AICallsService aiCallsService;
    private final JobMongoDbRepository jobMongoDbRepository;

    @Autowired
    public JobDashboardService(MongoTemplate mongoTemplate, AICallsService aiCallsService,JobMongoDbRepository jobMongoDbRepository) {
        this.mongoTemplate = mongoTemplate;
        this.aiCallsService = aiCallsService;
        this.jobMongoDbRepository = jobMongoDbRepository;
    }

    /* -----------------------------------------------------------
       Helper Methods for Responses
     ----------------------------------------------------------- */

    private ResponseEntity<Error> buildErrorResponse(String code, String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(Error.builder()
                        .errorCode(code)
                        .errorMessage(message)
                        .httpStatus(status)
                        .build());
    }

    private Optional<Job> getJobById(HttpServletRequest request, String jobId) {
        String userOrgId = getOrganizationId(request);
        String orgType = "OPERATOR";
        try {
            orgType = getOrganizationType(request);
        } catch (Exception e) {
        }
        if ("OPERATOR".equalsIgnoreCase(orgType)) {
            return jobMongoDbRepository.findByIdAndSharedWithOrganizationId(jobId, userOrgId);
        } else {
            return jobMongoDbRepository.findByIdAndOrganizationId(jobId, userOrgId);
        }
    }

    public ResponseEntity<?> getPadDetails(HttpServletRequest request, String jobId) {
        try {
            String organizationName = getOrganization(request);
            String timeZone = request.getHeader("Time-Zone");

            Optional<Job> jobOptional = getJobById(request, jobId);

            if (jobOptional.isEmpty()) {
                return buildErrorResponse("NOT_FOUND", "Job not found or access denied", HttpStatus.NOT_FOUND);
            }
            Job job = jobOptional.get();
            String organizationId = job.getOrganizationId();

            PadDetailsResponse padDetailsResponse = new PadDetailsResponse();

            Query activityLogQuery = new Query(
                    Criteria.where("organizationId").is(organizationId)
                            .and("jobId").is(jobId)
            );
            List<ActivityLogEntry> activityLogEntries = mongoTemplate.find(activityLogQuery, ActivityLogEntry.class, "activity-log-entries");

            // Step 1: Fetch wells under this pad
            List<WellDetails> wells = getWellsByPad(organizationId, job);
            padDetailsResponse.setWells(wells);

            // Step 2: Calculate daily averages
            DailyAverages dailyAverages = calculateDailyAverages(organizationId, organizationName, activityLogEntries, wells, jobId);
            padDetailsResponse.setDailyAverages(dailyAverages);

            // Step 3: Calculate pad totals
            PadTotals padTotals = calculatePadTotals(organizationId, activityLogEntries, wells, jobId);
            padDetailsResponse.setPadTotals(padTotals);

            // Step 4: Determine total number of days (from Activity Logs)
            double totalDays = calculatePadDurationDays(job, activityLogEntries, timeZone);
            padDetailsResponse.setCalculatedOverDays(totalDays);

            return ResponseEntity.ok(padDetailsResponse);
        } catch (Exception e) {
            log.error("Unexpected error while fetching pad details", e);
            return buildErrorResponse("UNEXPECTED_ERROR", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /* -----------------------------------------------------------
       Pad Totals
     ----------------------------------------------------------- */

    private PadTotals calculatePadTotals(String organizationId, List<ActivityLogEntry> activityLogEntries, List<WellDetails> wells, String jobId) {
        PadTotals padTotals = new PadTotals();

        Map<String, String> wellMap = new HashMap<>();

        Query emailQuery = new Query(
                Criteria.where("organizationId").is(organizationId)
                        .and("jobId").is(jobId)
                        .and("type").is("END_STAGE")
        );
        List<EndStageEmailPayload> endStageEmails = mongoTemplate.find(emailQuery, EndStageEmailPayload.class, "emails");

        Map<String, EndStageEmailPayload> latestEmailsMap = new HashMap<>();
        if (endStageEmails != null && !endStageEmails.isEmpty()) {
            for (EndStageEmailPayload email : endStageEmails) {
                String key;
                String wellName = email.getWell();
                if (!ObjectUtils.isEmpty(wellMap.get(wellName))) {
                    key = wellMap.get(wellName) + "-" + email.getStage();
                } else {
                    key = wellName + "-" + email.getStage();
                }
                EndStageEmailPayload existing = latestEmailsMap.get(key);
                if (existing == null || email.getCreated() > existing.getCreated()) {
                    latestEmailsMap.put(key, email);
                }
            }
        }

        List<EndStageEmailPayload> latestEndStageEmails = new ArrayList<>(latestEmailsMap.values());

        padTotals.setAverageRateBpm(calculateAverageRateBpm(latestEndStageEmails, wells));
        padTotals.setAveragePressurePsi(calculateAveragePressurePsi(latestEndStageEmails, wells));
        padTotals.setTotalWaterBbls(calculateTotalWaterBbls(organizationId, jobId, wells));
        padTotals.setTotalPumpHours(calculateTotalPumpHours(activityLogEntries));
        padTotals.setTotalProppantPumpedLbs(calculateTotalProppantPumpedLbs(organizationId, jobId));

        return padTotals;
    }

    private double calculateTotalProppantPumpedLbs(String organizationId, String jobId) {

        Query query = new Query();
        query.addCriteria(Criteria.where("organizationId").is(organizationId));
        query.addCriteria(Criteria.where("jobId").is(jobId));

        List<ProppantDeliveryEntryDto> deliveryEntries =
                mongoTemplate.find(query, ProppantDeliveryEntryDto.class, "proppant-delivery-entries");

        if (deliveryEntries == null || deliveryEntries.isEmpty()) {
            return 0.0;
        }

        Map<String, Double> wellToTotalPumped = new HashMap<>();

        for (ProppantDeliveryEntryDto entry : deliveryEntries) {
            if (entry.getUsedIn() == null) continue;

            for (IMaterialUsed used : entry.getUsedIn()) {
                double pumpedLbs = used.getSubmittedAmount() != null ? used.getSubmittedAmount() : 0.0;
                wellToTotalPumped.merge(used.getWell(), pumpedLbs, Double::sum);
            }
        }

        double sumOfWellTotals = wellToTotalPumped.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        return round(sumOfWellTotals, 2);
    }

    private double calculateTotalWaterBbls(String organizationId, String jobId, List<WellDetails> wells) {

        Query chemicalStageQuery = new Query(
                Criteria.where("organizationId").is(organizationId)
                        .and("jobId").is(jobId)
        );

        List<ChemicalStage> chemicalStages =
                mongoTemplate.find(chemicalStageQuery, ChemicalStage.class, "chemical-stages");

        if (chemicalStages == null || chemicalStages.isEmpty()) {
            return 0.0;
        }

        Map<String, List<ChemicalStage>> byWell = chemicalStages.stream()
                .filter(cs -> cs.getWell() != null)
                .collect(Collectors.groupingBy(ChemicalStage::getWell));

        double sumOfWellTotals = byWell.values().stream()
                .mapToDouble(wellStages ->
                        wellStages.stream()
                                .filter(s -> s.getCleanTotal() != null)
                                .mapToDouble(ChemicalStage::getCleanTotal)
                                .sum()
                )
                .sum();

        int wellCount = (wells != null && !wells.isEmpty()) ? wells.size() : 1;
        double finalAvg = sumOfWellTotals / wellCount;

        return round(finalAvg, 2);
    }

    private double calculateAverageRateBpm(List<EndStageEmailPayload> emails, List<WellDetails> wells) {

        if (emails == null || emails.isEmpty()) return 0.0;

        Map<String, List<EndStageEmailPayload>> byWell = emails.stream()
                .collect(Collectors.groupingBy(EndStageEmailPayload::getWell));

        double sumOfWellAverages = byWell.values().stream()
                .mapToDouble(wellEmails ->
                        wellEmails.stream()
                                .filter(e -> e.getAverageRate() != null)
                                .mapToDouble(EndStageEmailPayload::getAverageRate)
                                .average()
                                .orElse(0.0)
                )
                .sum();

        int wellCount = wells != null ? wells.size() : 1;

        double finalAvg = sumOfWellAverages / wellCount;

        return round(finalAvg, 2);
    }

    private double calculateAveragePressurePsi(List<EndStageEmailPayload> emails, List<WellDetails> wells) {

        if (emails == null || emails.isEmpty()) return 0.0;

        Map<String, List<EndStageEmailPayload>> byWell = emails.stream()
                .collect(Collectors.groupingBy(EndStageEmailPayload::getWell));

        double sumOfWellAverages = byWell.values().stream()
                .mapToDouble(wellEmails ->
                        wellEmails.stream()
                                .filter(e -> e.getAveragePressure() != null)
                                .mapToDouble(EndStageEmailPayload::getAveragePressure)
                                .average()
                                .orElse(0.0)
                )
                .sum();

        int wellCount = wells != null ? wells.size() : 1;

        double finalAvg = sumOfWellAverages / wellCount;

        return round(finalAvg, 2);
    }

    private double calculateTotalPumpHours(List<ActivityLogEntry> activityLogEntries) {

        if (activityLogEntries == null || activityLogEntries.isEmpty()) {
            return 0.0;
        }

        // Filter "Pump Time" entries
        long totalPumpMs = activityLogEntries.stream()
                .filter(l -> l.getEventOrNptCode() != null && l.getEventOrNptCode().equalsIgnoreCase("Pump Time"))
                .mapToLong(ActivityLogEntry::getMillisecondsSpan)
                .sum();

        // Convert ms → hours (double)
        return totalPumpMs / (1000.0 * 60 * 60);
    }

    /* -----------------------------------------------------------
       Duration calculation
     ----------------------------------------------------------- */

    public double calculatePadDurationDays(Job job, List<ActivityLogEntry> activityLogs, String timeZoneHeader) {

        ZoneId zoneId;
        try {
            zoneId = (timeZoneHeader != null && !timeZoneHeader.isEmpty())
                    ? ZoneId.of(timeZoneHeader)
                    : ZoneId.of("UTC");
        } catch (Exception ex) {
            zoneId = ZoneId.of("UTC");
        }

        if (job == null || job.getStartDate() == null) {
            return 0.01;
        }

        Instant startInstant = Instant.ofEpochMilli(job.getStartDate());
        ZonedDateTime startDateTime = startInstant.atZone(zoneId);

        ZonedDateTime endDateTime;

        String status = job.getStatus() != null ? job.getStatus().toUpperCase() : "";

        if ("COMPLETED".equals(status)) {
            if (job.getEndDate() != null) {
                endDateTime = Instant.ofEpochMilli(job.getEndDate()).atZone(zoneId);
            } else {
                int maxDay = 1;
                if (activityLogs != null && !activityLogs.isEmpty()) {
                    maxDay = activityLogs.stream()
                            .mapToInt(ActivityLogEntry::getDay)
                            .max()
                            .orElse(1);
                }
                endDateTime = startDateTime.plusDays(maxDay - 1);
            }
        }
        else {
            endDateTime = ZonedDateTime.now(zoneId);
        }

        Duration duration = Duration.between(startDateTime, endDateTime);
        double days = duration.toMillis() / (1000.0 * 60 * 60 * 24);

        return round(days, 2);
    }

    /* -----------------------------------------------------------
       Wells retrieval
     ----------------------------------------------------------- */

    private List<WellDetails> getWellsByPad(String organizationId, Job job) {

        if (job == null || job.getPad() == null) {
            return Collections.emptyList();
        }

        Query padQuery = new Query(
                Criteria.where("organizationId").is(organizationId)
                        .and("name").is(job.getPad())
        );
        Pad pad = mongoTemplate.findOne(padQuery, Pad.class, "pads");
        if (pad == null) {
            return Collections.emptyList();
        }

        Query wellQuery = new Query(
                Criteria.where("organizationId").is(organizationId)
                        .and("padId").is(pad.getId())
        );
        List<Well> wells = mongoTemplate.find(wellQuery, Well.class, "wells");

        if (wells == null || wells.isEmpty()) return Collections.emptyList();

        return wells.stream()
                .map(w -> new WellDetails(w.getId(), w.getName(), w.getApi(), w.getTotalStages()))
                .collect(Collectors.toList());
    }

    /* -----------------------------------------------------------
       Daily averages
     ----------------------------------------------------------- */

    private DailyAverages calculateDailyAverages(String organizationId, String organizationName, List<ActivityLogEntry> activityLogEntries, List<WellDetails> wells, String jobId) {
        DailyAverages dailyAverages = new DailyAverages();

        long days = 0;
        if (activityLogEntries != null && !activityLogEntries.isEmpty()) {
            days = activityLogEntries.stream().map(ActivityLogEntry::getDay).collect(Collectors.toSet()).size();
        }

        dailyAverages.setOverallAverageStages(calculateOverallAverageStagesPerDay(activityLogEntries, wells, days));
        dailyAverages.setAverageProppantLbs(calculateAverageProppantLbs(organizationId, jobId, wells, days));
        dailyAverages.setAveragePumpingTimeHrs(calculateAveragePumpTime(activityLogEntries, wells, days));
        dailyAverages.setAverageTotalNPTHrs(calculateAverageTotalNPTHrs(activityLogEntries, wells, days));
        dailyAverages.setAverageServiceNPTHrs(calcuateAverageServiceNPTHrs(activityLogEntries, wells, days, organizationName));
        dailyAverages.setFracEfficiencyPercentage(calculateFracEfficiency(activityLogEntries, wells));
        return dailyAverages;
    }

    private double calculateAverageProppantLbs(String organizationId, String jobId, List<WellDetails> wells, long days) {

        if (days == 0 || wells == null || wells.isEmpty()) {
            return 0.0;
        }

        Query query = new Query();
        query.addCriteria(Criteria.where("organizationId").is(organizationId));
        query.addCriteria(Criteria.where("jobId").is(jobId));

        List<ProppantDeliveryEntryDto> deliveryEntries =
                mongoTemplate.find(query, ProppantDeliveryEntryDto.class, "proppant-delivery-entries");

        if (deliveryEntries == null || deliveryEntries.isEmpty()) {
            return 0.0;
        }

        Map<String, Double> wellToTotalPumped = new HashMap<>();

        for (ProppantDeliveryEntryDto entry : deliveryEntries) {
            if (entry.getUsedIn() == null) continue;
            for (IMaterialUsed used : entry.getUsedIn()) {
                double pumpedLbs = used.getSubmittedAmount() != null ? used.getSubmittedAmount() : 0.0;
                wellToTotalPumped.merge(used.getWell(), pumpedLbs, Double::sum);
            }
        }

        double sumOfWellAverages = wellToTotalPumped.values().stream()
                .mapToDouble(totalPumped -> totalPumped / days)
                .sum();

        double finalAvg = sumOfWellAverages / wells.size();

        return round(finalAvg, 2);
    }

    private double calculateFracEfficiency(List<ActivityLogEntry> activityLogEntries, List<WellDetails> wells) {

        if (activityLogEntries == null || activityLogEntries.isEmpty() ||
                wells == null || wells.isEmpty()) {
            return 0.0;
        }

        List<ActivityLogEntry> pumpEntries = activityLogEntries.stream()
                .filter(l -> l.getEventOrNptCode() != null &&
                        l.getEventOrNptCode().equalsIgnoreCase("Pump Time"))
                .collect(Collectors.toList());

        List<ActivityLogEntry> nptEntries = activityLogEntries.stream()
                .filter(l -> l.getOpsActivity() != null &&
                        l.getOpsActivity().toUpperCase().contains("NPT"))
                .collect(Collectors.toList());

        Map<String, List<ActivityLogEntry>> pumpByWell = pumpEntries.stream()
                .filter(l -> l.getWell() != null)
                .collect(Collectors.groupingBy(ActivityLogEntry::getWell));

        Map<String, List<ActivityLogEntry>> nptByWell = nptEntries.stream()
                .filter(l -> l.getWell() != null)
                .collect(Collectors.groupingBy(ActivityLogEntry::getWell));

        double totalEfficiency = 0.0;
        int countedWells = 0;

        for (WellDetails well : wells) {
            String wellName = well.getName();

            long pumpMs = pumpByWell.getOrDefault(wellName, List.of())
                    .stream()
                    .mapToLong(ActivityLogEntry::getMillisecondsSpan)
                    .sum();

            long nptMs = nptByWell.getOrDefault(wellName, List.of())
                    .stream()
                    .mapToLong(ActivityLogEntry::getMillisecondsSpan)
                    .sum();

            if (pumpMs + nptMs == 0) {
                continue;
            }

            double wellEfficiency = (double) pumpMs / (pumpMs + nptMs);

            totalEfficiency += wellEfficiency;
            countedWells++;
        }

        if (countedWells == 0) {
            return 0.0;
        }
        double fracEfficiencyPercentage = (totalEfficiency / countedWells) * 100;
        return round(fracEfficiencyPercentage, 2);
    }

    String calcuateAverageServiceNPTHrs(List<ActivityLogEntry> activityLogEntries,
                                        List<WellDetails> wells,
                                        long days,
                                        String organizationName) {

        if (activityLogEntries == null || activityLogEntries.isEmpty() ||
                days == 0 || organizationName == null) {
            return "00:00";
        }

        String targetActivity = organizationName + " NPT";

        List<ActivityLogEntry> serviceNptEntries = activityLogEntries.stream()
                .filter(l -> l.getOpsActivity() != null &&
                        l.getOpsActivity().equalsIgnoreCase(targetActivity))
                .collect(Collectors.toList());

        if (serviceNptEntries.isEmpty()) {
            return "00:00";
        }

        Map<String, List<ActivityLogEntry>> logsByWell = serviceNptEntries.stream()
                .filter(l -> l.getWell() != null)
                .collect(Collectors.groupingBy(ActivityLogEntry::getWell));

        long totalAvgMillisAcrossWells = 0L;

        for (Map.Entry<String, List<ActivityLogEntry>> entry : logsByWell.entrySet()) {
            long totalMillis = entry.getValue().stream()
                    .mapToLong(ActivityLogEntry::getMillisecondsSpan)
                    .sum();
            long avgMillisPerDay = totalMillis / days;
            totalAvgMillisAcrossWells += avgMillisPerDay;
        }

        long overallAvgMillis = totalAvgMillisAcrossWells / wells.size();

        long totalMinutes = overallAvgMillis / (1000 * 60);
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        return String.format("%02d:%02d", hours, minutes);
    }

    private String calculateAverageTotalNPTHrs(List<ActivityLogEntry> activityLogEntries,
                                               List<WellDetails> wells,
                                               long days) {

        if (activityLogEntries == null || activityLogEntries.isEmpty() || days == 0) {
            return "00:00";
        }

        List<ActivityLogEntry> nptEntries = activityLogEntries.stream()
                .filter(l -> l.getOpsActivity() != null &&
                        l.getOpsActivity().toUpperCase().contains("NPT"))
                .collect(Collectors.toList());

        if (nptEntries.isEmpty()) {
            return "00:00";
        }

        Map<String, List<ActivityLogEntry>> logsByWell = nptEntries.stream()
                .filter(l -> l.getWell() != null)
                .collect(Collectors.groupingBy(ActivityLogEntry::getWell));

        long totalAvgMillisAcrossWells = 0L;

        for (Map.Entry<String, List<ActivityLogEntry>> entry : logsByWell.entrySet()) {
            long totalMillis = entry.getValue().stream()
                    .mapToLong(ActivityLogEntry::getMillisecondsSpan)
                    .sum();
            long avgMillisPerDay = totalMillis / days;
            totalAvgMillisAcrossWells += avgMillisPerDay;
        }

        long overallAvgMillis = totalAvgMillisAcrossWells / wells.size();

        long totalMinutes = overallAvgMillis / (1000 * 60);
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        return String.format("%02d:%02d", hours, minutes);
    }

    private String calculateAveragePumpTime(List<ActivityLogEntry> activityLogEntries, List<WellDetails> wells, long days) {
        if (activityLogEntries == null || activityLogEntries.isEmpty() || days == 0) {
            return "00:00";
        }

        List<ActivityLogEntry> pumpTimeEntries = activityLogEntries.stream()
                .filter(l -> l.getEventOrNptCode() != null && l.getEventOrNptCode().equalsIgnoreCase("Pump Time"))
                .collect(Collectors.toList());

        if (pumpTimeEntries.isEmpty()) {
            return "00:00";
        }

        Map<String, List<ActivityLogEntry>> logsByWell = pumpTimeEntries.stream()
                .filter(l -> l.getWell() != null)
                .collect(Collectors.groupingBy(ActivityLogEntry::getWell));

        long totalAvgMillisAcrossWells = 0L;

        for (Map.Entry<String, List<ActivityLogEntry>> entry : logsByWell.entrySet()) {
            long totalMillis = entry.getValue().stream()
                    .mapToLong(ActivityLogEntry::getMillisecondsSpan)
                    .sum();
            long avgMillisPerDay = totalMillis / days;
            totalAvgMillisAcrossWells += avgMillisPerDay;
        }

        long overallAvgMillis = totalAvgMillisAcrossWells / wells.size();

        long totalMinutes = overallAvgMillis / (1000 * 60);
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        return String.format("%02d:%02d", hours, minutes);
    }

    private double calculateOverallAverageStagesPerDay(List<ActivityLogEntry> activityLogEntries, List<WellDetails> wells, long days) {
        double totalAveragePerWell = 0.0;

        if (days == 0 || activityLogEntries == null || activityLogEntries.isEmpty() || wells == null || wells.isEmpty()) {
            return totalAveragePerWell;
        }

        Map<String, List<ActivityLogEntry>> logsByWell = activityLogEntries.stream()
                .filter(l -> l.getWell() != null && Boolean.TRUE.equals(l.getComplete()))
                .collect(Collectors.groupingBy(ActivityLogEntry::getWell));

        for (Map.Entry<String, List<ActivityLogEntry>> entry : logsByWell.entrySet()) {
            List<ActivityLogEntry> activityLogEntryList = entry.getValue();
            totalAveragePerWell += (double) activityLogEntryList.size() / days;
        }

        return round(totalAveragePerWell / wells.size(), 2);
    }

    public ResponseEntity<?> getWellCompletionInformation(HttpServletRequest request, String jobId, String wellId) {
        try {
            // Call 1: Fetch Job (Secure)
            Optional<Job> jobOptional = getJobById(request, jobId);

            if (jobOptional.isEmpty()) {
                return buildErrorResponse("NOT_FOUND", "Job not found", HttpStatus.NOT_FOUND);
            }
            Job job = jobOptional.get();

            // Assign organizationId from the Job
            String organizationId = job.getOrganizationId();

            WellCompletionInformation info = new WellCompletionInformation();

            // OPTIMIZATION: Pass the 'job' object directly, avoiding a 2nd DB call
            Well matchedWell = getWellFromJob(job, wellId);

            info.setCleanTotals(getCleanTotals(organizationId, jobId, wellId));
            info.setProppantPumped(getProppantPumpedInfo(organizationId, jobId, wellId, matchedWell));
            info.setSummaryChart(getSummaryChartInfo(organizationId, jobId, wellId, matchedWell));
            info.setPumpPerformance(getPumpPerformanceInfo(organizationId, jobId, wellId, matchedWell));

            return ResponseEntity.ok(info);

        } catch (Exception e) {
            log.error("Unexpected error while fetching well completion information", e);
            return buildErrorResponse("UNEXPECTED_ERROR", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private double actualCleanVolumeForWell(String organizationId, String jobId, String wellId){
        double actualCleanVolume = 0;
        Query chemicalStageQuery = new Query();
        chemicalStageQuery.addCriteria(Criteria.where("organizationId").is(organizationId));
        chemicalStageQuery.addCriteria(Criteria.where("jobId").is(jobId));
        chemicalStageQuery.addCriteria(Criteria.where("wellId").is(wellId));

        List<ChemicalStage> chemicalStages =
                mongoTemplate.find(chemicalStageQuery, ChemicalStage.class, "chemical-stages");

        if(!ObjectUtils.isEmpty(chemicalStages)) {
            for(ChemicalStage chemicalStage : chemicalStages) {
                actualCleanVolume += round((chemicalStage.getCleanTotal()),2);
            }
        }
        return actualCleanVolume;
    }

    private double designedCleanVolumeForWell(String organizationId, String jobId, String wellId){
        Query query = new Query();
        query.addCriteria(Criteria.where("jobId").is(jobId));
        query.addCriteria(Criteria.where("wellId").is(wellId));

        List<PumpSchedule> pumpSchedules =
                mongoTemplate.find(query, PumpSchedule.class, "pump-schedules");
        double designedCleanVolume = 0;
        if(!ObjectUtils.isEmpty(pumpSchedules)) {
            for(PumpSchedule pumpSchedule : pumpSchedules) {
                designedCleanVolume += round((pumpSchedule.getCleanVol()),2);
            }
        }
        return designedCleanVolume;
    }

    // OPTIMIZED METHOD: Uses the existing Job object
    private Well getWellFromJob(Job job, String wellId){
        if (job == null || job.getWells() == null) {
            return null;
        }

        return job.getWells()
                .stream()
                .filter(w -> w.getId().equals(wellId))
                .findFirst()
                .orElse(null);
    }

    private double proppantUsedByWell(String organizationId, String jobId, Well well){
        double amount = 0;
        Query query = new Query();
        query.addCriteria(Criteria.where("organizationId").is(organizationId));
        query.addCriteria(Criteria.where("jobId").is(jobId));
        query.addCriteria(
                Criteria.where("usedIn").elemMatch(
                        Criteria.where("well").is(well.getName())
                )
        );

        List<ProppantDeliveryEntryDto> deliveryEntries =
                mongoTemplate.find(query, ProppantDeliveryEntryDto.class, "proppant-delivery-entries");
        for (ProppantDeliveryEntryDto each : deliveryEntries) {
            if (each.getUsedIn() == null) continue;
            for (IMaterialUsed used : each.getUsedIn()) {
                if(used.getWell().equals(well.getName())) {
                    amount += used.getSubmittedAmount();
                }
            }
        }
        return round(amount, 2);
    }

    private CleanTotals getCleanTotals(String organizationId, String jobId, String wellId) {
        CleanTotals cleanTotals = new CleanTotals();

        cleanTotals.setDesignedCleanVolume(
                round(designedCleanVolumeForWell(organizationId, jobId, wellId), 2)
        );

        cleanTotals.setActualCleanVolume(
                round(actualCleanVolumeForWell(organizationId, jobId, wellId), 2)
        );

        return cleanTotals;
    }

    private ProppantPumped getProppantPumpedInfo(String organizationId, String jobId, String wellId, Well matchedWell) {
        ProppantPumped proppantPumped = new ProppantPumped();
        double designed = 0;
        double actual = 0;

        Query query = new Query();
        query.addCriteria(Criteria.where("organizationId").is(organizationId));
        query.addCriteria(Criteria.where("jobId").is(jobId));
        query.addCriteria(Criteria.where("wellId").is(matchedWell.getId()));
        List<ProppantStage> proppantStages =
                mongoTemplate.find(query, ProppantStage.class, "proppant-stages");

        int size = proppantStages == null ? 0 :
                proppantStages.stream()
                        .map(ProppantStage::getStage)
                        .collect(Collectors.toSet())
                        .size();

        if (!ObjectUtils.isEmpty(matchedWell)) {
            if (!ObjectUtils.isEmpty(matchedWell.getProppants())) {
                for (Proppant proppant : matchedWell.getProppants()) {
                    Float proppantVolumePerStage = proppant.getVolumePerStage();
                    if(proppantVolumePerStage == null) { proppantVolumePerStage = 0.0f; }
                    designed += proppantVolumePerStage;
                }
            }
            actual = proppantUsedByWell(organizationId, jobId, matchedWell);
        }
        proppantPumped.setDesignedProppantPumped(round(designed * size, 2));
        proppantPumped.setActualProppantPumped(round(actual, 2));

        return proppantPumped;
    }

    private SummaryChart getSummaryChartInfo(String organizationId, String jobId, String wellId, Well matchedWell) {

        SummaryChart summary = new SummaryChart();
        TotalPropAndClean total = new TotalPropAndClean();

        Map<String, ChemicalValue> chemicalValues = new HashMap<>();

        if (!ObjectUtils.isEmpty(matchedWell)) {

            Query query = new Query();
            query.addCriteria(Criteria.where("organizationId").is(organizationId));
            query.addCriteria(Criteria.where("jobId").is(jobId));
            query.addCriteria(Criteria.where("usedIn")
                    .elemMatch(Criteria.where("well").is(matchedWell.getName())));

            List<ChemicalDeliveryEntry> chemicalEntries =
                    mongoTemplate.find(query, ChemicalDeliveryEntry.class, "chemical-delivery-entries");

            List<ChemicalDeliveryEntry> validEntries = new ArrayList<>();

            for (ChemicalDeliveryEntry entry : chemicalEntries) {
                if (!(entry.getTransferredFromJobId() != null && !entry.getStatus().equals("accepted"))) {
                    validEntries.add(entry);
                }
            }

            for (ChemicalDeliveryEntry each : validEntries) {
                double amount = 0;
                String chemicalName = each.getChemical();
                String uom = each.getUom();

                for (ChemicalUsed used : each.getUsedIn()) {
                    if(used.getWell().equals(matchedWell.getName())) {
                        amount += used.getSubmittedAmount();
                    }
                }

                double roundedAmount = round(amount, 2);

                // Merge values if chemical already exists
                if (chemicalValues.containsKey(chemicalName))
                {
                    ChemicalValue existing = chemicalValues.get(chemicalName);
                    existing.setValue(existing.getValue() + roundedAmount);
                }
                else { chemicalValues.put(chemicalName, new ChemicalValue(roundedAmount, uom)); }
            }
        }
        summary.setChemicalValues(chemicalValues);

        total.setCleanVolume(
                round((actualCleanVolumeForWell(organizationId, jobId, wellId) * 42), 2)
        );

        total.setProppantPumped(
                round(proppantUsedByWell(organizationId, jobId, matchedWell), 2)
        );

        summary.setTotalPropAndClean(total);
        return summary;
    }

    private PumpPerformance getPumpPerformanceInfo(String organizationId, String jobId, String wellId, Well matchedWell) {

        PumpPerformance pumpPerformance = new PumpPerformance();

        Query ChemicalStageQuery = new Query();
        ChemicalStageQuery.addCriteria(Criteria.where("organizationId").is(organizationId));
        ChemicalStageQuery.addCriteria(Criteria.where("wellId").is(wellId));
        ChemicalStageQuery.addCriteria(Criteria.where("jobId").is(jobId));
        List<ChemicalStage> chemicalStages = mongoTemplate.find(ChemicalStageQuery, ChemicalStage.class, "chemical-stages");

        // Fetch well info from wells collection (required to get FracPro ID)
        Query wellQuery = new Query();
        wellQuery.addCriteria(Criteria.where("organizationId").is(organizationId));
        wellQuery.addCriteria(Criteria.where("_id").is(wellId));
        Well wellFromDB = mongoTemplate.findOne(wellQuery, Well.class, "wells");

        // Fetch pump time
        Query activityLogQuery = new Query();
        activityLogQuery.addCriteria(Criteria.where("organizationId").is(organizationId));
        activityLogQuery.addCriteria(Criteria.where("jobId").is(jobId));
        activityLogQuery.addCriteria(Criteria.where("well").is(matchedWell != null ? matchedWell.getName() : ""));
        activityLogQuery.addCriteria(Criteria.where("eventOrNptCode").is("Pump Time"));

        List<ActivityLogEntry> logs =
                mongoTemplate.find(activityLogQuery, ActivityLogEntry.class, "activity-log-entries");

        Map<Float, List<ActivityLogEntry>> activityLogMap = logs.stream()
                .collect(Collectors.groupingBy(ActivityLogEntry::getStage));

        int fracproId = wellFromDB != null ? wellFromDB.getFracproId() : 0;

        Client client = ClientBuilder.newClient();
        String token = aiCallsService.retrieveFracproAuthToken();
        List<FracProTreatmentId> treatmentIds =
                aiCallsService.getFracProTreatmentsListForCurWellDirect(fracproId, token, client);

        Map<Integer, FracProTreatment> map = new HashMap<>();

        if (!ObjectUtils.isEmpty(treatmentIds)) {
            map = aiCallsService.getAllFracproTreatmentsDirect(fracproId, treatmentIds, token);
        }

        List<Float> avgPresList = new ArrayList<>();
        List<Float> maxPresList = new ArrayList<>();
        List<Float> avgRateList = new ArrayList<>();
        List<Float> maxRateList = new ArrayList<>();
        List<Float> pumptimeList = new ArrayList<>();

        for (ChemicalStage chemicalStage : chemicalStages) {
            Integer stage = chemicalStage != null && chemicalStage.getStage() != null
                    ? chemicalStage.getStage().intValue()
                    : null;

            FracProTreatment treatment = stage != null ? map.get(stage) : null;
            List<ActivityLogEntry> activityLogEntries = stage != null ? activityLogMap.get(chemicalStage.getStage()) : null;

            avgPresList.add(
                    treatment != null && treatment.getAveragePres() != null
                            ? treatment.getAveragePres()
                            : 0.0f
            );

            maxPresList.add(
                    treatment != null && treatment.getMaxPres() != null
                            ? treatment.getMaxPres()
                            : 0.0f
            );

            avgRateList.add(
                    treatment != null && treatment.getAvgSlurryReturnRate() != null
                            ? treatment.getAvgSlurryReturnRate()
                            : 0.0f
            );

            maxRateList.add(
                    treatment != null && treatment.getMaxFluidRate() != null
                            ? treatment.getMaxFluidRate()
                            : 0.0f
            );

            Float pumpTime = (activityLogEntries != null)
                    ? ActivityLogUtil.getTotalPumpTimeInMins(activityLogEntries)
                    : 0.0f;

            pumptimeList.add(pumpTime);
        }

        pumpPerformance.setPumpTime(pumptimeList);
        pumpPerformance.setAveragePressure(avgPresList);
        pumpPerformance.setMaxPressure(maxPresList);
        pumpPerformance.setAverageRate(avgRateList);
        pumpPerformance.setMaxRate(maxRateList);

        return pumpPerformance;
    }

    public ResponseEntity<?> getCleanPerStage(HttpServletRequest request, String jobId, String wellId) {
        try {
            // Call 1: Fetch Job
            Optional<Job> jobOptional = getJobById(request, jobId);
            if (jobOptional.isEmpty()) {
                return buildErrorResponse("NOT_FOUND", "Job not found", HttpStatus.NOT_FOUND);
            }
            Job job = jobOptional.get();
            String organizationId = job.getOrganizationId();

            List<CleanPerStageResponse> responses = new ArrayList<>();

            Query query = new Query();
            query.addCriteria(Criteria.where("organizationId").is(organizationId));
            query.addCriteria(Criteria.where("jobId").is(jobId));
            query.addCriteria(Criteria.where("wellId").is(wellId));

            List<ChemicalStage> stages =
                    mongoTemplate.find(query, ChemicalStage.class, "chemical-stages");

            if (stages == null || stages.isEmpty()) {
                return ResponseEntity.ok(responses);
            }

            Map<Float, Float> stageToCleanMap = stages.stream()
                    .collect(Collectors.toMap(
                            ChemicalStage::getStage,
                            ChemicalStage::getCleanTotal,
                            (a, b) -> b
                    ));

            Float maxStage = stages.stream()
                    .map(ChemicalStage::getStage)
                    .max(Float::compare)
                    .orElse(0.0f);

            List<Float> allStages = new ArrayList<>();
            for (float s = 1.0f; s <= maxStage; s += 1.0f) {
                allStages.add(s);
            }

            for (Float stage : allStages) {
                Float cleanTotal = stageToCleanMap.getOrDefault(stage, 0.0f);

                CleanPerStageResponse resp = new CleanPerStageResponse();
                resp.setStageNumber(stage);
                resp.setTotalCleanBbls(cleanTotal);

                responses.add(resp);
            }

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            log.error("Unexpected error while fetching total clean per stage", e);
            return buildErrorResponse("UNEXPECTED_ERROR", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> getAveragePressureAndRate(HttpServletRequest request, String jobId, String wellId) {
        try{
            // Call 1: Fetch Job
            Optional<Job> jobOptional = getJobById(request, jobId);
            if (jobOptional.isEmpty()) {
                return buildErrorResponse("NOT_FOUND", "Job not found", HttpStatus.NOT_FOUND);
            }
            Job job = jobOptional.get();
            String organizationId = job.getOrganizationId();

            List<AveragePressureAndRateResponse> averagePressureAndRateResponses = new ArrayList<>();
            Query wellQuery = new Query();
            wellQuery.addCriteria(Criteria.where("organizationId").is(organizationId));
            wellQuery.addCriteria(Criteria.where("_id").is(wellId));
            Well wellFromDB = mongoTemplate.findOne(wellQuery, Well.class, "wells");

            Map<String, String> wellMap = new HashMap<>();
            wellMap.put(wellFromDB.getId(), wellFromDB.getName());

            Query emailQuery = new Query();
            emailQuery.addCriteria(Criteria.where("organizationId").is(organizationId));
            emailQuery.addCriteria(Criteria.where("jobId").is(jobId));
            emailQuery.addCriteria(Criteria.where("type").is("END_STAGE"));
            emailQuery.addCriteria(
                    new Criteria().orOperator(
                            Criteria.where("well").is(wellId),
                            Criteria.where("well").is(wellFromDB.getName())
                    )
            );
            List<EndStageEmailPayload> endStageEmails = mongoTemplate.find(emailQuery, EndStageEmailPayload.class, "emails");

            Map<String, EndStageEmailPayload> latestEmailsMap = new HashMap<>();
            if (endStageEmails != null && !endStageEmails.isEmpty()) {
                for (EndStageEmailPayload email : endStageEmails) {
                    String key;
                    String wellName = email.getWell();
                    if (!ObjectUtils.isEmpty(wellMap.get(wellName))) {
                        key = wellMap.get(wellName) + "-" + email.getStage();
                    } else {
                        key = wellName + "-" + email.getStage();
                    }
                    EndStageEmailPayload existing = latestEmailsMap.get(key);
                    if (existing == null || email.getCreated() > existing.getCreated()) {
                        latestEmailsMap.put(key, email);
                    }
                }
            }

            List<EndStageEmailPayload> latestEndStageEmails = new ArrayList<>(latestEmailsMap.values());

            for(EndStageEmailPayload endStageEmailPayload : latestEndStageEmails){
                AveragePressureAndRateResponse averagePressureAndRateResponse = new AveragePressureAndRateResponse();

                averagePressureAndRateResponse.setStageNumber(Float.valueOf(endStageEmailPayload.getStage()));
                averagePressureAndRateResponse.setAveragePressure(endStageEmailPayload.getAveragePressure());
                averagePressureAndRateResponse.setAverageRate(endStageEmailPayload.getAverageRate());

                averagePressureAndRateResponses.add(averagePressureAndRateResponse);
            }

            averagePressureAndRateResponses = averagePressureAndRateResponses.stream()
                    .sorted(Comparator.comparing(AveragePressureAndRateResponse::getStageNumber))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(averagePressureAndRateResponses);

        } catch (Exception e) {
            log.error("Unexpected error while fetching Average pressure and rate", e);
            return buildErrorResponse("UNEXPECTED_ERROR", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public ResponseEntity<?> getFinalISIPAndFG(HttpServletRequest request, String jobId, String wellId) {
        try{
            // Call 1: Fetch Job
            Optional<Job> jobOptional = getJobById(request, jobId);
            if (jobOptional.isEmpty()) {
                return buildErrorResponse("NOT_FOUND", "Job not found", HttpStatus.NOT_FOUND);
            }
            Job job = jobOptional.get();
            String organizationId = job.getOrganizationId();

            List<FinalISIPAndFGResponse> finalISIPAndFGResponses = new ArrayList<>();

            Query wellQuery = new Query();
            wellQuery.addCriteria(Criteria.where("organizationId").is(organizationId));
            wellQuery.addCriteria(Criteria.where("_id").is(wellId));
            Well wellFromDB = mongoTemplate.findOne(wellQuery, Well.class, "wells");

            int fracproId = wellFromDB != null ? wellFromDB.getFracproId() : 0;

            Client client = ClientBuilder.newClient();
            String token = aiCallsService.retrieveFracproAuthToken();
            List<FracProTreatmentId> treatmentIds =
                    aiCallsService.getFracProTreatmentsListForCurWellDirect(fracproId, token, client);

            Map<Integer, FracProTreatment> map = new HashMap<>();

            if (!ObjectUtils.isEmpty(treatmentIds)) {
                map = aiCallsService.getAllFracproTreatmentsDirect(fracproId, treatmentIds, token);
            }

            Query ChemicalStageQuery = new Query();
            ChemicalStageQuery.addCriteria(Criteria.where("organizationId").is(organizationId));
            ChemicalStageQuery.addCriteria(Criteria.where("wellId").is(wellId));
            ChemicalStageQuery.addCriteria(Criteria.where("jobId").is(jobId));
            List<ChemicalStage> chemicalStages = mongoTemplate.find(ChemicalStageQuery, ChemicalStage.class, "chemical-stages");

            for (ChemicalStage chemicalStage : chemicalStages) {
                FracProTreatment treatment = map.get(chemicalStage.getStage().intValue());
                FinalISIPAndFGResponse finalISIPAndFGResponse = new FinalISIPAndFGResponse();
                finalISIPAndFGResponse.setStageNumber(chemicalStage.getStage());
                if(treatment != null){
                    finalISIPAndFGResponse.setFinalISIP(round(treatment.getInitialShutinPres(), 2));
                    finalISIPAndFGResponse.setFracGradient(round(treatment.getFractureGradient(), 2));
                } else{
                    finalISIPAndFGResponse.setFinalISIP(0.0f);
                    finalISIPAndFGResponse.setFracGradient(0.0f);
                }
                finalISIPAndFGResponses.add(finalISIPAndFGResponse);
            }

            finalISIPAndFGResponses = finalISIPAndFGResponses.stream()
                    .sorted(Comparator.comparing(FinalISIPAndFGResponse::getStageNumber))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(finalISIPAndFGResponses);

        } catch (Exception e) {
            log.error("Unexpected error while fetching final ISIP and FG", e);
            return buildErrorResponse("UNEXPECTED_ERROR", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public ResponseEntity<?> getAverageVsMax(HttpServletRequest request, String jobId, String wellId) {
        try{
            // Call 1: Fetch Job
            Optional<Job> jobOptional = getJobById(request, jobId);
            if (jobOptional.isEmpty()) {
                return buildErrorResponse("NOT_FOUND", "Job not found", HttpStatus.NOT_FOUND);
            }
            Job job = jobOptional.get();
            String organizationId = job.getOrganizationId();

            List<AverageVsMaxResponse> averageVsMaxResponses = new ArrayList<>();
            Query wellQuery = new Query();
            wellQuery.addCriteria(Criteria.where("organizationId").is(organizationId));
            wellQuery.addCriteria(Criteria.where("_id").is(wellId));
            Well wellFromDB = mongoTemplate.findOne(wellQuery, Well.class, "wells");

            Map<String, String> wellMap = new HashMap<>();
            wellMap.put(wellFromDB.getId(), wellFromDB.getName());

            Query emailQuery = new Query();
            emailQuery.addCriteria(Criteria.where("organizationId").is(organizationId));
            emailQuery.addCriteria(Criteria.where("jobId").is(jobId));
            emailQuery.addCriteria(Criteria.where("type").is("END_STAGE"));
            emailQuery.addCriteria(
                    new Criteria().orOperator(
                            Criteria.where("well").is(wellId),
                            Criteria.where("well").is(wellFromDB.getName())
                    )
            );
            List<EndStageEmailPayload> endStageEmails = mongoTemplate.find(emailQuery, EndStageEmailPayload.class, "emails");

            Query ChemicalStageQuery = new Query();
            ChemicalStageQuery.addCriteria(Criteria.where("organizationId").is(organizationId));
            ChemicalStageQuery.addCriteria(Criteria.where("wellId").is(wellId));
            ChemicalStageQuery.addCriteria(Criteria.where("jobId").is(jobId));
            List<ChemicalStage> chemicalStages = mongoTemplate.find(ChemicalStageQuery, ChemicalStage.class, "chemical-stages");

            Map<String, EndStageEmailPayload> latestEmailsMap = new HashMap<>();
            if (endStageEmails != null && !endStageEmails.isEmpty()) {
                for (EndStageEmailPayload email : endStageEmails) {
                    String key;
                    String wellName = email.getWell();
                    if (!ObjectUtils.isEmpty(wellMap.get(wellName))) {
                        key = wellMap.get(wellName) + "-" + email.getStage();
                    } else {
                        key = wellName + "-" + email.getStage();
                    }
                    EndStageEmailPayload existing = latestEmailsMap.get(key);
                    if (existing == null || email.getCreated() > existing.getCreated()) {
                        latestEmailsMap.put(key, email);
                    }
                }
            }

            List<EndStageEmailPayload> latestEndStageEmails = new ArrayList<>(latestEmailsMap.values());

            Map<Float, EndStageEmailPayload> latestEmailStageMap = new HashMap<>();

            for (EndStageEmailPayload email : latestEndStageEmails) {

                Float stage = Float.valueOf(email.getStage());

                EndStageEmailPayload existing = latestEmailStageMap.get(stage);

                if (existing == null || email.getCreated() > existing.getCreated()) {
                    latestEmailStageMap.put(stage, email);
                }
            }

            int fracproId = wellFromDB != null ? wellFromDB.getFracproId() : 0;

            Client client = ClientBuilder.newClient();
            String token = aiCallsService.retrieveFracproAuthToken();
            List<FracProTreatmentId> treatmentIds =
                    aiCallsService.getFracProTreatmentsListForCurWellDirect(fracproId, token, client);

            Map<Integer, FracProTreatment> map = new HashMap<>();

            if (!ObjectUtils.isEmpty(treatmentIds)) {
                map = aiCallsService.getAllFracproTreatmentsDirect(fracproId, treatmentIds, token);
            }

            Map<Float, FracProTreatment> maxValuesMap = new HashMap<>();
            for (ChemicalStage chemicalStage : chemicalStages) {
                Float stage = (chemicalStage != null && chemicalStage.getStage() != null)
                        ? chemicalStage.getStage()
                        : null;

                if (stage == null) {
                    continue;
                }

                FracProTreatment treatment = map.get(chemicalStage.getStage().intValue());

                if (treatment == null) {
                    continue;
                }
                maxValuesMap.put(stage, treatment);
            }

            Float maxStage = 0f;

            if (!latestEmailStageMap.isEmpty()) {
                maxStage = Math.max(maxStage,
                        latestEmailStageMap.keySet().stream().max(Float::compare).orElse(0f));
            }

            if (!maxValuesMap.isEmpty()) {
                maxStage = Math.max(maxStage,
                        maxValuesMap.keySet().stream().max(Float::compare).orElse(0f));
            }

            for (float stage = 1f; stage <= maxStage; stage++) {

                AverageVsMaxResponse resp = new AverageVsMaxResponse();
                resp.setStageNumber(stage);

                // From latest email
                EndStageEmailPayload email = latestEmailStageMap.get(stage);
                if (email != null) {
                    resp.setAveragePressure(email.getAveragePressure());
                    resp.setAverageRate(email.getAverageRate());
                } else {
                    resp.setAveragePressure(0f);
                    resp.setAverageRate(0f);
                }

                // From FracPro
                FracProTreatment t = maxValuesMap.get(stage);
                if (t != null) {
                    resp.setMaxPressure(t.getMaxPres());
                    resp.setMaxRate(t.getMaxFluidRate());
                } else {
                    resp.setMaxPressure(0f);
                    resp.setMaxRate(0f);
                }

                averageVsMaxResponses.add(resp);
            }

            averageVsMaxResponses = averageVsMaxResponses.stream()
                    .sorted(Comparator.comparing(AverageVsMaxResponse::getStageNumber))
                    .collect(Collectors.toList());


            return ResponseEntity.ok(averageVsMaxResponses);

        } catch (Exception e) {
            log.error("Unexpected error while fetching average vs max", e);
            return buildErrorResponse("UNEXPECTED_ERROR", e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}