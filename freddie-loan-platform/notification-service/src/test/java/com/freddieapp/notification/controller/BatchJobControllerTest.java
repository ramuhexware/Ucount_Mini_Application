package com.freddieapp.notification.controller;

import com.freddieapp.notification.dao.ProcessDao;
import com.freddieapp.notification.service.JobService;
import com.freddieapp.notification.validation.JobParamsValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(BatchJobController.class)
public class BatchJobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean(name = "simpleJobService")
    private JobService simpleJobService;

    @MockBean(name = "loanSyncJobService")
    private JobService loanSyncJobService;

    @MockBean(name = "jobParamsValidator")
    private JobParamsValidator jobParamsValidator;

    @MockBean
    private ProcessDao processDao;

    @Test
    public void testGetJobStatus_SimpleJob() throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("jobName", "sample-job");
        result.put("status", "COMPLETED");

        when(simpleJobService.getJobStatus(eq("sample-job"), any(), any())).thenReturn(result);

        mockMvc.perform(post("/jobs/sample-job/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    public void testGetJobStatus_LoanSyncJob() throws Exception {
        Map<String, Object> result = new HashMap<>();
        result.put("jobName", "freddie-loan-sync-job");
        result.put("status", "RUNNING");

        when(loanSyncJobService.getJobStatus(eq("freddie-loan-sync-job"), any(), any())).thenReturn(result);

        mockMvc.perform(post("/jobs/freddie-loan-sync-job/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RUNNING"));
    }
}
