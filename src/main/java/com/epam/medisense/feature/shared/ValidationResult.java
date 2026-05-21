package com.epam.medisense.feature.shared;

public class ValidationResult {

    private final boolean isValidDocument;
    private final Boolean patientMatch;
    private final String reason;

    public ValidationResult(boolean isValidDocument, Boolean patientMatch, String reason) {
        this.isValidDocument = isValidDocument;
        this.patientMatch = patientMatch;
        this.reason = reason;
    }

    public boolean isValidDocument() { return isValidDocument; }
    public Boolean getPatientMatch() { return patientMatch; }
    public String getReason() { return reason; }
}
