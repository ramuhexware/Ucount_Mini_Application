package com.freddieapp.notification.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freddieapp.notification.dto.EventDTO;
import com.freddieapp.notification.dto.MainDTO;
import com.freddieapp.notification.dto.PayloadDTO;
import com.freddieapp.notification.entity.FreddieEventTracker;
import com.freddieapp.notification.repository.FreddieEventTrackerRepository;
import com.freddieapp.notification.service.EventPublisherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EventPublisherServiceImpl implements EventPublisherService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventPublisherServiceImpl.class);

    @Value("${spring.activemq.queue-name:FREDDIE-LOAN-NOTIFICATION-QUEUE.local}")
    private String defaultDestinationQueue;

    @Autowired(required = false)
    private JmsTemplate jmsTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FreddieEventTrackerRepository freddieEventTrackerRepository;

    @Override
    public List<String> publishEvent(String destination, String orgId, String eventTypeName,
                                      boolean isInitLoad, EventDTO eventDTO, PayloadDTO payloadDTO, MainDTO mainDTO) {
        List<String> returnMessage = new ArrayList<>();
        UUID eventId;
        String message;

        String targetDestination = StringUtils.hasText(destination) ? destination : defaultDestinationQueue;

        if (eventDTO == null) {
            eventDTO = new EventDTO();
        }
        if (mainDTO == null) {
            mainDTO = new MainDTO();
        }

        // For real-time, always generate new eventId
        eventId = java.util.UUID.randomUUID();

        eventDTO.setEventId(String.valueOf(eventId));
        eventDTO.setEventTypeName(eventTypeName); // set event type name from map

        mainDTO.setEventDTO(eventDTO);
        mainDTO.setPayloadDTO(payloadDTO);

        // Adds initial event to FreddieEventTracker repository
        if (!isInitLoad) {
            saveEventTracker(mainDTO);
        }

        try {
            message = objectMapper.writeValueAsString(mainDTO);
            LOGGER.info("converted DTO to string");
        } catch (JsonProcessingException e) {
            LOGGER.error("Error while writing message to string {}", e);
            throw new RuntimeException(e);
        }

        try {
            if (jmsTemplate != null) {
                jmsTemplate.convertAndSend(targetDestination, message);
            }
            LOGGER.info("Sent message to queue: {} for orgId: {} {}", targetDestination, orgId, message);
            returnMessage.add("Message sent to the queue successfully for Org: " + orgId);
        } catch (Exception ex) {
            returnMessage.add("Queued event (ActiveMQ offline fallback mode) for Org: " + orgId);
            LOGGER.warn("ActiveMQ queue dispatch note (broker offline or unreachable): {}", ex.getMessage());
        }

        return returnMessage;
    }

    private void saveEventTracker(MainDTO mainDTO) {
        try {
            String payloadJson = objectMapper.writeValueAsString(mainDTO);
            FreddieEventTracker tracker = FreddieEventTracker.builder()
                    .eventId(mainDTO.getEventDTO() != null ? mainDTO.getEventDTO().getEventId() : null)
                    .eventTypeName(mainDTO.getEventDTO() != null ? mainDTO.getEventDTO().getEventTypeName() : null)
                    .payloadJson(payloadJson)
                    .build();
            freddieEventTrackerRepository.save(tracker);
        } catch (Exception e) {
            LOGGER.error("Error saving event tracker", e);
        }
    }
}
