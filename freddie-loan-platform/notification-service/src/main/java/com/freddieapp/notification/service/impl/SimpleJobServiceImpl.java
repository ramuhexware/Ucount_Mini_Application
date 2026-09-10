package com.freddieapp.notification.service.impl;

import com.freddieapp.notification.service.JobService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("simpleJobService")
public class SimpleJobServiceImpl implements JobService {

    @Override
    public Map<String, Object> getJobStatus(String jobName, Map<String, Object> inMap, Map<String, Object> outMap) {
        Map<String, Object> result = new HashMap<>(outMap);
        result.put("jobName", jobName);
        result.put("status", "COMPLETED");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }

    @Override
    public Map<String, Object> getJobHistory(String jobName, Map<String, Object> inMap, Map<String, Object> outMap) {
        Map<String, Object> result = new HashMap<>(outMap);
        result.put("jobName", jobName);
        result.put("history", "Job execution history log for simpleJobService");
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
}
