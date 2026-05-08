package com.carbo.checklist.controllers;

import com.carbo.checklist.services.CheckListService;
import com.carbo.checklist.model.CheckList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

import static com.carbo.checklist.utils.ControllerUtil.getOrganizationId;

@RestController
@RequestMapping(value = "v1/checklists")
public class CheckListServiceController {
    private static final Logger logger = LoggerFactory.getLogger(CheckListServiceController.class);

    private final CheckListService checkListService;

    @Autowired
    public CheckListServiceController(CheckListService checkListService) {
        this.checkListService = checkListService;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public List<CheckList> getCheckLists(HttpServletRequest request, @RequestParam(name = "jobId", required = false) String jobId) {
        if (jobId == null || jobId.isEmpty()) {
            throw new IllegalArgumentException("jobId is required");
        } else {
            List<CheckList> ret = checkListService.getByJobId(jobId);
            return ret;
        }
    }

    @RequestMapping(value = "/{checkListId}", method = RequestMethod.GET)
    public CheckList getCheckList(@PathVariable("checkListId") String checkListId) {
        CheckList checkList = checkListService.getCheckList(checkListId).get();
        return checkList;
    }

    @RequestMapping(value = "/{checkListId}", method = RequestMethod.PUT)
    public void updateCheckList(HttpServletRequest request, @PathVariable("checkListId") String checkListId, @RequestBody CheckList checkList) {
        String organizationId = getOrganizationId(request);
        checkList.setOrganizationId(organizationId);
        checkListService.updateCheckList(checkList);
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public void saveCheckList(HttpServletRequest request, @RequestBody CheckList checkList) {
        String organizationId = getOrganizationId(request);
        checkList.setOrganizationId(organizationId);
        checkListService.saveCheckList(checkList);
    }

    @RequestMapping(value = "/{checkListId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCheckList(@PathVariable("checkListId") String checkListId) {
        checkListService.deleteCheckList(checkListId);
    }

}
