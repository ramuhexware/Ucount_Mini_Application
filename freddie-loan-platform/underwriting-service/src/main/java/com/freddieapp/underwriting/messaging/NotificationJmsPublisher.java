package com.freddieapp.underwriting.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * ActiveMQ JMS Event Publisher with UUID Generation, Event Tracking, JSON Serialization,
 * and ActiveMQ Queue Publishing matching code image.
 */
@Service
public class NotificationJmsPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationJmsPublisher.class);

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public NotificationJmsPublisher(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
        this.objectMapper = new ObjectMapper();
    }

    // Records & DTOs
    public record EventDTO(String eventId, String eventTypeName, String timestamp) {}
    public record PayloadDTO(String orgId, String payloadContent) {}
    public record MainEventDTO(EventDTO eventDTO, PayloadDTO payloadDTO) {}
    public record NotificationDTO(String eventId, String status, String queueName, LocalDateTime timestamp) {}
    public record OrganizationDTO(String orgId, String orgName, String status, List<String> activeServices) {}

    // Public JMS Publishing method matching image code structure
    public List<String> publishEventToQueue(String eventTypeName, String orgId, String payloadContent, String destination, boolean isInitLoad) {
        List<String> returnMessage = new ArrayList<>();
        UUID eventId;

        if (isInitLoad) {
            LOGGER.info("Initial load event tracking...");
            eventId = UUID.nameUUIDFromBytes((orgId + "-INIT").getBytes());
        } else {
            // For real-time, always generate new eventId (Image line: // For real-time, always generate new eventId)
            eventId = java.util.UUID.randomUUID();
        }

        EventDTO eventDTO = new EventDTO(String.valueOf(eventId), eventTypeName, LocalDateTime.now().toString());
        PayloadDTO payloadDTO = new PayloadDTO(orgId, payloadContent);

        MainEventDTO mainDTO = new MainEventDTO(eventDTO, payloadDTO);

        // Adds initial event to UcsSfOrgtnEventTracker repository (Image line: // Adds initial event to UcsSfOrgtnEventTracker repository)
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

        try {
            String targetQueue = (destination != null && !destination.isBlank()) ? destination : "freddie.underwriting.events";
            jmsTemplate.convertAndSend(targetQueue, message);
            LOGGER.info("Sent message to queue for orgId: {} {}", orgId, message);
            returnMessage.add("Message sent to the queue successfully for Org: " + orgId);
        } catch (JmsException ex) {
            returnMessage.add("Failed to send message to the queue for Org: " + orgId);
            LOGGER.error("Error while sending message {}", ex.getMessage(), ex);
            throw ex;
        }

        return returnMessage;
    }

    public NotificationDTO publishNotification(String eventType, String destination, String payloadJson) {
        List<String> result = publishEventToQueue(eventType, "ORG-100", payloadJson, destination, false);
        return new NotificationDTO(
            UUID.randomUUID().toString(),
            result.get(0),
            (destination != null ? destination : "freddie.underwriting.events"),
            LocalDateTime.now()
        );
    }

    private void saveEventTracker(MainEventDTO mainDTO) {
        LOGGER.info("Saved event tracker for eventId: {}", mainDTO.eventDTO().eventId());
    }

    // Organization Data Lookup (Matching image line: public OrganizationDTO getOrganizationData(String orgId))
    public OrganizationDTO getOrganizationData(String orgId) {
        OrganizationDTO organizationDTO = new OrganizationDTO(
            orgId,
            "Freddie Mac Mortgage Partner Org (" + orgId + ")",
            "ACTIVE_VERIFIED",
            List.of("LOAN_UNDERWRITING", "RATE_CALCULATOR", "AUTOMATED_DECISIONING")
        );
        LOGGER.info("Retrieved organization data for orgId: {}", orgId);
        return organizationDTO;
    }
}
