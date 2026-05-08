package com.carbo.pad.controllers;

import com.carbo.pad.model.Pad;
import com.carbo.pad.model.SyncRequest;
import com.carbo.pad.model.SyncResponse;
import com.carbo.pad.services.PadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.*;

import static com.carbo.pad.utils.ControllerUtil.getOrganizationId;

@RestController
@RequestMapping(value = "v1/sync")
public class SyncController {
    private static final Logger logger = LoggerFactory.getLogger(SyncController.class);

    private final PadService padService;

    @Autowired
    public SyncController(PadService padService) {
        this.padService = padService;
    }

    @RequestMapping(value = "/view", method = RequestMethod.GET)
    public Map<String, Long> view(HttpServletRequest request) {
        Map<String, Long> result = new HashMap<>();
        String organizationId = getOrganizationId(request);
        List<Pad> all = padService.getByOrganizationId(organizationId);
        all.forEach(each -> result.put(each.getId(), each.getTs()));
        return result;
    }

    @RequestMapping(value = "/sync", method = RequestMethod.POST)
    public SyncResponse sync(HttpServletRequest request, @RequestBody SyncRequest sync) {
        SyncResponse response = new SyncResponse();
        if (sync.getRemove() != null && !sync.getRemove().isEmpty()) {
            Set<String> removed = new HashSet<>();
            for (String id : sync.getRemove()) {
                this.padService.deletePad(id);
                removed.add(id);
            }
            response.setRemoved(removed);
        }

        List<Pad> gets = new ArrayList<>();

        String organizationId = getOrganizationId(request);
        if (sync.getUpdate() != null && !sync.getUpdate().isEmpty()) {
            Map<String, Long> updated = new HashMap<>();
            for (Pad pad : sync.getUpdate()) {
                if (pad.getOrganizationId() != null) {
                    if (!pad.getOrganizationId().equals(organizationId)) {
                        continue;
                    }
                }
                else {
                    pad.setOrganizationId(organizationId);
                }
                if (pad.getTs() > 0) {
                    // update
                    Pad dbPad = this.padService.getPad(pad.getId()).get();
                    if (dbPad.getTs() > pad.getTs()) {
                        // db object is newer than the version sent from the client
                        gets.add(dbPad);
                    }
                    else {
                        this.padService.updatePad(pad);
                        updated.put(pad.getId(), pad.getTs());
                    }
                }
                else {
                    // insert
                    pad.setTs(System.currentTimeMillis());
                    Pad saved = this.padService.savePad(pad);
                    updated.put(saved.getId(), saved.getCreated());
                }
            }
            response.setUpdated(updated);
        }

        if (sync.getGet() != null && !sync.getGet().isEmpty()) {
            for (String id : sync.getGet()) {
                Optional<Pad> obj = this.padService.getPad(id);
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
