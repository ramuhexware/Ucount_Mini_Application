package com.freddieapp.underwriting.dto;

import java.time.LocalDateTime;

public record NotificationDTO(
    String eventId,
    String status,
    String queueName,
    LocalDateTime timestamp
) {}
