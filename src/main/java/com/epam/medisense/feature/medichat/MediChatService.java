package com.epam.medisense.feature.medichat;

import com.epam.medisense.feature.claimsense.ClaimSenseService;
import com.epam.medisense.feature.claimsense.ClaimVerdict;
import com.epam.medisense.feature.claimsense.PolicyRule;
import com.epam.medisense.feature.claimsense.PolicyRuleRepository;
import com.epam.medisense.feature.claimsense.agents.BillAnalysisAgent;
import com.epam.medisense.feature.claimsense.agents.ExplanationAgent;
import com.epam.medisense.feature.claimsense.agents.ReasoningAgent;
import com.epam.medisense.feature.medichat.agents.ChatAgent;
import com.epam.medisense.feature.medichat.agents.FollowUpAgent;
import com.epam.medisense.feature.medichat.agents.OrchestratorAgent;
import com.epam.medisense.feature.medichat.agents.OrchestratorResult;
import com.epam.medisense.feature.medisummarize.MediSummarizeResponse;
import com.epam.medisense.feature.medisummarize.MediSummarizeService;
import com.epam.medisense.feature.shared.DocumentValidationAgent;
import com.epam.medisense.util.PdfTextExtractor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class MediChatService {

    private final OrchestratorAgent orchestratorAgent;
    private final FollowUpAgent followUpAgent;
    private final ChatAgent chatAgent;
    private final MediSummarizeService mediSummarizeService;
    private final ClaimSenseService claimSenseService;
    private final PdfTextExtractor pdfTextExtractor;
    private final DocumentValidationAgent documentValidationAgent;
    private final BillAnalysisAgent billAnalysisAgent;
    private final ReasoningAgent reasoningAgent;
    private final ExplanationAgent explanationAgent;
    private final PolicyRuleRepository policyRuleRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ConcurrentHashMap<String, MediChatSession> sessions = new ConcurrentHashMap<>();

    public MediChatService(OrchestratorAgent orchestratorAgent,
                           FollowUpAgent followUpAgent,
                           ChatAgent chatAgent,
                           MediSummarizeService mediSummarizeService,
                           ClaimSenseService claimSenseService,
                           PdfTextExtractor pdfTextExtractor,
                           DocumentValidationAgent documentValidationAgent,
                           BillAnalysisAgent billAnalysisAgent,
                           ReasoningAgent reasoningAgent,
                           ExplanationAgent explanationAgent,
                           PolicyRuleRepository policyRuleRepository) {
        this.orchestratorAgent = orchestratorAgent;
        this.followUpAgent = followUpAgent;
        this.chatAgent = chatAgent;
        this.mediSummarizeService = mediSummarizeService;
        this.claimSenseService = claimSenseService;
        this.pdfTextExtractor = pdfTextExtractor;
        this.documentValidationAgent = documentValidationAgent;
        this.billAnalysisAgent = billAnalysisAgent;
        this.reasoningAgent = reasoningAgent;
        this.explanationAgent = explanationAgent;
        this.policyRuleRepository = policyRuleRepository;
    }

    public MediChatResponse chat(String sessionId, String userMessage,
                                 MultipartFile file, String policyId) throws IOException {

        MediChatSession session = sessions.computeIfAbsent(sessionId,
                id -> new MediChatSession(id, new ArrayList<>(), null, null));

        String messageText = userMessage != null ? userMessage : "";
        session.getMessages().add(MediChatMessage.user(messageText));

        boolean hasDocument = file != null && !file.isEmpty();
        MediChatMessage botMessage;

        try {
            // Extract document preview for orchestrator
            String documentHint = "";
            String fullDocText = null;
            if (hasDocument) {
                fullDocText = pdfTextExtractor.extract(file.getBytes());
                if (fullDocText != null && !fullDocText.isBlank()) {
                    documentHint = fullDocText.length() > 400
                            ? fullDocText.substring(0, 400)
                            : fullDocText;
                }
            }

            OrchestratorResult orchestration = orchestratorAgent.orchestrate(messageText, hasDocument, documentHint);
            String intent = orchestration.getIntent();

            switch (intent) {
                case "SUMMARIZE" -> {
                    MediSummarizeResponse result = mediSummarizeService.summarize(file, "English");
                    session.setExtractedJson(result.getExtractedJson());
                    session.setLastIntent("SUMMARIZE");
                    String content = "📋 **Document Summary**\n\n" + result.getSummary()
                            + "\n\n_You can now ask me questions about your report._";
                    botMessage = MediChatMessage.assistantTyped(content, "summary");
                }

                case "CLAIM" -> {
                    if (policyId == null || policyId.isBlank()) {
                        // Use full doc text we already extracted, or extract now
                        String rawText = fullDocText != null ? fullDocText
                                : pdfTextExtractor.extract(file.getBytes());
                        String billAnalysisJson = billAnalysisAgent.analyze(rawText);
                        session.setExtractedJson(billAnalysisJson);
                        session.setLastIntent("AWAITING_POLICY");
                        String policyList = buildPolicyList();
                        String content = "I can see this is a medical bill. Which policy would you like to check it against?\n"
                                + "Please reply with the **exact policy ID** from the list below:\n" + policyList;
                        botMessage = MediChatMessage.assistant(content);
                    } else {
                        List<ClaimVerdict> verdicts = claimSenseService.analyze(policyId, "Patient", List.of(file), "English");
                        session.setLastIntent("CLAIM");
                        botMessage = MediChatMessage.assistantTyped(formatVerdicts(verdicts), "verdict");
                    }
                }

                case "FOLLOWUP" -> {
                    if ("AWAITING_POLICY".equals(session.getLastIntent())) {
                        String selectedPolicyId = messageText.trim();
                        // Validate the policy exists
                        if (policyRuleRepository.findByPolicyId(selectedPolicyId).isEmpty()) {
                            String policyList = buildPolicyList();
                            String content = "I don't recognise that policy ID. Please choose one of:\n" + policyList;
                            botMessage = MediChatMessage.assistant(content);
                        } else {
                            botMessage = runDeferredClaim(session, selectedPolicyId);
                        }
                    } else {
                        String response = followUpAgent.answer(messageText,
                                session.getExtractedJson(), session.getMessages());
                        botMessage = MediChatMessage.assistant(response);
                    }
                }

                case "CHAT" -> {
                    String response = chatAgent.chat(messageText);
                    botMessage = MediChatMessage.assistant(response);
                }

                case "INVALID" -> {
                    String content = "I'm sorry, I can only process medical documents (lab reports, "
                            + "prescriptions, discharge summaries) or medical bills. "
                            + "Please upload a valid document.";
                    botMessage = MediChatMessage.assistantTyped(content, "error");
                }

                default -> {
                    String response = chatAgent.chat(messageText);
                    botMessage = MediChatMessage.assistant(response);
                }
            }
        } catch (Exception e) {
            String errMsg = "⚠️ I'm temporarily unavailable (service error: " + e.getMessage() + "). Please try again in a moment.";
            botMessage = MediChatMessage.assistant(errMsg);
        }

        session.getMessages().add(botMessage);
        return new MediChatResponse(sessionId, botMessage);
    }

    private String buildPolicyList() {
        List<PolicyRule> policies = policyRuleRepository.findAll();
        if (policies.isEmpty()) {
            return "• No policies currently available.";
        }
        return policies.stream()
                .map(p -> "• **" + p.getPolicyId() + "** — " + p.getName())
                .collect(Collectors.joining("\n"));
    }

    private MediChatMessage runDeferredClaim(MediChatSession session, String policyId) throws IOException {
        String billAnalysisJson = session.getExtractedJson();
        if (billAnalysisJson == null) {
            return MediChatMessage.assistantTyped(
                    "I'm sorry, I no longer have the bill data. Please upload the bill again.", "error");
        }

        String policyRulesJson = policyRuleRepository.findByPolicyId(policyId)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found: " + policyId))
                .getRulesJson();

        String verdictJson = reasoningAgent.reason(policyRulesJson, billAnalysisJson);
        String explanation = explanationAgent.explain(verdictJson);

        JsonNode vNode = parseJson(verdictJson);
        String verdict = vNode.path("verdict").asText(vNode.path("overall_verdict").asText("EXCLUDED"));

        session.setExtractedJson(verdictJson);
        session.setLastIntent("CLAIM");

        String content = "🔍 **Claim Analysis**\n\n[" + verdict + "]\n" + explanation;
        return MediChatMessage.assistantTyped(content, "verdict");
    }

    private String formatVerdicts(List<ClaimVerdict> verdicts) {
        StringBuilder sb = new StringBuilder("🔍 **Claim Analysis**\n\n");
        for (ClaimVerdict v : verdicts) {
            sb.append("[").append(v.verdict()).append("] ")
              .append(v.fileName()).append("\n")
              .append(v.explanation()).append("\n\n");
        }
        return sb.toString().trim();
    }

    private JsonNode parseJson(String raw) throws IOException {
        String cleaned = raw == null ? "{}" : raw.trim()
                .replaceAll("(?s)^```json\\s*", "")
                .replaceAll("(?s)^```\\s*", "")
                .replaceAll("(?s)```\\s*$", "");
        return objectMapper.readTree(cleaned);
    }
}
