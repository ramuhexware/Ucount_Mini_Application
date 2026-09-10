package com.freddieapp.notification.service;

import java.util.Map;

public interface JobService {
    Map<String, Object> getJobStatus(String jobName, Map<String, Object> inMap, Map<String, Object> outMap);
    Map<String, Object> getJobHistory(String jobName, Map<String, Object> inMap, Map<String, Object> outMap);
}
