package com.epam.medisense.feature.medichat;

import com.epam.medisense.domain.ChatMessage;
import com.epam.medisense.domain.ChatSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatSessionService chatSessionService;

    public ChatController(ChatSessionService chatSessionService) {
        this.chatSessionService = chatSessionService;
    }

    /**
     * POST /api/chat/message
     * Main chat endpoint. Accepts text + optional PDF attachment.
     *
     * Form params:
     *   sessionId  (optional UUID — omit to start a new session)
     *   message    (optional text — can be null if file is the only input)
     *   file       (optional multipart PDF)
     *   language   (optional — defaults to English)
     */
    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(
            @RequestParam(required = false) UUID sessionId,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false, defaultValue = "English") String language) {

        if ((message == null || message.isBlank()) && (file == null || file.isEmpty())) {
            return ResponseEntity.badRequest().build();
        }

        try {
            ChatResponse response = chatSessionService.chat(sessionId, message, file, language);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            UUID sid = sessionId != null ? sessionId : UUID.randomUUID();
            return ResponseEntity.ok(new ChatResponse(
                    sid, "assistant",
                    "⚠️ I encountered an error processing your request. Please try again.",
                    "text", null));
        }
    }

    /**
     * GET /api/chat/sessions
     * Returns all sessions ordered by most recently updated.
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSession>> getSessions() {
        return ResponseEntity.ok(chatSessionService.getAllSessions());
    }

    /**
     * GET /api/chat/sessions/{sessionId}/history
     * Returns all messages in a session in chronological order.
     */
    @GetMapping("/sessions/{sessionId}/history")
    public ResponseEntity<List<ChatMessage>> getHistory(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(chatSessionService.getSessionHistory(sessionId));
    }

    /**
     * GET /api/chat/languages
     * Returns the list of supported languages for the frontend dropdown.
     */
    @GetMapping("/languages")
    public ResponseEntity<List<String>> getSupportedLanguages() {
        List<String> languages = java.util.Arrays.stream(SupportedLanguage.values())
                .map(SupportedLanguage::getDisplayName)
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(languages);
    }

    /**
     * GET /api/chat/insurers
     * Returns the list of supported insurers and their tiers for the frontend.
     */
    @GetMapping("/insurers")
    public ResponseEntity<List<java.util.Map<String, Object>>> getSupportedInsurers() {
        List<java.util.Map<String, Object>> insurers = List.of(
            java.util.Map.of("id", "medishield",  "name", "MediShield",
                             "tiers", List.of("silver", "gold")),
            java.util.Map.of("id", "starhealth",  "name", "Star Health",
                             "tiers", List.of("silver", "gold")),
            java.util.Map.of("id", "hdfcergo",    "name", "HDFC Ergo",
                             "tiers", List.of("optima", "myhealth")),
            java.util.Map.of("id", "nivabupa",    "name", "Niva Bupa",
                             "tiers", List.of("reassure", "companion"))
        );
        return ResponseEntity.ok(insurers);
    }
}
