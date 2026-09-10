package com.freddieapp.notification.service;

import com.freddieapp.notification.dto.EventDTO;
import com.freddieapp.notification.dto.MainDTO;
import com.freddieapp.notification.dto.PayloadDTO;

import java.util.List;

public interface EventPublisherService {

    /**
     * Publishes event DTO payload to JMS destination queue and tracks event in database.
     *
     * @param destination destination queue name
     * @param orgId organization identifier
     * @param eventTypeName event type name
     * @param isInitLoad initial load flag
     * @param eventDTO event metadata DTO
     * @param payloadDTO event payload DTO
     * @param mainDTO main aggregate DTO
     * @return list of status return messages
     */
    List<String> publishEvent(String destination, String orgId, String eventTypeName,
                              boolean isInitLoad, EventDTO eventDTO, PayloadDTO payloadDTO, MainDTO mainDTO);
}
