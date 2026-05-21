package com.epam.medisense.feature.claimsense.agents;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class ExplanationAgent {

    private final ChatClient chatClient;

    @Value("classpath:prompts/claimsense/explanation-agent.st")
    private Resource systemPrompt;

    public ExplanationAgent(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String explain(String verdictJson) {
        return chatClient.prompt()
                .system(s -> s.text(systemPrompt))
                .user(verdictJson)
                .call()
                .content();
    }
}
