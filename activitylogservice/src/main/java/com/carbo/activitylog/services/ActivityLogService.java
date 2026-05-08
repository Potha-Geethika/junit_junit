package com.carbo.activitylog.services;

import com.carbo.activitylog.controllers.SyncController;
import com.carbo.activitylog.exception.ErrorException;
import com.carbo.activitylog.model.*;
import com.carbo.activitylog.model.error.Success;
import com.carbo.activitylog.repository.*;
import com.carbo.activitylog.utils.Constants;
import com.carbo.activitylog.utils.ErrorConstants;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;
import com.carbo.activitylog.model.error.Error;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.carbo.activitylog.utils.ActivityLogUtil.convertToLocalDateTime;
import static com.carbo.activitylog.utils.CommonUtils.*;
import static com.carbo.activitylog.utils.Constants.*;
import static com.carbo.activitylog.utils.ControllerUtil.*;

@Service
public class ActivityLogService {
    private final ActivityLogMongoDbRepository activityLogRepository;
    private final MongoTemplate mongoTemplate;
    private static final Logger logger = LoggerFactory.getLogger(SyncController.class);
    private final DeletedActivityLogService deletedActivityLogService;
    private final JobMongoDbRepository jobMongoDbRepository;
    private final PumpIssueMongoDbRepository pumpIssueMongoDbRepository;
    private final PendingMaintenanceEntryMongoDbRepository pendingMaintenanceEntryMongoDbRepository;
    private final OrganizationMongoDbRepository organizationMongoDbRepository;


    @Autowired
    public ActivityLogService(ActivityLogMongoDbRepository activityLogRepository, MongoTemplate mongoTemplate, DeletedActivityLogService deletedActivityLogService, JobMongoDbRepository jobMongoDbRepository, PumpIssueMongoDbRepository pumpIssueMongoDbRepository, PendingMaintenanceEntryMongoDbRepository pendingMaintenanceEntryMongoDbRepository,
                              OrganizationMongoDbRepository organizationMongoDbRepository) {
        this.activityLogRepository = activityLogRepository;
        this.mongoTemplate = mongoTemplate;
        this.deletedActivityLogService = deletedActivityLogService;
        this.jobMongoDbRepository = jobMongoDbRepository;
        this.pumpIssueMongoDbRepository = pumpIssueMongoDbRepository;
        this.pendingMaintenanceEntryMongoDbRepository = pendingMaintenanceEntryMongoDbRepository;
        this.organizationMongoDbRepository = organizationMongoDbRepository;
    }

    public List<ActivityLogEntry> getByOrganizationId(String organizationId) {
        return activityLogRepository.findByOrganizationId(organizationId);
    }

    public Optional<ActivityLogEntry> getActivityLog(String activityLogId) {
        return activityLogRepository.findById(activityLogId);
    }

    public List<ActivityLogEntry> findByOrganizationIdAndJobId(String organizationId, String jobId) {
        return activityLogRepository.findByOrganizationIdAndJobId(organizationId, jobId);
    }

    public List<ActivityLogEntry> findByOrganizationIdAndJobIdAndWellAndStage(String organizationId, String jobId, String well, Float stage) {
        return activityLogRepository.findByOrganizationIdAndJobIdAndWellAndStage(organizationId, jobId, well, stage);
    }

    public List<ActivityLogEntry> findByOrganizationIdAndJobIdAndDay(String organizationId, String jobId, Integer day) {
        return activityLogRepository.findByOrganizationIdAndJobIdAndDay(organizationId, jobId, day);
    }

    public List<ActivityLogEntry> findByOrganizationIdAndJobIdAndDayAndWell(String organizationId, String jobId, Integer day, String well) {
        return activityLogRepository.findByOrganizationIdAndJobIdAndDayAndWell(organizationId, jobId, day, well);
    }

