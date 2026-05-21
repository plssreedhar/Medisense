package com.epam.medisense.feature.medichat;

import com.epam.medisense.feature.claimsense.ClaimVerdict;
import java.util.List;
import java.util.UUID;

public record ChatResponse(
    UUID sessionId,
    String role,
    String message,
    String contentType,       // text | summary | verdict | question
    List<ClaimVerdict> verdicts  // non-null only when contentType = "verdict"
) {}
