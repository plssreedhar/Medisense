package com.epam.medisense.feature.medichat;

import java.util.List;

public class MediChatSession {

    private final String sessionId;
    private final List<MediChatMessage> messages;
    private String extractedJson;
    private String lastIntent;

    public MediChatSession(String sessionId, List<MediChatMessage> messages,
                           String extractedJson, String lastIntent) {
        this.sessionId = sessionId;
        this.messages = messages;
        this.extractedJson = extractedJson;
        this.lastIntent = lastIntent;
    }

    public String getSessionId() { return sessionId; }
    public List<MediChatMessage> getMessages() { return messages; }

    public String getExtractedJson() { return extractedJson; }
    public void setExtractedJson(String extractedJson) { this.extractedJson = extractedJson; }

    public String getLastIntent() { return lastIntent; }
    public void setLastIntent(String lastIntent) { this.lastIntent = lastIntent; }
}
