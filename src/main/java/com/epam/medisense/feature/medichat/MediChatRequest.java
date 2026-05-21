package com.epam.medisense.feature.medichat;

public class MediChatRequest {

    private final String sessionId;
    private final String message;

    public MediChatRequest(String sessionId, String message) {
        this.sessionId = sessionId;
        this.message = message;
    }

    public String getSessionId() { return sessionId; }
    public String getMessage() { return message; }
}
