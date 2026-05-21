package com.epam.medisense.feature.medichat.agents;

public class OrchestratorResult {

    private final String intent;
    private final String message;

    public OrchestratorResult(String intent, String message) {
        this.intent = intent;
        this.message = message;
    }

    public String getIntent() { return intent; }
    public String getMessage() { return message; }
}
