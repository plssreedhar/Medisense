package com.epam.medisense.feature.claimsense;

import java.util.List;

public record ClaimVerdict(
    String fileName,
    String verdict,           // CLAIMABLE | PARTIAL | EXCLUDED
    double confidence,
    String ruleApplied,
    String explanation,
    double totalAmount,
    double claimableAmount,
    List<LineItemVerdict> lineItems
) {}
