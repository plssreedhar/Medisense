package com.epam.medisense.feature.medichat;

import com.epam.medisense.domain.ChatSessionState;
import com.epam.medisense.feature.claimsense.ClaimVerdict;
import com.epam.medisense.feature.claimsense.LineItemVerdict;
import com.epam.medisense.feature.claimsense.agents.BillAnalysisAgent;
import com.epam.medisense.feature.claimsense.agents.ExplanationAgent;
import com.epam.medisense.feature.claimsense.agents.PolicyAgent;
import com.epam.medisense.feature.claimsense.agents.ReasoningAgent;
import com.epam.medisense.feature.medisummarize.agents.ExtractionAgent;
import com.epam.medisense.feature.medisummarize.agents.SummarizationAgent;
import com.epam.medisense.util.PdfTextExtractor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class MediChatOrchestrator {

    private final ConversationStateManager stateManager;
    private final GuardrailAgent guardrailAgent;
    private final TranslationAgent translationAgent;
    private final PdfTextExtractor pdfTextExtractor;
    private final ExtractionAgent extractionAgent;
    private final SummarizationAgent summarizationAgent;
    private final PolicyAgent policyAgent;
    private final BillAnalysisAgent billAnalysisAgent;
    private final ReasoningAgent reasoningAgent;
    private final ExplanationAgent explanationAgent;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final java.util.Map<String, String> INSURER_ID_MAP = java.util.Map.of(
        "star health", "starhealth",
        "star",        "starhealth",
        "hdfc ergo",   "hdfcergo",
        "hdfc",        "hdfcergo",
        "niva bupa",   "nivabupa",
        "niva",        "nivabupa",
        "bupa",        "nivabupa",
        "medishield",  "medishield"
    );

    private static final java.util.Map<String, String> TIER_ID_MAP = java.util.Map.of(
        "silver", "silver",
        "gold", "gold",
        "optima", "optima",
        "optima secure", "optima",
        "my:health", "myhealth",
        "myhealth", "myhealth",
        "reassure", "reassure",
        "companion", "companion",
        "health companion", "companion"
    );

    private static final java.util.Map<String, List<String>> VALID_TIERS = java.util.Map.of(
        "starhealth", List.of("silver", "gold"),
        "hdfcergo",   List.of("optima", "myhealth"),
        "nivabupa",   List.of("reassure", "companion")
    );

    private static final java.util.Map<String, String> TIER_DISPLAY = java.util.Map.of(
        "silver", "Silver",
        "gold", "Gold",
        "optima", "Optima",
        "myhealth", "MyHealth",
        "reassure", "ReAssure",
        "companion", "Companion"
    );

    public MediChatOrchestrator(ConversationStateManager stateManager,
                                 GuardrailAgent guardrailAgent,
                                 TranslationAgent translationAgent,
                                 PdfTextExtractor pdfTextExtractor,
                                 ExtractionAgent extractionAgent,
                                 SummarizationAgent summarizationAgent,
                                 PolicyAgent policyAgent,
                                 BillAnalysisAgent billAnalysisAgent,
                                 ReasoningAgent reasoningAgent,
                                 ExplanationAgent explanationAgent) {
        this.stateManager = stateManager;
        this.guardrailAgent = guardrailAgent;
        this.translationAgent = translationAgent;
        this.pdfTextExtractor = pdfTextExtractor;
        this.extractionAgent = extractionAgent;
        this.summarizationAgent = summarizationAgent;
        this.policyAgent = policyAgent;
        this.billAnalysisAgent = billAnalysisAgent;
        this.reasoningAgent = reasoningAgent;
        this.explanationAgent = explanationAgent;
    }

    public ChatResponse handle(UUID sessionId,
                               String userMessage,
                               MultipartFile file,
                               String language) throws IOException {

        var session = stateManager.getOrCreateSession(sessionId, null);
        UUID sid = session.getId();
        ChatSessionState state = stateManager.getOrCreateState(sid);

        String attachedFileName = (file != null && !file.isEmpty()) ? file.getOriginalFilename() : null;
        stateManager.persistMessage(sid, "user",
                userMessage != null ? userMessage : "(file upload)",
                "text", attachedFileName);

        String responseText;
        String contentType = "text";
        List<ClaimVerdict> verdicts = null;

        if (file != null && !file.isEmpty()) {
            String rawText = pdfTextExtractor.extract(file.getBytes());
            if (rawText == null || rawText.isBlank()) {
                responseText = "I wasn't able to read text from that PDF. Please make sure it's a typed (not scanned) document and try again.";
            } else {
                state.setRawText(rawText);

                String extractedJson = extractionAgent.extract(rawText);
                String docType = detectDocType(extractedJson);
                state.setExtractedJson(extractedJson);
                state.setDocType(docType);

                if ("HOSPITAL_BILL".equals(docType) || "MIXED".equals(docType)) {
                    if (state.getInsurerId() == null || state.getPolicyTier() == null) {
                        state.setAwaiting("INSURER");
                        stateManager.saveState(state);
                        responseText = "I've read your hospital bill. To check what's claimable, I need to know your insurance details.\n\nWhich insurance company are you with? (e.g. Star Health, HDFC Ergo, Niva Bupa)";
                    } else {
                        ChatResponse claimResponse = runClaimPipeline(sid, state, file.getOriginalFilename(), language);
                        stateManager.persistMessage(sid, "assistant",
                                claimResponse.message(), claimResponse.contentType(), null);
                        return claimResponse;
                    }
                } else {
                    String summary = summarizationAgent.summarize(extractedJson);
                    state.setAwaiting(null);
                    stateManager.saveState(state);
                    contentType = "summary";
                    String translated = translationAgent.translate(summary, language);
                    responseText = translated + "\n\n---\nWould you like me to check if any of this is covered under your insurance? Tell me your insurer and tier (e.g. \"Star Health Gold\").";
                }
            }
        } else {
            String msg = userMessage != null ? userMessage.trim() : "";

            if ("INSURER".equals(state.getAwaiting())) {
                String insurerId = resolveInsurerId(msg);
                if (insurerId != null) {
                    if ("medishield".equals(insurerId)) {
                        responseText = "I recognise MediShield, but I don't have policy data for it yet. We currently support Star Health, HDFC Ergo, and Niva Bupa. Which one are you with?";
                        // stay in INSURER awaiting state
                    } else {
                        state.setInsurerId(insurerId);
                        state.setAwaiting("TIER");
                        stateManager.saveState(state);
                        List<String> validTiers = VALID_TIERS.getOrDefault(insurerId, List.of());
                        String tierOptions = validTiers.stream()
                                .map(t -> TIER_DISPLAY.getOrDefault(t, t))
                                .collect(Collectors.joining(", "));
                        responseText = "Got it! Which tier or plan are you on? (" + tierOptions + ")";
                    }
                } else {
                    responseText = "I didn't recognise that insurer. We support Star Health, HDFC Ergo, and Niva Bupa. Which one are you with?";
                }
            } else if ("TIER".equals(state.getAwaiting())) {
                String tier = resolveTier(msg);
                if (tier != null) {
                    List<String> validTiers = VALID_TIERS.getOrDefault(state.getInsurerId(), List.of());
                    if (!validTiers.contains(tier)) {
                        String validDisplay = validTiers.stream()
                                .map(t -> TIER_DISPLAY.getOrDefault(t, t))
                                .collect(Collectors.joining(", "));
                        responseText = "That tier isn't available under your insurer. Please choose from: " + validDisplay;
                    } else {
                        state.setPolicyTier(tier);
                        state.setAwaiting(null);
                        stateManager.saveState(state);

                        if (("HOSPITAL_BILL".equals(state.getDocType()) || "MIXED".equals(state.getDocType()))
                                && state.getExtractedJson() != null) {
                            ChatResponse claimResponse = runClaimPipelineFromJson(sid, state, language);
                            stateManager.persistMessage(sid, "assistant",
                                    claimResponse.message(), claimResponse.contentType(), null);
                            return claimResponse;
                        } else {
                            responseText = "Perfect — noted. Please upload your hospital bill and I'll check what's claimable.";
                        }
                    }
                } else {
                    List<String> validTiers = VALID_TIERS.getOrDefault(state.getInsurerId(), List.of());
                    String validDisplay = validTiers.isEmpty()
                            ? "Silver, Gold, Optima, ReAssure, Companion"
                            : validTiers.stream().map(t -> TIER_DISPLAY.getOrDefault(t, t)).collect(Collectors.joining(", "));
                    responseText = "I didn't recognise that tier. Could you try again? Available options: " + validDisplay;
                }
            } else {
                GuardrailAgent.GuardrailResult guardrail = guardrailAgent.evaluate(msg);

                if (!guardrail.isHealthRelated()) {
                    responseText = "I'm MediSense, a healthcare assistant. I can help you understand medical reports, check hospital bills against your insurance, or answer questions about your health coverage. How can I help?";
                } else if ("GREETING".equals(guardrail.intent())) {
                    String name = session.getPatientName() != null ? ", " + session.getPatientName() : "";
                    responseText = "Hello" + name + "! I'm MediSense. You can upload a medical report or hospital bill, or ask me about your insurance coverage. What would you like to do?";
                } else if ("COVERAGE_QUESTION".equals(guardrail.intent()) || "POLICY_QUESTION".equals(guardrail.intent())) {
                    if (state.getInsurerId() != null && state.getPolicyTier() != null) {
                        String policyId = state.getInsurerId() + "-" + state.getPolicyTier();
                        String policyRules = policyAgent.fetchRules(policyId);
                        responseText = answerPolicyQuestion(msg, policyRules, language);
                    } else {
                        state.setAwaiting("INSURER");
                        stateManager.saveState(state);
                        responseText = "I'd be happy to answer that! Which insurance company are you with? (e.g. Star Health, HDFC Ergo, Niva Bupa)";
                    }
                } else {
                    // GENERAL_HEALTH and other health intents — answer via LLM
                    String context = "You are MediSense, a helpful healthcare assistant. Answer the following health question clearly and concisely:\n\n" + msg;
                    responseText = explanationAgent.explain(context);
                }
            }
        }

        String finalResponse = translationAgent.translate(responseText, language);
        stateManager.persistMessage(sid, "assistant", finalResponse, contentType, null);

        return new ChatResponse(sid, "assistant", finalResponse, contentType, verdicts);
    }

    private ChatResponse runClaimPipeline(UUID sessionId, ChatSessionState state,
                                           String fileName, String language) throws IOException {
        String policyId = state.getInsurerId() + "-" + state.getPolicyTier();
        String policyRules = policyAgent.fetchRules(policyId);
        String billJson = billAnalysisAgent.analyze(rawTextFromState(state));
        String verdictJson = reasoningAgent.reason(policyRules, billJson);
        String explanation = explanationAgent.explain(verdictJson);

        ClaimVerdict verdict = buildVerdict(fileName, verdictJson, explanation);
        String translated = translationAgent.translate(explanation, language);

        return new ChatResponse(sessionId, "assistant", translated, "verdict",
                Collections.singletonList(verdict));
    }

    private ChatResponse runClaimPipelineFromJson(UUID sessionId, ChatSessionState state,
                                                   String language) throws IOException {
        String policyId = state.getInsurerId() + "-" + state.getPolicyTier();
        String policyRules = policyAgent.fetchRules(policyId);
        String billJson = billAnalysisAgent.analyze(rawTextFromState(state));
        String verdictJson = reasoningAgent.reason(policyRules, billJson);
        String explanation = explanationAgent.explain(verdictJson);

        ClaimVerdict verdict = buildVerdict("uploaded-bill.pdf", verdictJson, explanation);
        String translated = translationAgent.translate(explanation, language);

        return new ChatResponse(sessionId, "assistant", translated, "verdict",
                Collections.singletonList(verdict));
    }

    private String rawTextFromState(ChatSessionState state) {
        return state.getRawText() != null ? state.getRawText() : "";
    }

    private ClaimVerdict buildVerdict(String fileName, String verdictJson, String explanation) {
        try {
            String cleaned = verdictJson.replaceAll("```json", "").replaceAll("```", "").trim();
            JsonNode node = objectMapper.readTree(cleaned);
            double total = node.path("total_amount").asDouble(0.0);
            double claimable = node.path("claimable_amount").asDouble(0.0);
            double confidence = node.path("confidence").asDouble(0.0);
            String overallVerdict = node.path("overall_verdict").asText("EXCLUDED");
            String ruleApplied = node.path("rule_applied").asText("");
            List<LineItemVerdict> items = new ArrayList<>();
            JsonNode itemsNode = node.path("line_items");
            if (itemsNode.isArray()) {
                for (JsonNode item : itemsNode) {
                    items.add(new LineItemVerdict(
                            item.path("description").asText(),
                            item.path("amount").asDouble(0.0),
                            item.path("verdict").asText("EXCLUDED"),
                            item.path("reason").asText("")
                    ));
                }
            }
            return new ClaimVerdict(fileName, overallVerdict, confidence, ruleApplied,
                    explanation, total, claimable, items);
        } catch (Exception e) {
            return new ClaimVerdict(fileName, "EXCLUDED", 0.0, "parse-error",
                    explanation, 0.0, 0.0, Collections.emptyList());
        }
    }

    private String detectDocType(String extractedJson) {
        try {
            JsonNode node = objectMapper.readTree(extractedJson);
            String docType = node.path("document_type").asText("UNKNOWN");
            if ("LAB_REPORT".equals(docType) || "PRESCRIPTION".equals(docType)
                    || "DISCHARGE_SUMMARY".equals(docType)) {
                return "MEDICAL_REPORT";
            }
            if ("HOSPITAL_BILL".equals(docType) || "INVOICE".equals(docType)) {
                return "HOSPITAL_BILL";
            }
            return docType;
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    private String resolveInsurerId(String msg) {
        String lower = msg.toLowerCase();
        for (java.util.Map.Entry<String, String> entry : INSURER_ID_MAP.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String resolveTier(String msg) {
        String lower = msg.toLowerCase();
        for (java.util.Map.Entry<String, String> entry : TIER_ID_MAP.entrySet()) {
            if (lower.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String answerPolicyQuestion(String question, String policyRules, String language) {
        String context = "POLICY RULES:\n" + policyRules + "\n\nPATIENT QUESTION:\n" + question;
        String answer = explanationAgent.explain(context);
        return translationAgent.translate(answer, language);
    }
}
