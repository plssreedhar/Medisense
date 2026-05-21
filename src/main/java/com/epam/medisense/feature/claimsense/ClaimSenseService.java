package com.epam.medisense.feature.claimsense;

import com.epam.medisense.feature.claimsense.agents.BillAnalysisAgent;
import com.epam.medisense.feature.claimsense.agents.ExplanationAgent;
import com.epam.medisense.feature.claimsense.agents.ReasoningAgent;
import com.epam.medisense.feature.medichat.TranslationAgent;
import com.epam.medisense.feature.shared.DocumentValidationAgent;
import com.epam.medisense.feature.shared.ValidationResult;
import com.epam.medisense.storage.StorageService;
import com.epam.medisense.util.PdfTextExtractor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ClaimSenseService {

    private final PolicyRuleRepository policyRuleRepository;
    private final BillAnalysisAgent billAnalysisAgent;
    private final ReasoningAgent reasoningAgent;
    private final ExplanationAgent explanationAgent;
    private final DocumentValidationAgent documentValidationAgent;
    private final PdfTextExtractor pdfTextExtractor;
    private final StorageService storageService;
    private final ClaimResultRepository claimResultRepository;
    private final TranslationAgent translationAgent;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ClaimSenseService(PolicyRuleRepository policyRuleRepository,
                             BillAnalysisAgent billAnalysisAgent,
                             ReasoningAgent reasoningAgent,
                             ExplanationAgent explanationAgent,
                             DocumentValidationAgent documentValidationAgent,
                             PdfTextExtractor pdfTextExtractor,
                             StorageService storageService,
                             ClaimResultRepository claimResultRepository,
                             TranslationAgent translationAgent) {
        this.policyRuleRepository = policyRuleRepository;
        this.billAnalysisAgent = billAnalysisAgent;
        this.reasoningAgent = reasoningAgent;
        this.explanationAgent = explanationAgent;
        this.documentValidationAgent = documentValidationAgent;
        this.pdfTextExtractor = pdfTextExtractor;
        this.storageService = storageService;
        this.claimResultRepository = claimResultRepository;
        this.translationAgent = translationAgent;
    }

    public List<ClaimVerdict> analyze(String policyId, String registeredUserName,
                                      List<MultipartFile> bills, String language) throws IOException {
        List<ClaimVerdict> results = new ArrayList<>();
        String sessionId = java.util.UUID.randomUUID().toString();

        for (MultipartFile bill : bills) {
            String fileName = bill.getOriginalFilename();

            // Step 1: Extract text
            String rawText = pdfTextExtractor.extract(bill.getBytes());
            if (rawText == null || rawText.isBlank()) {
                throw new IllegalArgumentException("Could not extract text from: " + fileName);
            }

            // Step 2: Analyse bill to get patient name
            String billAnalysisJson = billAnalysisAgent.analyze(rawText);
            JsonNode billNode = objectMapper.readTree(stripMarkdownFences(billAnalysisJson));
            String patientNameOnBill = billNode.has("patient_name") && !billNode.get("patient_name").isNull()
                    ? billNode.get("patient_name").asText()
                    : "Unknown";

            // Step 3: Validate document and patient identity
            ValidationResult vr = documentValidationAgent.validate(rawText, patientNameOnBill, registeredUserName);

            // Step 4: Invalid document
            if (!vr.isValidDocument()) {
                String invalidMsg = translationAgent.translate(
                        "This does not appear to be a medical bill. " + vr.getReason(), language);
                results.add(new ClaimVerdict(fileName, "EXCLUDED", 0.0, null,
                        invalidMsg, 0.0, 0.0, Collections.emptyList()));
                continue;
            }

            // Step 5: Patient mismatch
            if (vr.getPatientMatch() != null && !vr.getPatientMatch()) {
                String mismatchMsg = translationAgent.translate(
                        "The patient name on this bill (" + patientNameOnBill + ") does not match the registered user ("
                                + registeredUserName + "). This bill cannot be processed.", language);
                results.add(new ClaimVerdict(fileName, "EXCLUDED", 0.0, null,
                        mismatchMsg, 0.0, 0.0, Collections.emptyList()));
                continue;
            }

            // Step 6: Store file
            String localPath = storageService.store(fileName, bill.getBytes());

            // Step 7: Fetch policy rules directly from DB
            PolicyRule policyRule = policyRuleRepository.findByPolicyId(policyId)
                    .orElseThrow(() -> new IllegalArgumentException("Policy not found: " + policyId));
            String policyRulesJson = policyRule.getRulesJson();

            // Steps 8-12: Adjudicate, explain, parse, persist, collect
            try {
                // Step 8: Adjudicate
                String verdictJson = reasoningAgent.reason(policyRulesJson, billAnalysisJson);

                // Step 9: Patient-facing explanation
                String explanation = explanationAgent.explain(verdictJson);
                explanation = translationAgent.translate(explanation, language);

                // Step 10: Parse verdict fields
                JsonNode verdictNode = objectMapper.readTree(stripMarkdownFences(verdictJson));
                double totalAmount = verdictNode.path("total_amount").asDouble(0.0);
                double claimableAmount = verdictNode.path("claimable_amount").asDouble(0.0);
                double confidence = verdictNode.path("confidence").asDouble(0.0);
                String overallVerdict = verdictNode.path("overall_verdict").asText("EXCLUDED");
                String ruleApplied = verdictNode.path("rule_applied").asText("");

                List<LineItemVerdict> lineItems = new ArrayList<>();
                JsonNode itemsNode = verdictNode.path("line_items");
                if (itemsNode.isArray()) {
                    for (JsonNode item : itemsNode) {
                        lineItems.add(new LineItemVerdict(
                            item.path("description").asText(),
                            item.path("amount").asDouble(0.0),
                            item.path("verdict").asText("EXCLUDED"),
                            item.path("reason").asText("")
                        ));
                    }
                }

                // Step 11: Persist result
                ClaimResult claimResult = new ClaimResult(
                        sessionId, policyId, fileName, localPath,
                        patientNameOnBill, overallVerdict, BigDecimal.valueOf(confidence),
                        ruleApplied, explanation, verdictJson);
                claimResult.setLineItems(verdictJson);
                claimResult.setClaimableAmount(BigDecimal.valueOf(claimableAmount));
                claimResult.setTotalAmount(BigDecimal.valueOf(totalAmount));
                claimResultRepository.save(claimResult);

                // Step 12: Add to results
                results.add(new ClaimVerdict(
                        bill.getOriginalFilename(),
                        overallVerdict,
                        confidence,
                        ruleApplied,
                        explanation,
                        totalAmount,
                        claimableAmount,
                        lineItems));
            } catch (Exception e) {
                results.add(new ClaimVerdict(
                        fileName,
                        "EXCLUDED",
                        0.0,
                        "parse-error",
                        "The bill could not be processed automatically. Please contact your insurer directly.",
                        0.0,
                        0.0,
                        Collections.emptyList()));
            }
        }

        return results;
    }

    private String stripMarkdownFences(String raw) {
        if (raw == null) return null;
        return raw.trim()
                .replaceAll("(?s)^```json\\s*", "")
                .replaceAll("(?s)^```\\s*", "")
                .replaceAll("(?s)```\\s*$", "");
    }

}
