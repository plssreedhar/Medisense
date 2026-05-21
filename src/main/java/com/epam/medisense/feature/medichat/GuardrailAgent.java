package com.epam.medisense.feature.medichat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class GuardrailAgent {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("classpath:prompts/medichat/guardrail-agent.st")
    private Resource systemPrompt;

    public GuardrailAgent(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public GuardrailResult evaluate(String userMessage) {
        String raw = chatClient.prompt()
                .system(s -> s.text(systemPrompt))
                .user(userMessage)
                .call()
                .content();
        try {
            String cleaned = raw.replaceAll("```json", "").replaceAll("```", "").trim();
            JsonNode node = objectMapper.readTree(cleaned);
            boolean isHealthRelated = node.path("is_health_related").asBoolean(true);
            String intent = node.path("intent").asText("GENERAL_HEALTH");
            double confidence = node.path("confidence").asDouble(1.0);
            return new GuardrailResult(isHealthRelated, intent, confidence);
        } catch (Exception e) {
            return new GuardrailResult(true, "GENERAL_HEALTH", 1.0);
        }
    }

    public record GuardrailResult(boolean isHealthRelated, String intent, double confidence) {}
}
