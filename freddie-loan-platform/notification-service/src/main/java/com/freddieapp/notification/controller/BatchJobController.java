package com.freddieapp.notification.controller;

import com.freddieapp.notification.dao.ProcessDao;
import com.freddieapp.notification.service.JobService;
import com.freddieapp.notification.util.ProcessValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.MapBindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class BatchJobController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchJobController.class);

    @Autowired
    @Qualifier("simpleJobService")
    private JobService jobService;

    @Autowired
    @Qualifier("jobParamsValidator")
    private Validator jobParamsValidator;

    @Autowired
    private ProcessDao dao;

    @Autowired
    @Qualifier("loanSyncJobService")
    private JobService loanSyncJobService;

    private String transToMainJob = "freddie-loan-sync-job";
    private String adhocJob = "freddie-adhoc-job";
    private String jobNameMap = "jobName";
    private String inMapCosnt = "inMap";
    private String acctgCycleConst = "acctgCycle";

    @PostMapping(value = "/jobs/{jobName}/status", consumes = "application/json", produces = "application/json")
    public Map<String, Object> getJobStatus(
            @RequestBody final Map<String, Object> inMap,
            @PathVariable final String jobName) {
        return commonStatus(inMap, jobName);
    }

    private Map<String, Object> commonStatus(final Map<String, Object> inMap,
                                              final String jobName) {
        MapBindingResult bindingResult = new MapBindingResult(inMap, inMapCosnt);
        jobParamsValidator.validate(inMap, bindingResult);
        Map<String, Object> outMap = new HashMap<>(inMap);
        if (bindingResult.hasErrors()) {
            return ProcessValidationUtil.parseErrors(bindingResult, outMap);
        } else {
            JobService targetService = transToMainJob.equalsIgnoreCase(jobName) ? loanSyncJobService : jobService;
            return targetService.getJobStatus(jobName, inMap, outMap);
        }
    }

    @PostMapping(value = "/jobs/{jobName}/history", consumes = "application/json", produces = "application/json")
    public Map<String, Object> getJobHistory(
            @RequestBody final Map<String, Object> inMap,
            @PathVariable final String jobName) {
        return commonJobHistory(inMap, jobName);
    }

    public Map<String, Object> commonJobHistory(
            @RequestBody final Map<String, Object> inMap,
            @PathVariable final String jobName) {
        MapBindingResult bindingResult = new MapBindingResult(inMap, inMapCosnt);
        jobParamsValidator.validate(inMap, bindingResult);
        Map<String, Object> outMap = new HashMap<>(inMap);
        if (bindingResult.hasErrors()) {
            return ProcessValidationUtil.parseErrors(bindingResult, outMap);
        } else {
            JobService targetService = transToMainJob.equalsIgnoreCase(jobName) ? loanSyncJobService : jobService;
            return targetService.getJobHistory(jobName, inMap, outMap);
        }
    }
}
