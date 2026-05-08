package com.carbo.activitylog.controllers;

import com.carbo.activitylog.model.ActivityLogEntry;
import com.carbo.activitylog.model.DeletedActivityLogEntry;
import com.carbo.activitylog.model.SyncRequest;
import com.carbo.activitylog.model.SyncResponse;
import com.carbo.activitylog.services.ActivityLogService;
import com.carbo.activitylog.services.DeletedActivityLogService;
import com.carbo.activitylog.utils.Constants;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpServletRequest;

import java.util.*;

import static com.carbo.activitylog.utils.ControllerUtil.getOrganizationId;
import static com.carbo.activitylog.utils.ControllerUtil.getUserFullName;

@RestController
@RequestMapping(value = "v1/sync")
public class SyncController {
    private static final Logger logger = LoggerFactory.getLogger(SyncController.class);

    private final ActivityLogService activityLogService;
    private final DeletedActivityLogService deletedActivityLogService;
    private final ModelMapper modelMapper;


    @Autowired
    public SyncController(ActivityLogService activityLogService, DeletedActivityLogService deletedActivityLogService, ModelMapper mapper) {
        this.activityLogService = activityLogService;
        this.deletedActivityLogService = deletedActivityLogService;
        this.modelMapper = mapper;
    }

    @RequestMapping(value = "/view", method = RequestMethod.GET)
    public Map<String, Long> view(HttpServletRequest request,
                                  @RequestParam(name = "jobId", required = true) String jobId) {
        Map<String, Long> result = new HashMap<>();
        String organizationId = getOrganizationId(request);
        List<ActivityLogEntry> all = activityLogService.findByOrganizationIdAndJobId(organizationId, jobId);
        all.forEach(each -> result.put(each.getId(), each.getTs()));
        return result;
    }

    @RequestMapping(value = "/sync", method = RequestMethod.POST)
    public SyncResponse<ActivityLogEntry> sync(HttpServletRequest request,
                                               @RequestBody SyncRequest<?> sync,
                                               @RequestParam(name = "jobId", required = true) String jobId) {
        SyncResponse response = new SyncResponse<ActivityLogEntry>();

        String organizationId = getOrganizationId(request);
        List<ActivityLogEntry> allRecords = this.activityLogService.findByOrganizationIdAndJobId(organizationId, jobId);

        if (sync.getRemove() != null && !sync.getRemove().isEmpty()) {
            Set<String> removed = new HashSet<>();
            for (String id : sync.getRemove()) {
                Optional<ActivityLogEntry> dbActivityLog = allRecords.stream().filter(each -> each.getId().equals(id)).findFirst();
                if (dbActivityLog.isPresent()) {
                    this.deletedActivityLogService.saveActivityLog(new DeletedActivityLogEntry(dbActivityLog.get()));
                    this.activityLogService.deleteActivityLog(id);
                    removed.add(id);
                }
            }
            response.setRemoved(removed);
        }

        List<ActivityLogEntry> gets = new ArrayList<>();

        if (sync.getUpdate() != null && !sync.getUpdate().isEmpty()) {
            try {
                Map<String, Long> updated = new HashMap<>();
                for (Object obj : sync.getUpdate()) {
                    // Convert the object to JSON string and then map to ActivityLogEntry
                    Map<String, Object> activityLog = (Map<String, Object>) obj;
                    activityLog.put(Constants.ORGANISATIONID, organizationId);

                    String id = (String) activityLog.get(Constants.ID);
                    Long ts = activityLog.get(Constants.TS) instanceof Number ? ((Number) activityLog.get(Constants.TS)).longValue() : 0L;
                    Optional<ActivityLogEntry> activityLogEntryOpt = allRecords.stream().filter(each -> each.getId().equals(id)).findFirst();

                    if (ts > 0 && activityLogEntryOpt.isPresent()) {
                        ActivityLogEntry activityLogEntry = modelMapper.map(activityLog, ActivityLogEntry.class);
                        ActivityLogEntry dbActivityLog = activityLogEntryOpt.get();
                        if (dbActivityLog.getTs() > ts) {
                            gets.add(dbActivityLog);
                        } else {
                            activityLogService.updateActivityLog(activityLogEntry);
                            updated.put(id, ts);
                        }
                    } else {
                        modelMapper.getConfiguration()
                                .setFieldMatchingEnabled(true)
                                .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE)
                                .setAmbiguityIgnored(true);
                        ActivityLogEntry activityLogEntry = modelMapper.map(activityLog, ActivityLogEntry.class);
                        activityLogEntry.setTs(System.currentTimeMillis());
                        ActivityLogEntry saved = this.activityLogService.saveActivityLog(activityLogEntry);
                        updated.put(saved.getId(), saved.getCreated());
                    }
                }
                response.setUpdated(updated);
            } catch (DuplicateKeyException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, Constants.DUPLICATE_RECORD_FOUND);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, Constants.ERROR_WHILE_CREATE_OR_UPDATE);
            }

        }


        if (sync.getGet() != null && !sync.getGet().isEmpty()) {
            for (String id : sync.getGet()) {
                Optional<ActivityLogEntry> obj = allRecords.stream().filter(each -> each.getId().equals(id)).findFirst();
                if (obj.isPresent()) {
                    gets.add(obj.get());
                }
            }

            if (!gets.isEmpty()) {
                response.setGet(gets);
            }
        }

        return response;
    }
}
