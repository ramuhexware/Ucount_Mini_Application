package com.freddieapp.notification.domain;

import java.time.LocalDateTime;

public class NotificationDomain {

    private String id;
    private String eventTypeName;
    private String status;
    private LocalDateTime timestamp;

    public NotificationDomain() {}

    public NotificationDomain(String id, String eventTypeName, String status, LocalDateTime timestamp) {
        this.id = id;
        this.eventTypeName = eventTypeName;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEventTypeName() { return eventTypeName; }
    public void setEventTypeName(String eventTypeName) { this.eventTypeName = eventTypeName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
