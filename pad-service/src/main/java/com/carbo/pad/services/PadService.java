package com.carbo.pad.services;

import com.carbo.pad.events.source.PadTimezoneSourceBean;
import com.carbo.pad.model.Pad;
import com.carbo.pad.repository.PadMongoDbRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class PadService {
    private final PadMongoDbRepository padRepository;
    private final PadTimezoneSourceBean padTimezoneSourceBean;

    @Autowired
    public PadService(PadMongoDbRepository padRepository,
                      PadTimezoneSourceBean padTimezoneSourceBean) {
        this.padRepository = padRepository;
        this.padTimezoneSourceBean = padTimezoneSourceBean;
    }

    public List<Pad> getAll() {
        return padRepository.findAll();
    }

    public List<Pad> getByOrganizationId(String organizationId) {
        return padRepository.findByOrganizationId(organizationId);
    }

    public Optional<Pad> getPad(String padId) {
        return padRepository.findById(padId);
    }

    public Pad savePad(Pad pad) {
        return padRepository.save(pad);
    }

    public void updatePad(Pad pad) {
        Optional<Pad> padInDbOptional = padRepository.findById(pad.getId());
        if (didPadTimeZoneChanged(pad, padInDbOptional)) {
            String previousTimezone = padInDbOptional.get().getTimezone() != null ? padInDbOptional.get().getTimezone() : pad.getTimezone();
            padTimezoneSourceBean.publishPadTimezoneChange("UPDATE", pad, previousTimezone);
        }
        padRepository.save(pad);
    }

    public void deletePad(String padId) {
        padRepository.deleteById(padId);
    }

    private boolean didPadTimeZoneChanged(Pad updatedPad, Optional<Pad> padInDbOptional) {
        if (padInDbOptional != null && padInDbOptional.isPresent()) {
            Pad padInDb = padInDbOptional.get();
            return padInDb.getTimezone() != null ? !padInDb.getTimezone().equals(updatedPad.getTimezone()) : true;
        } else {
            return updatedPad.getTimezone() != null;
        }
    }

    public List<Pad> getByOrganizationIdIn(Set<String> organizationIds) {
        return padRepository.findByOrganizationIdIn(organizationIds);
    }
}
