package com.carbo.pad.controllers;

import com.carbo.pad.model.JobDTO;
import com.carbo.pad.model.Pad;
import com.carbo.pad.services.PadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import static com.carbo.pad.utils.Constants.OPERATOR;
import static com.carbo.pad.utils.Constants.SHARED_ORGANIZATION_ID;
import static com.carbo.pad.utils.ControllerUtil.getOrganizationId;
import static com.carbo.pad.utils.ControllerUtil.getOrganizationType;

@RestController
@RequestMapping(value = "v1/pads")
public class PadServiceController {

    private static final Logger logger = LoggerFactory.getLogger(PadServiceController.class);

    private final PadService padService;

    private final MongoTemplate mongoTemplate;

    @Autowired
    public PadServiceController(PadService padService, MongoTemplate mongoTemplate) {
        this.padService = padService;
        this.mongoTemplate = mongoTemplate;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public List<Pad> getPads(HttpServletRequest request) {
        String organizationId = getOrganizationId(request);
        String organizationType = getOrganizationType(request);
        List<Pad> all;
        Set<String> organizationIds = new HashSet<>();
        organizationIds.add(organizationId);
        if(organizationType.contentEquals(OPERATOR)){
            Query query = new Query(Criteria.where(SHARED_ORGANIZATION_ID).is(organizationId));
            List<JobDTO> jobs = mongoTemplate.find(query, JobDTO.class,"jobs");
            if (!ObjectUtils.isEmpty(jobs)) {
                Set<String> sharedOrgIds = jobs.stream().map(JobDTO::getOrganizationId).collect(Collectors.toSet());
                organizationIds.addAll(sharedOrgIds);
            }
        }
            all = padService.getByOrganizationIdIn(organizationIds);
        return all;
    }

    @RequestMapping(value = "/{padId}", method = RequestMethod.GET)
    public Pad getPad(@PathVariable("padId") String padId) {
        logger.debug("Looking up data for pad {}", padId);

        Pad pad = padService.getPad(padId).get();
        return pad;
    }

    @RequestMapping(value = "/{padId}", method = RequestMethod.PUT)
    public void updatePad(@PathVariable("padId") String padId, @RequestBody Pad pad) {
        padService.updatePad(pad);
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public void savePad(@RequestBody Pad pad) {
        padService.savePad(pad);
    }

    @RequestMapping(value = "/{padId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePad(@PathVariable("padId") String padId) {
        padService.deletePad(padId);
    }
}
