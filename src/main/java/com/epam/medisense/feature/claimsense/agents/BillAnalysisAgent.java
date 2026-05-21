package com.epam.medisense.feature.claimsense.agents;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class BillAnalysisAgent {

    private final ChatClient chatClient;

    @Value("classpath:prompts/claimsense/bill-analysis-agent.st")
    private Resource systemPrompt;

    public BillAnalysisAgent(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultOptions(
                    org.springframework.ai.anthropic.AnthropicChatOptions.builder()
                        .maxTokens(4096)
                        .build()
                )
                .build();
    }

    public String analyze(String billText) {
        return chatClient.prompt()
                .system(s -> s.text(systemPrompt))
                .user(billText)
                .call()
                .content();
    }
}
