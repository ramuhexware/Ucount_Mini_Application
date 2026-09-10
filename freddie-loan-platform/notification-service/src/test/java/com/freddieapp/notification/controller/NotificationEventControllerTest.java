package com.freddieapp.notification.controller;

import com.freddieapp.notification.service.EventPublisherService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(NotificationEventController.class)
public class NotificationEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventPublisherService eventPublisherService;

    @Test
    public void testPublishNotificationEvent() throws Exception {
        when(eventPublisherService.publishEvent(any(), eq("ORG-001"), any(), eq(false), any(), any(), any()))
                .thenReturn(Collections.singletonList("Message sent to the queue successfully for Org: ORG-001"));

        mockMvc.perform(post("/api/notifications/publish")
                        .param("orgId", "ORG-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Message sent to the queue successfully for Org: ORG-001"));
    }
}
