package com.epam.medisense.feature.medichat;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class TranslationAgent {

    private final ChatClient chatClient;

    @Value("classpath:prompts/medichat/translation-agent.st")
    private Resource systemPrompt;

    public TranslationAgent(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    /**
     * Translates text into the target language.
     * If language is null, blank, or "English", returns the original text unchanged.
     */
    public String translate(String text, String language) {
        if (language == null || language.isBlank() || language.equalsIgnoreCase("English")) {
            return text;
        }
        String userMessage = "TARGET LANGUAGE: " + language + "\n\nTEXT TO TRANSLATE:\n" + text;
        return chatClient.prompt()
                .system(s -> s.text(systemPrompt))
                .user(userMessage)
                .call()
                .content();
    }
}
