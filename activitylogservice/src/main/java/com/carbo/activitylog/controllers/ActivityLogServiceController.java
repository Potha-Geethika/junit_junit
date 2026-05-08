package com.carbo.activitylog.controllers;

import com.carbo.activitylog.model.*;
import com.carbo.activitylog.services.*;
import com.carbo.activitylog.utils.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import static com.carbo.activitylog.utils.Constants.OPERATOR;
import static com.carbo.activitylog.utils.ControllerUtil.getCurDay;
import static com.carbo.activitylog.utils.ControllerUtil.getOrganizationId;
import static com.carbo.activitylog.utils.ControllerUtil.getOrganizationType;

@RestController
@RequestMapping(value = "v1/activity-logs")
public class ActivityLogServiceController {
    private static final Logger logger = LoggerFactory.getLogger(ActivityLogServiceController.class);

    private final ActivityLogService activityLogService;
    private final DistrictFleetTimeZoneService districtFleetTimeZoneService;
    private final JobService jobService;
    private final OperatorService operatorService;
    private final OrganizationService organizationService;

    @Autowired
    public ActivityLogServiceController(ActivityLogService activityLogService,
                                        DistrictFleetTimeZoneService districtFleetTimeZoneService,
                                        JobService jobService, OperatorService operatorService, OrganizationService organizationService) {
        this.activityLogService = activityLogService;
        this.districtFleetTimeZoneService = districtFleetTimeZoneService;
        this.jobService = jobService;
        this.operatorService = operatorService;
        this.organizationService = organizationService;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public List<ActivityLogEntry> getActivityLogs(HttpServletRequest request,
                                                  @RequestParam(name = "jobId", required = true) String jobId,
                                                  @RequestParam(name = "day", required = false) Integer day,
                                                  @RequestParam(name = "wellName", required = false) String wellName,
                                                  @RequestParam(name = "stage", required = false) Float stage,
                                                  @RequestParam(value = "sharedFrom", required = false) String sharedFromOrganizationId
                                                  ) {
        List<ActivityLogEntry> ret = new ArrayList<>();
        String organizationId = getOrganizationId(request);
        String organizationType = getOrganizationType(request);
        if(organizationType.contentEquals(OPERATOR)){
            Optional<Job> job=jobService.findByJobId(jobId);
            if (!ObjectUtils.isEmpty(job)) {
                Job getJob = job.get();
                sharedFromOrganizationId = getJob.getOrganizationId();
            }
        }
        String queriedOrganizationId;
        if (sharedFromOrganizationId == null) {
            queriedOrganizationId = organizationId;
        }
        else {
            if (operatorService.isShared(sharedFromOrganizationId, organizationId)) {
                queriedOrganizationId = sharedFromOrganizationId;
            } else {
                throw new AccessDeniedException("Is not shared!");
            }
        }
        if (jobId != null && day != null) {
            ret = activityLogService.findByOrganizationIdAndJobIdAndDay(queriedOrganizationId, jobId, day);
        } else if (jobId != null && wellName != null && stage != null) {
            ret = activityLogService.findByOrganizationIdAndJobIdAndWellAndStage(queriedOrganizationId, jobId, wellName, stage);
        } else if (jobId != null) {
            ret = activityLogService.findByOrganizationIdAndJobId(queriedOrganizationId, jobId);
        } else {
            throw new IllegalArgumentException("Filtering parameters is required.");
        }
        return ret;
    }
    @RequestMapping(value = "/getActivityByOrganizationIdAndJobId", method = RequestMethod.GET)
    public List<ActivityLogEntry> getActivityByOrganizationIdAndJobId(HttpServletRequest request,
                                                  @RequestParam(name = "jobId", required = true) String jobId,
                                                                      @RequestParam(value = "sharedFrom", required = false) String sharedFromOrganizationId

    ) {
        List<ActivityLogEntry> ret = new ArrayList<>();
        String organizationId = getOrganizationId(request);
        String queriedOrganizationId;
        if (sharedFromOrganizationId == null) {
            queriedOrganizationId = organizationId;
        }
        else {
            if (operatorService.isShared(sharedFromOrganizationId, organizationId)) {
                queriedOrganizationId = sharedFromOrganizationId;
            } else {
                throw new AccessDeniedException("Is not shared!");
            }
        }
         if (jobId != null) {
            ret = activityLogService.findByOrganizationIdAndJobId(queriedOrganizationId, jobId);
        } else {
            throw new IllegalArgumentException("Filtering parameters is required.");
        }
        return ret;
    }


    @RequestMapping(value = "/summary", method = RequestMethod.GET)
    public ActivityLogSummary getActivityLogSummary(HttpServletRequest request,
                                                    @RequestParam(name = "jobId", required = true) String jobId,
                                                    @RequestParam(value = "sharedFrom", required = false) String sharedFromOrganizationId
                                                  ) {
        System.out.println("***************************");                                        
        String organizationId = getOrganizationId(request);
        System.out.println("organizationId: "+organizationId);
        String organizationName = organizationService.get(organizationId).get().getName();
        System.out.println(organizationName);
        String queriedOrganizationId;
        if (sharedFromOrganizationId == null) {
            queriedOrganizationId = organizationId;
        }
        else {
            if (operatorService.isShared(sharedFromOrganizationId, organizationId)) {
                queriedOrganizationId = sharedFromOrganizationId;
            } else {
                throw new AccessDeniedException("Is not shared!");
            }
        }
        Optional<Job> job = jobService.getByOrganizationIdAndJobId(queriedOrganizationId, jobId);
        ActivityLogSummary ret = new ActivityLogSummary();
        if (job.isPresent()) {
            ZoneId zoneId = districtFleetTimeZoneService.getZone(queriedOrganizationId, job.get());
            int curDay = 0;
            if (zoneId != null && job.get().getStartDate() != null) {
                curDay = getCurDay(job.get().getStartDate(), zoneId);
                List<ActivityLogEntry> all = activityLogService.findByOrganizationIdAndJobId(queriedOrganizationId, jobId);
                // if current job do not have any activity log records, move on to the next one
                String opsType = job.get().getOperationsType();
                boolean isSimulFracJob =
                        (Constants.SIMUL_FRAC_OPS.equals(opsType) || Constants.SIMUL_FRAC_OPS_DB.equals(opsType))
                                && job.get().getBankCount() == BankCountEnum.MULTI_BANK;
                if (!all.isEmpty()) {
                    ret = new ActivityLogSummary(all, curDay, organizationName, job.get().getStartDate(),isSimulFracJob);
                }
            }
        }
        return ret;
    }

    @RequestMapping(value = "/{activityLogId}", method = RequestMethod.GET)
    public ActivityLogEntry getActivityLog(@PathVariable("activityLogId") String activityLogId) {
        ActivityLogEntry activityLog = activityLogService.getActivityLog(activityLogId).get();
        return activityLog;
    }

    @RequestMapping(value = "/{activityLogId}", method = RequestMethod.PUT)
    public void updateActivityLog(@PathVariable("activityLogId") String activityLogId, @RequestBody ActivityLogEntry activityLog) {
        activityLogService.updateActivityLog(activityLog);
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public void saveActivityLog(@RequestBody ActivityLogEntry activityLog) {
        activityLogService.saveActivityLog(activityLog);
    }

    @RequestMapping(value = "/{activityLogId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteActivityLog(HttpServletRequest request, @PathVariable("activityLogId") String activityLogId) {
        if(activityLogService.validateAndStoreActivityLog(request, activityLogId)){
        activityLogService.deleteActivityLog(activityLogId);
    }
    }

    @RequestMapping(value = "/latest", method = RequestMethod.GET)
    public ActivityLogEntry getLatestActivityLog(HttpServletRequest request, @RequestParam(name = "jobId", required = true) String jobId) {
        List<ActivityLogEntry> found = getActivityLogs(request, jobId, null, null, null, null);
        List<ActivityLogEntry> sorted = found.stream().sorted((o1, o2) -> o2.compareTo(o1)).collect(Collectors.toList());
        if (sorted.size() > 0) {
            return sorted.get(0);
        }
        else {
            return null;
        }
    }
    @RequestMapping(value = "/copyActivityLog", method = RequestMethod.POST)
    public ResponseEntity copyActivityLog(@RequestBody ActivityLogCopyPayload copyPayload) {
        return activityLogService.copyActivityLogs(copyPayload);
    }

    @RequestMapping(value = "/fetchMismatchedActivityLogs", method = RequestMethod.GET)
    public MismatchActivityLogsResponse fetchMismatchedActivityLogs(@RequestParam(required = false) String requestedOrganizationId,
                                                                    @RequestParam(required = false) String startDate,
                                                                    @RequestParam(required = false) String endDate) throws IOException {
        return activityLogService.fetchMismatchedActivityLogs(requestedOrganizationId, startDate, endDate, false);
    }


    @Scheduled(cron = "0 0 0 1 * *") // Runs at 12:00 AM on the 1st of every month
    public void notifyMismatchedActivityLogsMail() throws IOException {
        String startDate = activityLogService.calculateStartDate();
        String endDate = activityLogService.calculateEndDate();
        activityLogService.fetchMismatchedActivityLogs(null, startDate, endDate, true);
    }

    @RequestMapping(value = "/validate", method = RequestMethod.POST)
    public boolean validateActivity(HttpServletRequest request, @RequestParam(name = "jobId") String jobId) {
       return activityLogService.validateActivity(request,jobId);
    }

    @RequestMapping(value = "/pump-time-history", method = RequestMethod.GET)
    public ResponseEntity getPumpTimeHistory(HttpServletRequest request,
                                             @RequestParam(name = "jobId", required = true) String jobId, @RequestParam(name = "eventOrNptCode", required = true) String eventOrNptCode) {

        return activityLogService.getPumpTimeHistory(request, jobId, eventOrNptCode);
    }

    @RequestMapping(value = "/activity-and-npt-history", method = RequestMethod.GET)
    public ResponseEntity getActivityAndNptHistory(HttpServletRequest request,
                                             @RequestParam(name = "jobId", required = true) String jobId,  @RequestParam(name = "activityOrNptCode", required = true) String activityOrNptCode) {

        return activityLogService.getActivityAndNptHistory(request, jobId, activityOrNptCode);
    }

}
