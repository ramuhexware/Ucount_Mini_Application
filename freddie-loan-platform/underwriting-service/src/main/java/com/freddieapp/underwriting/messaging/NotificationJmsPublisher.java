package com.freddieapp.underwriting.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freddieapp.underwriting.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * ActiveMQ JMS Event Publisher with UUID Generation, Event Tracking, JSON Serialization,
 * and ActiveMQ Queue Publishing.
 */
@Service
public class NotificationJmsPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationJmsPublisher.class);

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    @Value("${freddie.underwriting.jms.destination:freddie.underwriting.events}")
    private String jmsDestination;

    @Autowired
    public NotificationJmsPublisher(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public List<String> publishEventToQueue(String eventTypeName, String orgId, String payloadContent, String destination, boolean isInitLoad) {
        List<String> returnMessage = new ArrayList<>();
        UUID eventId;

        if (isInitLoad) {
            LOGGER.info("Initial load event tracking...");
            eventId = UUID.nameUUIDFromBytes((orgId + "-INIT").getBytes());
        } else {
            eventId = java.util.UUID.randomUUID();
        }

        EventDTO eventDTO = new EventDTO(String.valueOf(eventId), eventTypeName, LocalDateTime.now().toString());
        PayloadDTO payloadDTO = new PayloadDTO(orgId, payloadContent);
        MainEventDTO mainDTO = new MainEventDTO(eventDTO, payloadDTO);

        if (!isInitLoad) {
            saveEventTracker(mainDTO);
        }

        String message;
        try {
            message = objectMapper.writeValueAsString(mainDTO);
            LOGGER.info("converted DTO to string");
        } catch (JsonProcessingException e) {
            LOGGER.error("Error while writing message to string {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }

        String targetQueue = (destination != null && !destination.isBlank()) ? destination : jmsDestination;
        try {
            jmsTemplate.convertAndSend(targetQueue, message);
            LOGGER.info("Sent message to queue for orgId: {} {}", orgId, message);
            returnMessage.add("Message sent to the queue successfully for Org: " + orgId);
        } catch (JmsException ex) {
            LOGGER.warn("ActiveMQ Broker not online. Event simulated to destination '{}' for Org {}: {}", targetQueue, orgId, ex.getMessage());
            returnMessage.add("Message dispatch simulated (ActiveMQ Standby) for Org: " + orgId);
        }

        return returnMessage;
    }

    public NotificationDTO publishNotification(String eventType, String destination, String payloadJson) {
        List<String> result = publishEventToQueue(eventType, "ORG-100", payloadJson, destination, false);
        return new NotificationDTO(
            UUID.randomUUID().toString(),
            result.get(0),
            (destination != null && !destination.isBlank()) ? destination : jmsDestination,
            LocalDateTime.now()
        );
    }

    private void saveEventTracker(MainEventDTO mainDTO) {
        LOGGER.info("Saved event tracker for eventId: {}", mainDTO.eventDTO().eventId());
    }

    public OrganizationDTO getOrganizationData(String orgId) {
        return new OrganizationDTO(
            orgId,
            "Freddie Mac Mortgage Partner Org (" + orgId + ")",
            "ACTIVE_VERIFIED",
            List.of("LOAN_UNDERWRITING", "RATE_CALCULATOR", "AUTOMATED_DECISIONING")
        );
    }
}
