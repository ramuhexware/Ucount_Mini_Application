package com.freddieapp.notification.controller;

import com.freddieapp.notification.dto.EventDTO;
import com.freddieapp.notification.dto.MainDTO;
import com.freddieapp.notification.dto.PayloadDTO;
import com.freddieapp.notification.service.EventPublisherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationEventController {

    @Autowired
    private EventPublisherService eventPublisherService;

    @PostMapping("/publish")
    public List<String> publishNotificationEvent(
            @RequestParam(required = false) String destination,
            @RequestParam(required = false, defaultValue = "ORG-001") String orgId,
            @RequestParam(required = false, defaultValue = "LOAN_NOTIFICATION_EVENT") String eventTypeName,
            @RequestParam(required = false, defaultValue = "false") boolean isInitLoad,
            @RequestBody(required = false) MainDTO mainDTO) {

        EventDTO eventDTO = mainDTO != null ? mainDTO.getEventDTO() : null;
        PayloadDTO payloadDTO = mainDTO != null ? mainDTO.getPayloadDTO() : null;
        return eventPublisherService.publishEvent(destination, orgId, eventTypeName, isInitLoad, eventDTO, payloadDTO, mainDTO);
    }
}
