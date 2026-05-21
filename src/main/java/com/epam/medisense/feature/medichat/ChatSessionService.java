package com.epam.medisense.feature.medichat;

import com.epam.medisense.domain.ChatMessage;
import com.epam.medisense.domain.ChatSession;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class ChatSessionService {

    private final MediChatOrchestrator orchestrator;
    private final ConversationStateManager stateManager;

    public ChatSessionService(MediChatOrchestrator orchestrator,
                              ConversationStateManager stateManager) {
        this.orchestrator = orchestrator;
        this.stateManager = stateManager;
    }

    public ChatResponse chat(UUID sessionId,
                             String message,
                             MultipartFile file,
                             String language) throws IOException {
        return orchestrator.handle(sessionId, message, file, language);
    }

    public List<ChatSession> getAllSessions() {
        return stateManager.getAllSessions();
    }

    public List<ChatMessage> getSessionHistory(UUID sessionId) {
        return stateManager.getHistory(sessionId);
    }
}
