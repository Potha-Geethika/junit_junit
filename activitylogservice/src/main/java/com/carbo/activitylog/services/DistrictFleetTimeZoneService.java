package com.carbo.activitylog.services;

import com.carbo.activitylog.model.Job;
import com.carbo.activitylog.model.Pad;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Optional;
import java.util.TimeZone;

@Service
public class DistrictFleetTimeZoneService {

    private final PadService padService;

    public DistrictFleetTimeZoneService(PadService padService) {
        this.padService = padService;
    }


    public ZoneId getZone(String organizationId, Job job) {
        Optional<Pad> pad = getPad(organizationId, job);
        if (pad.isPresent()) {
            Pad found = pad.get();
            if (found.getTimezone() != null) {
                TimeZone tz = TimeZone.getTimeZone(found.getTimezone());
                return tz.toZoneId();
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }

    private Optional<Pad> getPad(String organizationId, Job job) {
        return padService.getByName(organizationId, job.getPad());
    }
}