    public ActivityLogEntry saveActivityLog(ActivityLogEntry activityLog) {
        LocalDateTime startTime = convertToLocalDateTime(activityLog.getStart());
        LocalDateTime endTime = convertToLocalDateTime(activityLog.getEnd());
        if (startTime.isAfter(endTime)) {
            logger.error(
                    "Invalid activity log time range - JobId: {}, Well: {}, Day: {}, Stage: {}, Start: {}, End: {}",
                    activityLog.getJobId(),
                    activityLog.getWell(),
                    activityLog.getDay(),
                    activityLog.getStage(),
                    activityLog.getStart(),
                    activityLog.getEnd()
            );
            throw new IllegalStateException("Start time cannot be after end time");
        }
        Optional<Job> parentJob = jobMongoDbRepository.findById(activityLog.getJobId());
        if (parentJob.isPresent()) {
            if (!parentJob.get().getOrganizationId().equals(activityLog.getOrganizationId())) {
                throw new IllegalArgumentException(ERROR_ORGANIZATIONID_MISMATCH);
            }
            String jobOperationType = parentJob.get().getOperationsType();
            //Checking whether Job is Simul Frac OPS and is created after 21 July 2025
            Double highestDayHoursToCheck = (jobOperationType.equals(SIMUL_FRAC_OPS) ||jobOperationType.equals(SIMUL_FRAC_OPS_DB))
                    && parentJob.get().getCreated() > 1753036200000.0
                    && parentJob.get().getBankCount() == BankCountEnum.MULTI_BANK ? 48.0 : 24.0;
            //Check if exact duplicate already exists
            Optional<ActivityLogEntry> existing = activityLogRepository
                    .findByOrganizationIdAndJobIdAndDayAndWellAndStartAndEndAndStage(activityLog.getOrganizationId(), activityLog.getJobId(), activityLog.getDay(),
                            activityLog.getWell(), activityLog.getStart(), activityLog.getEnd(), activityLog.getStage());

            if (existing.isPresent()) {
                String errorMsg = String.format(
                        "Duplicate ActivityLogEntry already exists with values: organizationId=%s, jobId=%s, day=%s, well=%s, start=%s, end=%s, stage=%s",
                        activityLog.getOrganizationId(),
                        activityLog.getJobId(),
                        activityLog.getDay(),
                        activityLog.getWell(),
                        activityLog.getStart(),
                        activityLog.getEnd(),
                        activityLog.getStage()
                );
                throw new IllegalArgumentException(errorMsg);
                //throw new IllegalArgumentException("Duplicate ActivityLogEntry already exists.");
            }

            else {
                // Enforce mandatory bank for Simul Frac OPS + Split Activity
                boolean isSimulFracMultiBank = (SIMUL_FRAC_OPS.equals(jobOperationType) ||SIMUL_FRAC_OPS_DB.equals(jobOperationType))
                        && BankCountEnum.MULTI_BANK.equals(parentJob.get().getBankCount());

                if (isSimulFracMultiBank && activityLog.getBank() == null) {
                    throw new IllegalArgumentException("Bank field is mandatory for Simul Frac OPS with Split Activity.");
                }
                List<ActivityLogEntry> jobActivityLogs = findByOrganizationIdAndJobId(activityLog.getOrganizationId(), activityLog.getJobId());
                List<ActivityLogEntry> jobWellDayActivityLogs =  jobActivityLogs.stream()
                        .filter(e->e.getDay().equals(activityLog.getDay())).collect(Collectors.toList());
                if(ObjectUtils.isEmpty(jobWellDayActivityLogs)) {
                    Integer highestDay = jobActivityLogs.stream()
                            .map(ActivityLogEntry::getDay)
                            .filter(Objects::nonNull)
                            .max(Integer::compareTo)
                            .orElse(0);
                    Stream<ActivityLogEntry> allActivityLogEntriesForDay = jobActivityLogs.stream().filter(each -> each.getDay().equals(highestDay));
                    Double highestDayTotalHours = 0.0;
                    boolean isSimulFracJob = (parentJob.get().getOperationsType().equals(SIMUL_FRAC_OPS)||parentJob.get().getOperationsType().equals(SIMUL_FRAC_OPS_DB)) && parentJob.get().getBankCount().equals(BankCountEnum.MULTI_BANK);
                    if (isSimulFracJob){
                        highestDayTotalHours = allActivityLogEntriesForDay.map(each -> each.getMillisecondsSpan()).reduce(0L, (a, b) -> a + b)/3.6e+6;
                    } else {
                        highestDayTotalHours = allActivityLogEntriesForDay
                                // First, filter out invalid logs
                                .filter(each -> each.getStart() != null && each.getEnd() != null)
                                // Use a map with composite key (start + end) to ensure unique time ranges
                                .collect(Collectors.toMap(
                                        each -> each.getStart() + "-" + each.getEnd(), // composite key
                                        each -> each.getMillisecondsSpan(),            // store duration
                                        (existing1, replacement) -> existing1            // if duplicate, keep one
                                ))
                                .values()
                                .stream()
                                .reduce(0L, Long::sum) / 3.6e+6;
                    }
                    logger.info("highestDayTotalHours:{} and highestDayHoursToCheck: {}, highestDay: {}",highestDayTotalHours,highestDayHoursToCheck,highestDay);
                    if((highestDayTotalHours.equals(highestDayHoursToCheck) || ObjectUtils.isEmpty(jobActivityLogs) || activityLog.isFracProConnectNextDay()) && activityLog.getDay().equals(highestDay + 1)) {
                        return activityLogRepository.save(activityLog);
                    }else {
                        throw new IllegalStateException(ERROR_LAST_DAY_24_HOURS_NOT_COMPLETED);
                    }
                } else {
                    return activityLogRepository.save(activityLog);
                }
            }
        } else {
            throw new IllegalStateException(ERROR_JOB_DOES_NOT_EXISTS_FOR_ACTIVITY);
        }
    }


