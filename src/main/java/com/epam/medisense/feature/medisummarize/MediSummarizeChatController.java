package com.epam.medisense.feature.medisummarize;

import com.epam.medisense.feature.medichat.TranslationAgent;
import com.epam.medisense.feature.medisummarize.agents.MediChatAgent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/summarize")
@CrossOrigin
public class MediSummarizeChatController {

    private final MediChatAgent mediChatAgent;
    private final TranslationAgent translationAgent;

    public MediSummarizeChatController(MediChatAgent mediChatAgent, TranslationAgent translationAgent) {
        this.mediChatAgent = mediChatAgent;
        this.translationAgent = translationAgent;
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody ChatRequest request) {
        try {
            String response = mediChatAgent.chat(request.extractedJson(), request.message());
            String lang = request.language() != null && !request.language().isBlank()
                    ? request.language() : "English";
            String translated = translationAgent.translate(response, lang);
            return ResponseEntity.ok(Map.of("response", translated));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body("An error occurred: " + ex.getMessage());
        }
    }

    record ChatRequest(String message, String extractedJson, String language) {}
}
