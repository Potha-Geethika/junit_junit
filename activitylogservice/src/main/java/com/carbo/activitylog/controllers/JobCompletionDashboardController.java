package com.carbo.activitylog.controllers;

import com.carbo.activitylog.model.*;
import com.carbo.activitylog.services.JobCompletionDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

import static com.carbo.activitylog.utils.ControllerUtil.getOrganizationId;
import static com.carbo.activitylog.utils.ControllerUtil.getOrganizationName;

@RestController
@RequestMapping("/v1/job-complete-dashboard")
public class JobCompletionDashboardController {

    @Autowired
    JobCompletionDashboardService jobCompletionDashboardService;

    @GetMapping(value = "/activity-breakdown")
    public PadActivitySummary index(HttpServletRequest request, @RequestParam String jobId) {
        return jobCompletionDashboardService.getPadSummary(request, jobId);
    }

    @RequestMapping(value = "/stages-per-day", method = RequestMethod.GET)
    public List<StagePerDay> getStagesPerDay(HttpServletRequest request,
                                             @RequestParam(name = "jobId", required = true) String jobId) {

        return jobCompletionDashboardService.getStagesPerDay(request, jobId);
    }

    @RequestMapping(value = "/pump-hours-per-day", method = RequestMethod.GET)
    public List<PumpHoursPerDay> getPumpHoursPerDay(HttpServletRequest request,
                                                    @RequestParam(name = "jobId", required = true) String jobId) {
        return jobCompletionDashboardService.getPumpHoursPerDay(request, jobId);
    }

    @RequestMapping(value = "/pump-hours-per-stage", method = RequestMethod.GET)
    public List<PumpHoursPerStage> getPumpHoursPerStage(HttpServletRequest request,
                                                        @RequestParam(name = "jobId", required = true) String jobId,
                                                        @RequestParam(name = "well", required = true) String wellName) {
        return jobCompletionDashboardService.getPumpHoursPerStageFromLogs(request, jobId, wellName);
    }

    @RequestMapping(value = "/service-organization", method = RequestMethod.GET)
    public ServiceOrganizationDetails getServiceOrganization(HttpServletRequest request,
                                                             @RequestParam(name = "jobId", required = true) String jobId) {
        return jobCompletionDashboardService.getServiceOrganization(request, jobId);
    }
}