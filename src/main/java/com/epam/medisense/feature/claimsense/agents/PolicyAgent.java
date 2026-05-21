package com.epam.medisense.feature.claimsense.agents;

import com.epam.medisense.feature.claimsense.PolicyRule;
import com.epam.medisense.feature.claimsense.PolicyRuleRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class PolicyAgent {

    private final ChatClient chatClient;
    private final PolicyRuleRepository policyRuleRepository;

    public PolicyAgent(ChatClient.Builder chatClientBuilder,
                       ResourceLoader resourceLoader,
                       @Value("${prompts.policy}") String promptPath,
                       PolicyRuleRepository policyRuleRepository) throws IOException {
        String systemPrompt = FileCopyUtils.copyToString(
                new InputStreamReader(
                        resourceLoader.getResource(promptPath).getInputStream(),
                        StandardCharsets.UTF_8));
        this.chatClient = chatClientBuilder.defaultSystem(systemPrompt).build();
        this.policyRuleRepository = policyRuleRepository;
    }

    public String fetchRules(String policyId) {
        PolicyRule policyRule = policyRuleRepository.findByPolicyId(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found: " + policyId));
        return chatClient.prompt().user(policyRule.getRulesJson()).call().content();
    }
}
