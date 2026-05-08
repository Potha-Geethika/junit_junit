package com.carbo.activitylog.controllers.external;

import com.carbo.activitylog.model.ActivityLogEntry;
import com.carbo.activitylog.model.ActivityLogSummary;
import com.carbo.activitylog.model.User;
import com.carbo.activitylog.model.ServiceAccount;
import com.carbo.activitylog.model.Job;
import com.carbo.activitylog.services.ActivityLogService;
import com.carbo.activitylog.services.UserService;
import com.carbo.activitylog.services.ServiceAccountService;
import com.carbo.activitylog.services.JobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.carbo.activitylog.utils.ControllerUtil.getOrganizationType;
import static com.carbo.activitylog.utils.ControllerUtil.getOrganizationName;
import static com.carbo.activitylog.utils.ControllerUtil.getUserName;


@RestController
@RequestMapping(value = "v1/activity-logs/external")
public class ExternalActivityLogServiceController {
    private static final Logger logger = LoggerFactory.getLogger(ExternalActivityLogServiceController.class);

    private final ActivityLogService activityLogService;
    private final UserService userService;
    private final ServiceAccountService serviceAccountService;
    private final JobService jobService;

    @Autowired
    public ExternalActivityLogServiceController(ActivityLogService activityLogService, ServiceAccountService serviceAccountService, UserService userService, JobService jobService) {
        this.activityLogService = activityLogService;
        this.userService = userService;
        this.serviceAccountService = serviceAccountService;
        this.jobService = jobService;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public List<ActivityLogEntry> getActivityLogs(HttpServletRequest request,
                                                  @RequestParam(name = "jobNumber", required = true) String jobNumber,
                                                  @RequestParam(name = "day", required = false) Integer day,
                                                  @RequestParam(name = "wellName", required = false) String wellName,
                                                  @RequestParam(name = "stage", required = false) Float stage
                                                  ) {
        List<ActivityLogEntry> ret;
        String organizationType = getOrganizationType(request);
        List<ActivityLogEntry> result = new ArrayList<>();
        if (organizationType.contentEquals("PARTNER")) {
            String userName = getUserName(request);
            Optional<User> userInfo = userService.getUserByUserName(userName);
            String userId = userInfo.get().getId();
            Optional<ServiceAccount> serviceAccount = serviceAccountService.get(userId);
            if (serviceAccount.isPresent()) {
                List<String> organizationIds = serviceAccount.get().getOrganizationIds();
                List<Job> jobs = jobService.getJobByJobNumber(jobNumber);
                if (jobs != null) {
                    for (Job job : jobs) {
                        if(organizationIds.contains(job.getOrganizationId())){
                            if (day != null) {
                                ret = activityLogService.findByOrganizationIdAndJobIdAndDay(job.getOrganizationId(), job.getId(), day);
                            }
                            else if  (wellName != null && stage != null) {
                                ret = activityLogService.findByOrganizationIdAndJobIdAndWellAndStage(job.getOrganizationId(), job.getId(), wellName, stage);
                            }
                            else  {
                                ret = activityLogService.findByOrganizationIdAndJobId(job.getOrganizationId(), job.getId());
                            }
                            for (ActivityLogEntry entry : ret) {
                                result.add(entry);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    @RequestMapping(value = "/summary", method = RequestMethod.GET)
    public ActivityLogSummary getActivityLogSummary(HttpServletRequest request,
                                                          @RequestParam(name = "jobNumber", required = true) String jobNumber
                                                  ) {
        String organizationType = getOrganizationType(request);
        String organizationName = getOrganizationName(request);
        List<ActivityLogEntry> result = new ArrayList<>();
        if (organizationType.contentEquals("PARTNER")) {
            String userName = getUserName(request);
            Optional<User> userInfo = userService.getUserByUserName(userName);
            String userId = userInfo.get().getId();
            Optional<ServiceAccount> serviceAccount = serviceAccountService.get(userId);
            if (serviceAccount.isPresent()) {
                List<String> organizationIds = serviceAccount.get().getOrganizationIds();
                List<Job> jobs = jobService.getJobByJobNumber(jobNumber);
                if (jobs != null) {
                    for (Job job : jobs) {
                        if(organizationIds.contains(job.getOrganizationId())){
                            List<ActivityLogEntry> all = activityLogService.findByOrganizationIdAndJobId(job.getOrganizationId(), job.getId());
                            for (ActivityLogEntry entry : all) {
                                result.add(entry);
                            }
                        }
                    }
                }
            }
        }
        ActivityLogSummary ret = new ActivityLogSummary(result, null, organizationName);
        return ret;
    }

    @RequestMapping(value = "/{activityLogId}", method = RequestMethod.GET)
    public ActivityLogEntry getConsumable(HttpServletRequest request,
                                                 @PathVariable("activityLogId") String activityLogId) {
        String organizationType = getOrganizationType(request);
        ActivityLogEntry result = null;
        if (organizationType.contentEquals("PARTNER")) {
            String userName = getUserName(request);
            Optional<User> userInfo = userService.getUserByUserName(userName);
            String userId = userInfo.get().getId();
            Optional<ServiceAccount> serviceAccount = serviceAccountService.get(userId);
            if (serviceAccount.isPresent()) {
                List<String> organizationIds = serviceAccount.get().getOrganizationIds();
                result = activityLogService.getActivityLog(activityLogId).get();
                if(organizationIds.contains(result.getOrganizationId())){
                    return result;
                } else {
                    throw new AccessDeniedException("This account do not have access to see ");
                }
            }
        }
        return result;
    }
}
