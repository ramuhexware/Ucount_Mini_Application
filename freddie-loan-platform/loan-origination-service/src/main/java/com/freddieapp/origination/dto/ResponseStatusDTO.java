package com.freddieapp.origination.dto;

public class ResponseStatusDTO {
    private int statusCode;
    private String statusMessage;

    public ResponseStatusDTO() {}

    public ResponseStatusDTO(int statusCode, String statusMessage) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
    public String getStatusMessage() { return statusMessage; }
    public void setStatusMessage(String statusMessage) { this.statusMessage = statusMessage; }
}
