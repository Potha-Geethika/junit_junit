package com.carbo.pad.controllers;

import com.carbo.pad.services.JobDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "v1/job-complete-dashboard")
public class JobDashboardController {

    private final JobDashboardService jobDashboardService;

    @Autowired
    public JobDashboardController(JobDashboardService jobDashboardService) {
        this.jobDashboardService = jobDashboardService;
    }

    @RequestMapping(value = "/pad-details", method = RequestMethod.GET)
    public ResponseEntity<?> getPadDetails(HttpServletRequest request, @RequestParam String jobId) {
        return jobDashboardService.getPadDetails(request, jobId);
    }

    @RequestMapping(value = "/well-completion-information", method = RequestMethod.GET)
    public ResponseEntity<?> getWellCompletionInformation(HttpServletRequest request, @RequestParam String jobId, @RequestParam String wellId) {
        return jobDashboardService.getWellCompletionInformation(request, jobId, wellId);
    }

    @RequestMapping(value = "/clean-per-stage", method = RequestMethod.GET)
    public ResponseEntity<?> getCleanPerStage(HttpServletRequest request, @RequestParam String jobId, @RequestParam String wellId) {
        return jobDashboardService.getCleanPerStage(request, jobId, wellId);
    }

    @RequestMapping(value = "/final-isip-and-fg", method = RequestMethod.GET)
    public ResponseEntity<?> getFinalISIPAndFG(HttpServletRequest request, @RequestParam String jobId, @RequestParam String wellId) {
        return jobDashboardService.getFinalISIPAndFG(request, jobId, wellId);
    }

    @RequestMapping(value = "/average-pressure-rate", method = RequestMethod.GET)
    public ResponseEntity<?> getAveragePressureAndRate(HttpServletRequest request, @RequestParam String jobId, @RequestParam String wellId) {
        return jobDashboardService.getAveragePressureAndRate(request, jobId, wellId);
    }

    @RequestMapping(value = "/average-vs-max", method = RequestMethod.GET)
    public ResponseEntity<?> getAverageVsMax(HttpServletRequest request, @RequestParam String jobId, @RequestParam String wellId) {
        return jobDashboardService.getAverageVsMax(request, jobId, wellId);
    }
}
