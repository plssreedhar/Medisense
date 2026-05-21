package com.epam.medisense.feature.claimsense;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "claim_results")
public class ClaimResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "policy_id")
    private String policyId;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "local_path")
    private String localPath;

    @Column(name = "patient_name_on_bill")
    private String patientNameOnBill;

    @Column(name = "verdict")
    private String verdict;

    @Column(name = "confidence")
    private BigDecimal confidence;

    @Column(name = "rule_applied")
    private String ruleApplied;

    @Column(name = "reason")
    private String reason;

    @Column(name = "raw_llm_output")
    private String rawLlmOutput;

    @Column(name = "line_items", columnDefinition = "TEXT")
    private String lineItems;

    @Column(name = "claimable_amount", precision = 12, scale = 2)
    private java.math.BigDecimal claimableAmount;

    @Column(name = "total_amount", precision = 12, scale = 2)
    private java.math.BigDecimal totalAmount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public ClaimResult() {}

    public ClaimResult(String sessionId, String policyId, String fileName, String localPath,
                       String patientNameOnBill, String verdict, BigDecimal confidence,
                       String ruleApplied, String reason, String rawLlmOutput) {
        this.sessionId = sessionId;
        this.policyId = policyId;
        this.fileName = fileName;
        this.localPath = localPath;
        this.patientNameOnBill = patientNameOnBill;
        this.verdict = verdict;
        this.confidence = confidence;
        this.ruleApplied = ruleApplied;
        this.reason = reason;
        this.rawLlmOutput = rawLlmOutput;
    }

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public String getPolicyId() { return policyId; }
    public void setPolicyId(String policyId) { this.policyId = policyId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getLocalPath() { return localPath; }
    public void setLocalPath(String localPath) { this.localPath = localPath; }

    public String getPatientNameOnBill() { return patientNameOnBill; }
    public void setPatientNameOnBill(String patientNameOnBill) { this.patientNameOnBill = patientNameOnBill; }

    public String getVerdict() { return verdict; }
    public void setVerdict(String verdict) { this.verdict = verdict; }

    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(  BigDecimal confidence) { this.confidence = confidence; }

    public String getRuleApplied() { return ruleApplied; }
    public void setRuleApplied(String ruleApplied) { this.ruleApplied = ruleApplied; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getRawLlmOutput() { return rawLlmOutput; }
    public void setRawLlmOutput(String rawLlmOutput) { this.rawLlmOutput = rawLlmOutput; }

    public String getLineItems() { return lineItems; }
    public void setLineItems(String lineItems) { this.lineItems = lineItems; }

    public java.math.BigDecimal getClaimableAmount() { return claimableAmount; }
    public void setClaimableAmount(java.math.BigDecimal claimableAmount) { this.claimableAmount = claimableAmount; }

    public java.math.BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(java.math.BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
