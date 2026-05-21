package com.epam.medisense.feature.medisummarize.agents;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class MediChatAgent {

    private final ChatClient chatClient;

    public MediChatAgent(ChatClient.Builder chatClientBuilder,
                         ResourceLoader resourceLoader,
                         @Value("${prompts.chat}") String promptPath) throws IOException {
        String systemPrompt = FileCopyUtils.copyToString(
                new InputStreamReader(
                        resourceLoader.getResource(promptPath).getInputStream(),
                        StandardCharsets.UTF_8));
        this.chatClient = chatClientBuilder.defaultSystem(systemPrompt).build();
    }

    public String chat(String extractedJson, String message) {
        String userMessage = "REPORT DATA:\n" + extractedJson + "\n\nUSER QUESTION:\n" + message;
        return chatClient.prompt().user(userMessage).call().content();
    }
}
