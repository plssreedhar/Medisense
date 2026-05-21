package com.epam.medisense.feature.medichat;

import com.epam.medisense.domain.ChatMessage;
import com.epam.medisense.domain.ChatMessageRepository;
import com.epam.medisense.domain.ChatSession;
import com.epam.medisense.domain.ChatSessionRepository;
import com.epam.medisense.domain.ChatSessionState;
import com.epam.medisense.domain.ChatSessionStateRepository;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class ConversationStateManager {

    private final ChatSessionRepository sessionRepo;
    private final ChatMessageRepository messageRepo;
    private final ChatSessionStateRepository stateRepo;

    public ConversationStateManager(ChatSessionRepository sessionRepo,
                                    ChatMessageRepository messageRepo,
                                    ChatSessionStateRepository stateRepo) {
        this.sessionRepo = sessionRepo;
        this.messageRepo = messageRepo;
        this.stateRepo = stateRepo;
    }

    public ChatSession createSession(String patientName) {
        ChatSession session = new ChatSession();
        session.setPatientName(patientName);
        session.setSessionName("Chat " + OffsetDateTime.now().toLocalDate());
        session.setUpdatedAt(OffsetDateTime.now());
        return sessionRepo.save(session);
    }

    public ChatSession getOrCreateSession(UUID sessionId, String patientName) {
        if (sessionId != null) {
            return sessionRepo.findById(sessionId)
                    .orElseGet(() -> createSession(patientName));
        }
        return createSession(patientName);
    }

    public ChatSessionState getOrCreateState(UUID sessionId) {
        return stateRepo.findById(sessionId).orElseGet(() -> {
            ChatSessionState state = new ChatSessionState();
            state.setSessionId(sessionId);
            state.setUpdatedAt(OffsetDateTime.now());
            return stateRepo.save(state);
        });
    }

    public ChatSessionState saveState(ChatSessionState state) {
        state.setUpdatedAt(OffsetDateTime.now());
        return stateRepo.save(state);
    }

    public void persistMessage(UUID sessionId, String role, String content, String contentType, String attachedFile) {
        ChatMessage msg = new ChatMessage();
        msg.setSessionId(sessionId);
        msg.setRole(role);
        msg.setContent(content);
        msg.setContentType(contentType);
        msg.setAttachedFile(attachedFile);
        messageRepo.save(msg);

        sessionRepo.findById(sessionId).ifPresent(s -> {
            s.setUpdatedAt(OffsetDateTime.now());
            sessionRepo.save(s);
        });
    }

    public List<ChatMessage> getHistory(UUID sessionId) {
        return messageRepo.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }

    public List<ChatSession> getAllSessions() {
        return sessionRepo.findAllByOrderByUpdatedAtDesc();
    }
}
