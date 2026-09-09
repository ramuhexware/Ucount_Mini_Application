package com.freddieapp.customerservice.event;

import com.freddieapp.customerservice.entity.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
public class CustomerEventPublisher {

    public void publishCustomerCreated(Customer customer) {
        log.info("CustomerCreatedEvent recorded for customerId={}", customer.getId());
    }

    public void publishCustomerUpdated(Customer customer) {
        log.info("CustomerUpdatedEvent recorded for customerId={}", customer.getId());
    }

    @lombok.Builder
    public record CustomerCreatedEvent(
            String eventId, String eventType, String customerId,
            String email, String kycStatus, Instant occurredAt) {}

    @lombok.Builder
    public record CustomerUpdatedEvent(
            String eventId, String eventType, String customerId, Instant occurredAt) {}
}
