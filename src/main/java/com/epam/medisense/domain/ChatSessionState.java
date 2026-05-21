package com.epam.medisense.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_session_state")
public class ChatSessionState {

    @Id
    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "doc_type")
    private String docType; // MEDICAL_REPORT | HOSPITAL_BILL | UNKNOWN

    @Column(name = "extracted_json", columnDefinition = "TEXT")
    private String extractedJson;

    @Column(name = "insurer_id")
    private String insurerId;

    @Column(name = "policy_tier")
    private String policyTier;

    @Column(name = "awaiting")
    private String awaiting; // INSURER | TIER | null (nothing pending)

    @Column(name = "raw_text", columnDefinition = "TEXT")
    private String rawText;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public ChatSessionState() {}

    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }
    public String getDocType() { return docType; }
    public void setDocType(String docType) { this.docType = docType; }
    public String getExtractedJson() { return extractedJson; }
    public void setExtractedJson(String extractedJson) { this.extractedJson = extractedJson; }
    public String getInsurerId() { return insurerId; }
    public void setInsurerId(String insurerId) { this.insurerId = insurerId; }
    public String getPolicyTier() { return policyTier; }
    public void setPolicyTier(String policyTier) { this.policyTier = policyTier; }
    public String getAwaiting() { return awaiting; }
    public void setAwaiting(String awaiting) { this.awaiting = awaiting; }
    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