    public void updateActivityLog(ActivityLogEntry activityLog) {
        Optional<Job> parentJob = jobMongoDbRepository.findById(activityLog.getJobId());
        if (parentJob.isPresent()) {
            if (!parentJob.get().getOrganizationId().equals(activityLog.getOrganizationId())) {
                throw new IllegalArgumentException(ERROR_ORGANIZATIONID_MISMATCH);
            } else {
                // Fetch existing log to check bank field
                Optional<ActivityLogEntry> existing = activityLogRepository.findById(activityLog.getId());
                if (existing.isPresent() && existing.get().getBank() != null) {
                    activityLog.setBank(existing.get().getBank());
                }
                activityLogRepository.save(activityLog);
            }
        } else {
            throw new IllegalStateException(ERROR_JOB_DOES_NOT_EXISTS_FOR_ACTIVITY);
        }
    }

    public void deleteActivityLog(String activityLogId) {
        activityLogRepository.deleteById(activityLogId);
    }
    public boolean validateAndStoreActivityLog(HttpServletRequest request, String activityLogId) {
        String userName = getUserName(request);
        ActivityLogEntry activityLogEntry = activityLogRepository.findById(activityLogId)
                .orElseThrow(() -> new ErrorException(
                        Error.builder()
                                .errorCode(ErrorConstants.NO_ACTIVITY_LOG_FOUND_ERROR_CODE)
                                .errorMessage(ErrorConstants.NO_ACTIVITY_LOG_FOUND_ERROR_MESSAGE)
                                .httpStatus(HttpStatus.BAD_REQUEST)
                                .build()));

        DeletedActivityLogEntry deletedActivityLogEntry = new DeletedActivityLogEntry(activityLogEntry);
        deletedActivityLogEntry.setDeletedByAPI(true);
        deletedActivityLogEntry.setDeletedByUser(userName);
        if (ObjectUtils.isEmpty(deletedActivityLogService.saveActivityLog(deletedActivityLogEntry))) {
            throw new ErrorException(
                    Error.builder()
                            .errorCode(ErrorConstants.UNABLE_TO_DELETE_ACTIVITY_LOG_ERROR_CODE)
                            .errorMessage(ErrorConstants.UNABLE_TO_DELETE_ACTIVITY_LOG_ERROR_MESSAGE)
                            .httpStatus(HttpStatus.BAD_REQUEST)
                            .build());
        }
        return true;
    }

    public void update(String id, String organizationId, Map<String, Object> newLogEntry) {
        Query query = new Query();
        query.addCriteria(Criteria.where(Constants.ID).is(id).and(Constants.ORGANISATIONID).is(organizationId));
        Update update = new Update();
        // Fetch existing record to check bank field
        ActivityLogEntry existing = mongoTemplate.findOne(query, ActivityLogEntry.class);

        for (Map.Entry<String, Object> entry : newLogEntry.entrySet()) {
            if (Constants.ID.equals(entry.getKey())) {
                continue;
            }
            // Skip bank if already present in DB
            if ("bank".equals(entry.getKey()) && existing != null && existing.getBank() != null) {
                continue;
            }
            update.set(entry.getKey(), entry.getValue());
        }
        update.set("modified", System.currentTimeMillis());
        mongoTemplate.updateFirst(query, update, ActivityLogEntry.class);
    }

