package com.epam.medisense.feature.medisummarize;

public class MediSummarizeResponse {

    private final String extractedJson;
    private final String summary;

    public MediSummarizeResponse(String extractedJson, String summary) {
        this.extractedJson = extractedJson;
        this.summary = summary;
    }

    public String getExtractedJson() {
        return extractedJson;
    }

    public String getSummary() {
        return summary;
    }
}
