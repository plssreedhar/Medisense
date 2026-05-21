package com.epam.medisense.feature.medichat;

import java.time.LocalDateTime;

public class MediChatMessage {

    private final String role;
    private final String content;
    private final String messageType;
    private final LocalDateTime timestamp;

    public MediChatMessage(String role, String content, String messageType, LocalDateTime timestamp) {
        this.role = role;
        this.content = content;
        this.messageType = messageType;
        this.timestamp = timestamp;
    }

    public static MediChatMessage user(String content) {
        return new MediChatMessage("user", content, "text", LocalDateTime.now());
    }

    public static MediChatMessage assistant(String content) {
        return new MediChatMessage("assistant", content, "text", LocalDateTime.now());
    }

    public static MediChatMessage assistantTyped(String content, String type) {
        return new MediChatMessage("assistant", content, type, LocalDateTime.now());
    }

    public String getRole() { return role; }
    public String getContent() { return content; }
    public String getMessageType() { return messageType; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
