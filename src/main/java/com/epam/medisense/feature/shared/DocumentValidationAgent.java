package com.epam.medisense.feature.shared;

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
public class DocumentValidationAgent {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public DocumentValidationAgent(ChatClient.Builder chatClientBuilder,
                                   ResourceLoader resourceLoader,
                                   @Value("${prompts.validation}") String promptPath) throws IOException {
        String systemPrompt = FileCopyUtils.copyToString(
                new InputStreamReader(
                        resourceLoader.getResource(promptPath).getInputStream(),
                        StandardCharsets.UTF_8));
        this.chatClient = chatClientBuilder.defaultSystem(systemPrompt).build();
    }

    public ValidationResult validate(String rawText, String patientNameOnBill, String registeredUserName) throws IOException {
        String userMessage = "DOCUMENT TEXT:\n" + rawText
                + (patientNameOnBill != null ? "\n\nPATIENT_ON_BILL: " + patientNameOnBill : "")
                + (registeredUserName != null ? "\nREGISTERED_USER: " + registeredUserName : "");

        String response = chatClient.prompt().user(userMessage).call().content();
        JsonNode jsonNode = objectMapper.readTree(response);

        Boolean patientMatch = jsonNode.has("patient_match") && !jsonNode.get("patient_match").isNull()
                ? jsonNode.get("patient_match").asBoolean()
                : null;

        return new ValidationResult(
                jsonNode.get("is_valid_document").asBoolean(),
                patientMatch,
                jsonNode.get("reason").asText()
        );
    }
}
