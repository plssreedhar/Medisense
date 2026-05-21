package com.epam.medisense.feature.claimsense.agents;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class ReasoningAgent {

    private final ChatClient chatClient;

    @Value("classpath:prompts/claimsense/reasoning-agent.st")
    private Resource systemPrompt;

    public ReasoningAgent(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultOptions(
                    org.springframework.ai.anthropic.AnthropicChatOptions.builder()
                        .maxTokens(4096)
                        .build()
                )
                .build();
    }

    public String reason(String policyRulesJson, String billAnalysisJson) {
        String userMessage = "POLICY RULES:\n" + policyRulesJson +
                             "\n\nBILL ANALYSIS:\n" + billAnalysisJson;
        return chatClient.prompt()
                .system(s -> s.text(systemPrompt))
                .user(userMessage)
                .call()
                .content();
    }
}
