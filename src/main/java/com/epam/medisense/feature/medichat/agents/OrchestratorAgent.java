package com.epam.medisense.feature.medichat.agents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class OrchestratorAgent {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OrchestratorAgent(ChatClient.Builder chatClientBuilder,
                             ResourceLoader resourceLoader,
                             @Value("${prompts.medichat.orchestrator}") String promptPath) throws IOException {
        String systemPrompt = FileCopyUtils.copyToString(
                new InputStreamReader(
                        resourceLoader.getResource(promptPath).getInputStream(),
                        StandardCharsets.UTF_8));
        this.chatClient = chatClientBuilder.defaultSystem(systemPrompt).build();
    }

    public OrchestratorResult orchestrate(String userMessage, boolean hasDocument, String documentHint) throws IOException {
        String input = "USER MESSAGE: " + userMessage + "\nHAS_DOCUMENT: " + hasDocument
                + (documentHint != null && !documentHint.isBlank()
                    ? "\nDOCUMENT PREVIEW (first 400 chars):\n" + documentHint
                    : "");
        String raw = chatClient.prompt().user(input).call().content();
        JsonNode node = objectMapper.readTree(stripFences(raw));
        return new OrchestratorResult(
                node.path("intent").asText("FOLLOWUP"),
                node.path("message").asText("Processing your request..."));
    }

    private String stripFences(String raw) {
        if (raw == null) return "{}";
        return raw.trim()
                .replaceAll("(?s)^```json\\s*", "")
                .replaceAll("(?s)^```\\s*", "")
                .replaceAll("(?s)```\\s*$", "");
    }
}
