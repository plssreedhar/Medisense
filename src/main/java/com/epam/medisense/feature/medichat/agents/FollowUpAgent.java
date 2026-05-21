package com.epam.medisense.feature.medichat.agents;

import com.epam.medisense.feature.medichat.MediChatMessage;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class FollowUpAgent {

    private final ChatClient chatClient;

    public FollowUpAgent(ChatClient.Builder chatClientBuilder,
                         ResourceLoader resourceLoader,
                         @Value("${prompts.medichat.followup}") String promptPath) throws IOException {
        String systemPrompt = FileCopyUtils.copyToString(
                new InputStreamReader(
                        resourceLoader.getResource(promptPath).getInputStream(),
                        StandardCharsets.UTF_8));
        this.chatClient = chatClientBuilder.defaultSystem(systemPrompt).build();
    }

    public String answer(String userMessage, String extractedJson, List<MediChatMessage> history) {
        StringBuilder historyText = new StringBuilder();
        for (MediChatMessage m : history) {
            historyText.append(m.getRole()).append(": ").append(m.getContent()).append("\n");
        }

        String context = "CONVERSATION HISTORY:\n" + historyText
                + "\n\nDOCUMENT CONTEXT:\n"
                + (extractedJson != null ? extractedJson : "No document uploaded yet")
                + "\n\nPATIENT QUESTION: " + userMessage;

        return chatClient.prompt().user(context).call().content();
    }
}
