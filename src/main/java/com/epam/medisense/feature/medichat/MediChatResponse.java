package com.epam.medisense.feature.medichat;

public class MediChatResponse {

    private final String sessionId;
    private final MediChatMessage botMessage;

    public MediChatResponse(String sessionId, MediChatMessage botMessage) {
        this.sessionId = sessionId;
        this.botMessage = botMessage;
    }

    public String getSessionId() { return sessionId; }
    public MediChatMessage getBotMessage() { return botMessage; }
}
