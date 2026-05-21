package com.epam.medisense.feature.claimsense;

public record LineItemVerdict(
    String description,
    double amount,
    String verdict,   // CLAIMABLE | PARTIAL | EXCLUDED
    String reason
) {}