    public MismatchActivityLogsResponse fetchMismatchedActivityLogs(
            String requestedOrganizationId, String startDateTime, String endDateTime, boolean sendEmail) throws IOException {

        OffsetDateTime start;
        OffsetDateTime end;
        ZoneId zoneId = ZoneId.of("UTC");
        try {
            start = OffsetDateTime.parse(startDateTime.trim());
            end = (endDateTime == null) ? OffsetDateTime.now() : OffsetDateTime.parse(endDateTime.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(ERROR_INVALID_DATE_TIME_FORMAT, e);
        }
        Long startEpochMillis = start.toEpochSecond() * 1000;
        Long endEpochMillis = end.toEpochSecond() * 1000;
        MismatchActivityLogsResponse mismatchActivityLogsResponse = new MismatchActivityLogsResponse();
        List<MismatchActivityLogs> mismatchActivityLogsList = new ArrayList<>();
        List<ActivityLogEntry> activityLogEntryList;

        // Updated: 2026-01-15 - Use String#isBlank to avoid deprecated StringUtils.isEmpty.
        if (requestedOrganizationId == null || requestedOrganizationId.isBlank()) {
            activityLogEntryList = activityLogRepository
                    .getSimplifiedActivitiesForMismatchEntriesAndCreatedRange(startEpochMillis, endEpochMillis);
        } else {
            activityLogEntryList = activityLogRepository
                    .getSimplifiedActivitiesByOrganizationIdAndCreatedRange(requestedOrganizationId, startEpochMillis, endEpochMillis);
        }
        List<Job> allSimplifiedJobs = jobMongoDbRepository.getSimplifiedJobForMismatchEntries();
        Map<String, String> jobIdToOrganizationIdMap = allSimplifiedJobs.stream()
                .collect(Collectors.toMap(Job::getId, Job::getOrganizationId));

        for (ActivityLogEntry entry : activityLogEntryList) {
            String jobId = entry.getJobId();
            String activityLogOrgId = entry.getOrganizationId();
            String jobOrgId = jobIdToOrganizationIdMap.get(jobId);

            if (jobOrgId != null && activityLogOrgId != null && !activityLogOrgId.equals(jobOrgId)) {
                MismatchActivityLogs mismatch = new MismatchActivityLogs();
                mismatch.setActivityLogId(entry.getId());
                mismatch.setParentJobId(jobId);
                mismatch.setActivityLogOrganizationId(activityLogOrgId);
                mismatch.setJobOrganizationId(jobOrgId);
                mismatch.setActivityLogCreated(convertEpochMillisToReadableFormat(entry.getCreated(), zoneId));
                mismatchActivityLogsList.add(mismatch);
            }
        }
        mismatchActivityLogsResponse.setMismatchActivityLogsList(mismatchActivityLogsList);
        mismatchActivityLogsResponse.setTotalCount(mismatchActivityLogsList.size());

        // Send email if totalCount > 0
        if (mismatchActivityLogsResponse.getTotalCount() > 0 && sendEmail) {
            sendEmailWithMismatchLogs(mismatchActivityLogsResponse);
        }

        return mismatchActivityLogsResponse;
    }

    public static String convertEpochMillisToReadableFormat(Long epochMillis, ZoneId zoneId) {
        Instant instant = Instant.ofEpochMilli(epochMillis);
        OffsetDateTime dateTime = instant.atZone(zoneId).toOffsetDateTime();
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss O"));
    }

    private void sendEmailWithMismatchLogs(MismatchActivityLogsResponse mismatchActivityLogsResponse) throws IOException {
        EmailPayload emailPayload1 = new EmailPayload();
        emailPayload1.setTo(MISMATCH_ACTIVITY_LOGS_TO_EMAILS);
        emailPayload1.setSubject(MISMATCH_ACTIVITY_LOGS_NOTIFICATION);
        emailPayload1.setBody(formatMismatchLogsEmailBody(mismatchActivityLogsResponse));
        sendEmailForMismatchActivities(emailPayload1);
    }

    private String formatMismatchLogsEmailBody(MismatchActivityLogsResponse mismatchActivityLogsResponse) {
        StringBuilder bodyBuilder = new StringBuilder();
        bodyBuilder.append("Dear Team,<br><br>")
                .append("Please find below the mismatch activity logs generated in past month:<br><br>")
                .append("<table border='1'>")
                .append("<tr><th>Activity Log ID</th><th>Job ID</th><th>Activity Log Org ID</th><th>Job Org ID</th><th>Created Time</th></tr>");
        for (MismatchActivityLogs log : mismatchActivityLogsResponse.getMismatchActivityLogsList()) {
            bodyBuilder.append("<tr>")
                    .append("<td>").append(log.getActivityLogId()).append("</td>")
                    .append("<td>").append(log.getParentJobId()).append("</td>")
                    .append("<td>").append(log.getActivityLogOrganizationId()).append("</td>")
                    .append("<td>").append(log.getJobOrganizationId()).append("</td>")
                    .append("<td>").append(log.getActivityLogCreated()).append("</td>")
                    .append("</tr>");
        }
        bodyBuilder.append("</table><br>")
                .append("Total mismatched logs: ").append(mismatchActivityLogsResponse.getTotalCount());

        return bodyBuilder.toString();
    }

    public String calculateStartDate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneId.of("UTC"));
        OffsetDateTime oneMonthBack = now.minusMonths(1).withDayOfMonth(1);
        return oneMonthBack.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));
    }

    public String calculateEndDate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneId.of("UTC"));
        OffsetDateTime firstOfThisMonth = now.withDayOfMonth(1);
        return firstOfThisMonth.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));
    }

    public void sendEmailForMismatchActivities(EmailPayload emailPayload) throws RuntimeException, IOException {
        String from = MISMATCH_ACTIVITY_LOGS_BY_EMAIL;
        String apiKey = SENDGRID_API_KEY;
        Email fromEmailPayload = new Email(from);
        String subject = emailPayload.getSubject();
        String bodyContent = emailPayload.getBody();
        if (bodyContent.equals("")) {
            bodyContent = " ";
        }
        // Construct a Personalization object to add recipients (to, cc, bcc)
        Personalization personalization = new Personalization();

        // Split the "to" emails and add them to personalization
        List<String> toEmails = Arrays.asList(emailPayload.getTo().split(";"));
        Set<String> toSet = new HashSet<>(toEmails);
        for (String toEmail : toSet) {
            personalization.addTo(new Email(toEmail.trim()));
        }
        Mail mail = new Mail();
        mail.setFrom(fromEmailPayload);
        mail.setSubject(subject);
        mail.addContent(new Content("text/html", bodyContent));
        mail.addPersonalization(personalization);

        // Create a SendGrid object with your API key
        SendGrid sg = new SendGrid(apiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            logger.info("Sending email through SendGrid");
            // Send the email via SendGrid
            Response response = sg.api(request);
            logger.info("Email sent via SendGrid. Status: {}", response.getStatusCode());
            logger.trace("SendGrid response body: {}", response.getBody());
            logger.trace("SendGrid response headers: {}", response.getHeaders());


        } catch (IOException ex) {
            logger.error("Failed to send email via SendGrid due to I/O error", ex);
            throw ex;
        }
    }

    public ResponseEntity copyActivityLogs(ActivityLogCopyPayload copyPayload) {
        List<String> savedPumpIds = new ArrayList<>();
        List<String> savedMaintenanceIds = new ArrayList<>();
        try {
            List<ActivityLogEntry> entries = activityLogRepository.findByJobIdAndDay(copyPayload.getJobId(), copyPayload.getFromDay());

            if (entries.stream().anyMatch(entry -> Boolean.TRUE.equals(entry.getComplete()))) {
                return buildErrorResponse(
                        ErrorConstants.ERROR_WHILE_ACTIVITY_COMPLETE_CODE,
                        ErrorConstants.ERROR_WHILE_ACTIVITY_COMPLETE_MESSAGE,
                        HttpStatus.BAD_REQUEST
                );
            }

            for (int day = copyPayload.getFromDay() + 1; day <= copyPayload.getToDay() + copyPayload.getFromDay(); day++) {
                if (activityLogRepository.existsByJobIdAndDay(copyPayload.getJobId(), day)) {
                    return buildErrorResponse(
                            ErrorConstants.ERROR_WHILE_ACTIVITY_ALREADY_EXIST_CODE,
                            ErrorConstants.ERROR_WHILE_ACTIVITY_ALREADY_EXIST_MESSAGE,
                            HttpStatus.BAD_REQUEST
                    );
                }
            }

            List<ActivityLogEntry> newEntries = new ArrayList<>();
            long currentTimestamp = System.currentTimeMillis();

            for (int day = copyPayload.getFromDay() + 1; day <= copyPayload.getToDay() + copyPayload.getFromDay(); day++) {
                for (ActivityLogEntry entry : entries) {
                    ActivityLogEntry newEntry = new ActivityLogEntry();
                    BeanUtils.copyProperties(entry, newEntry, "id", "created", "modified", "ts");
                    List<String> newPumpIssueIds = createAndSavePumpIssues(entry.getEquipmentIssueId(), currentTimestamp, savedPumpIds, savedMaintenanceIds);
                    newEntry.setEquipmentIssueId(newPumpIssueIds);
                    newEntry.setDay(day);
                    newEntry.setId(UUID.randomUUID().toString());
                    newEntry.setTs(currentTimestamp);
                    newEntries.add(newEntry);
                }
            }
            activityLogRepository.saveAll(newEntries);
            Success response=Success.builder().code(ErrorConstants.COPY_SUCCESSFULLY_CODE).message(ErrorConstants.COPY_SUCCESSFULLY_MESSAGE).build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Manual rollback
            if (!ObjectUtils.isEmpty(savedPumpIds)) {
                pumpIssueMongoDbRepository.deleteByIds(savedPumpIds);
            }
            if (!ObjectUtils.isEmpty(savedMaintenanceIds)) {
                pendingMaintenanceEntryMongoDbRepository.deleteByIds(savedMaintenanceIds);
            }
            return buildErrorResponse(
                    ErrorConstants.ERROR_WHILE_COPY_ACTIVITY_CODE,
                    ErrorConstants.ERROR_WHILE_COPY_ACTIVITY_MESSAGE,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    private List<String> createAndSavePumpIssues(List<String> oldIssueIds, long currentTimestamp,
                                                 List<String> savedPumpIds, List<String> savedMaintenanceIds) {
        if (ObjectUtils.isEmpty(oldIssueIds)) {
            return Collections.emptyList();
        }
        List<PumpIssue> pumpIssues = pumpIssueMongoDbRepository.findByIdIn(oldIssueIds);
        if (ObjectUtils.isEmpty(pumpIssues)) {
            return Collections.emptyList();
        }
        List<String> newPumpIssueIds = new ArrayList<>();
        for (PumpIssue oldIssue : pumpIssues) {
            PumpIssue newPumpIssue = new PumpIssue();
            BeanUtils.copyProperties(oldIssue, newPumpIssue, "id", "created", "modified", "ts");
            newPumpIssue.setId(UUID.randomUUID().toString());
            newPumpIssue.setTs(currentTimestamp);
            pumpIssueMongoDbRepository.save(newPumpIssue);
            savedPumpIds.add(newPumpIssue.getId());
            PendingMaintenanceEntry pendingMaintenanceEntry = buildPendingMaintenanceEntry(oldIssue, newPumpIssue, currentTimestamp);
            pendingMaintenanceEntryMongoDbRepository.save(pendingMaintenanceEntry);
            savedMaintenanceIds.add(pendingMaintenanceEntry.getId());
            newPumpIssueIds.add(newPumpIssue.getId());
        }
        return newPumpIssueIds;
    }
    private PendingMaintenanceEntry buildPendingMaintenanceEntry(PumpIssue oldIssue, PumpIssue newPumpIssue, long currentTimestamp) {
        PendingMaintenanceEntry pendingMaintenanceEntry = new PendingMaintenanceEntry();
        pendingMaintenanceEntry.setJobId(oldIssue.getJobId());
        pendingMaintenanceEntry.setWell(oldIssue.getWell());
        pendingMaintenanceEntry.setStage(oldIssue.getStage());
        pendingMaintenanceEntry.setTs(currentTimestamp);
        pendingMaintenanceEntry.setOrganizationId(oldIssue.getOrganizationId());
        pendingMaintenanceEntry.setPumpIssueId(newPumpIssue.getId());
        pendingMaintenanceEntry.setEquipment(newPumpIssue.getPumpNumber());
        pendingMaintenanceEntry.setIssue(oldIssue.getIssue());
        return pendingMaintenanceEntry;
    }
    private ResponseEntity<Error> buildErrorResponse(String errorCode, String errorMessage, HttpStatus status) {
        Error error = Error.builder()
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .httpStatus(status)
                .build();
        return ResponseEntity.status(status).body(error);
    }

    public boolean validateActivity(HttpServletRequest request, String jobId) {
        String organizationId=getOrganizationId(request);
        return activityLogRepository.existsByJobIdAndOrganizationId(jobId,organizationId);
    }

    public ResponseEntity getPumpTimeHistory(HttpServletRequest request, String jobId, String eventOrNptCode) {
        try {
            // Timezone from header
            ZoneId zoneId = resolveTimeZone(request);

            Optional<Job> optionalJob = jobMongoDbRepository.findById(jobId);
            if(ObjectUtils.isEmpty(optionalJob)){
                Error error = Error.builder().errorCode(ErrorConstants.ERROR_WHILE_GETTING_JOB_CODE).errorMessage(ErrorConstants.ERROR_WHILE_GETTING_JOB_MESSAGE).build();
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }

            // Convert job start date → LocalDate
            LocalDate startDate = Instant.ofEpochMilli(optionalJob.get().getStartDate())
                    .atZone(zoneId)
                    .toLocalDate();

            PumpTimeHistoryResponse pumpTimeHistoryResponse = new PumpTimeHistoryResponse();
            List<ActivityLogEntry> activityLogEntries = activityLogRepository.findByJobIdAndEventOrNptCode(jobId, eventOrNptCode);
            if(ObjectUtils.isEmpty(activityLogEntries)){
                Error error = Error.builder().errorCode(ErrorConstants.ERROR_WHILE_GETTING_ACTIVITY_LOG_ENTRIES_CODE).errorMessage(ErrorConstants.ERROR_WHILE_GETTING_ACTIVITY_LOG_ENTRIES_MESSAGE).build();
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }

            long activityMillis = activityLogEntries.stream()
                    .mapToLong(ActivityLogEntry::getMillisecondsSpan)
                    .sum();

            double activityHours = activityMillis / (1000.0 * 60 * 60);
            pumpTimeHistoryResponse.setPumpTimeHistoryTable(createPumpTimeHistoryTable(activityLogEntries, startDate));
            pumpTimeHistoryResponse.setTotal(round(activityHours, 2));
            return new ResponseEntity<>(pumpTimeHistoryResponse, HttpStatus.OK);
        } catch (Exception e) {
            Error error = Error.builder().errorCode(ErrorConstants.ERROR_WHILE_GETTING_PUMP_TIME_HISTORY_DATA_CODE).errorMessage(ErrorConstants.ERROR_WHILE_GETTING_PUMP_TIME_HISTORY_DATA_MESSAGE).build();
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private List<PumpTimeHistoryTable> createPumpTimeHistoryTable(List<ActivityLogEntry> activityLogEntries, LocalDate startDate) {
        List<PumpTimeHistoryTable> pumpTimeHistoryTableRecords = new ArrayList<>();



        Map<Integer, List<ActivityLogEntry>> activityLogEntryPerDayMap = activityLogEntries.stream()
                .collect(Collectors.groupingBy(ActivityLogEntry::getDay));

        int maxDay = activityLogEntries.stream()
                .mapToInt(ActivityLogEntry::getDay)
                .max()
                .orElse(0);

        for (int day = 1; day <= maxDay; day++) {
            List<ActivityLogEntry> activityLogEntryList = activityLogEntryPerDayMap.get(day);
            LocalDate currentDate = startDate.plusDays(day - 1);
            if(ObjectUtils.isEmpty(activityLogEntryList)){
                continue;
            }
            for (ActivityLogEntry activityLogEntry : activityLogEntryList){
                PumpTimeHistoryTable pumpTimeHistoryTableEntry = new PumpTimeHistoryTable();
                pumpTimeHistoryTableEntry.setDate(currentDate.toString());
                pumpTimeHistoryTableEntry.setWell(activityLogEntry.getWell());
                pumpTimeHistoryTableEntry.setStage(activityLogEntry.getStage());
                pumpTimeHistoryTableEntry.setActivity(activityLogEntry.getOpsActivity());
                pumpTimeHistoryTableEntry.setNptCode(activityLogEntry.getEventOrNptCode());
                String duration = formatMillisToHHmm(activityLogEntry.getMillisecondsSpan());
                pumpTimeHistoryTableEntry.setDuration(duration);
                pumpTimeHistoryTableEntry.setEquipment(activityLogEntry.getEquipment());
                pumpTimeHistoryTableEntry.setIssue(activityLogEntry.getIssue());
                pumpTimeHistoryTableEntry.setOperationNotes(activityLogEntry.getComments());
                pumpTimeHistoryTableRecords.add(pumpTimeHistoryTableEntry);
            }
        }

        return pumpTimeHistoryTableRecords;
    }

    public ResponseEntity getActivityAndNptHistory(HttpServletRequest request, String jobId, String activityOrNptCode) {
        try {
            // Timezone from header
            ZoneId zoneId = resolveTimeZone(request);

            Optional<Job> optionalJob = jobMongoDbRepository.findById(jobId);
            if(ObjectUtils.isEmpty(optionalJob)){
                Error error = Error.builder().errorCode(ErrorConstants.ERROR_WHILE_GETTING_JOB_CODE).errorMessage(ErrorConstants.ERROR_WHILE_GETTING_JOB_MESSAGE).build();
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }
            String organizationName;
            Optional<Organization> organization = organizationMongoDbRepository.findById(optionalJob.get().getOrganizationId());
            if(org.apache.commons.lang3.ObjectUtils.isEmpty(organization)){
                organizationName = getOrganizationName(request);
            } else{
                organizationName = organization.get().getName();
            }

            // Convert job start date → LocalDate
            LocalDate startDate = Instant.ofEpochMilli(optionalJob.get().getStartDate())
                    .atZone(zoneId)
                    .toLocalDate();

            ActivityAndNptHistoryResponse pumpTimeHistoryResponse = new ActivityAndNptHistoryResponse();
            List<ActivityLogEntry> activityLogEntries = activityLogRepository.findByJobId(jobId);
            if(ObjectUtils.isEmpty(activityLogEntries)){
                Error error = Error.builder().errorCode(ErrorConstants.ERROR_WHILE_GETTING_ACTIVITY_LOG_ENTRIES_CODE).errorMessage(ErrorConstants.ERROR_WHILE_GETTING_ACTIVITY_LOG_ENTRIES_MESSAGE).build();
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }

            List<ActivityLogEntry> filteredActivityLogEntries = new ArrayList<>();

            if(activityOrNptCode.equals("Pump Time")){
                filteredActivityLogEntries = activityLogEntries.stream()
                        .filter(entry -> "Pump Time".equalsIgnoreCase(
                                entry.getEventOrNptCode() == null ? "" : entry.getEventOrNptCode()
                        ))
                        .collect(Collectors.toList());

            } else if(activityOrNptCode.equals("NPT")){
                filteredActivityLogEntries = activityLogEntries.stream()
                        .filter(entry -> entry.getOpsActivity() != null
                                && entry.getOpsActivity().toUpperCase().contains("NPT"))
                        .collect(Collectors.toList());

            } else if(activityOrNptCode.equals("Scheduled Time")){
                filteredActivityLogEntries = activityLogEntries.stream()
                        .filter(entry -> "Scheduled Time".equals(entry.getOpsActivity()))
                        .filter(entry -> entry.getEventOrNptCode() == null
                                || !"Pump Time".equals(entry.getEventOrNptCode()))
                        .collect(Collectors.toList());

            } else if(activityOrNptCode.equals(organizationName + " NPT")){
                filteredActivityLogEntries = activityLogEntries.stream()
                        .filter(entry -> entry.getOpsActivity() != null
                                && entry.getOpsActivity().equals(organizationName + " NPT"))
                        .collect(Collectors.toList());

            } else if(activityOrNptCode.equals("Non-" + organizationName + " NPT")){
                filteredActivityLogEntries = activityLogEntries.stream()
                        .filter(entry -> {
                            if (entry.getOpsActivity() == null) return false;

                            String activity = entry.getOpsActivity().toUpperCase();
                            return activity.contains("NPT")
                                    && !activity.contains(organizationName.toUpperCase());
                        })
                        .collect(Collectors.toList());
            }

            long activityMillis = filteredActivityLogEntries.stream()
                    .mapToLong(ActivityLogEntry::getMillisecondsSpan)
                    .sum();

            int totalPumpMinutes = (int) (activityMillis / 60000);
            pumpTimeHistoryResponse.setPumpTimeHistoryTable(createPumpTimeHistoryTable(filteredActivityLogEntries, startDate));

            pumpTimeHistoryResponse.setTotal(formatMinutes(totalPumpMinutes));
            return new ResponseEntity<>(pumpTimeHistoryResponse, HttpStatus.OK);
        } catch (Exception e) {
            Error error = Error.builder().errorCode(ErrorConstants.ERROR_WHILE_GETTING_PUMP_TIME_HISTORY_DATA_CODE).errorMessage(ErrorConstants.ERROR_WHILE_GETTING_PUMP_TIME_HISTORY_DATA_MESSAGE).build();
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private String formatMinutes(int minutes) {
        int hrs = minutes / 60;
        int mins = minutes % 60;
        return String.format("%02d:%02d", hrs, mins);
    }
}
