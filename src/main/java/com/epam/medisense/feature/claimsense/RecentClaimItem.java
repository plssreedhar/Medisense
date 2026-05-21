package com.epam.medisense.feature.claimsense;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecentClaimItem(
        Long id,
        String fileName,
        String verdict,
        String explanation,
        BigDecimal totalAmount,
        BigDecimal claimableAmount,
        LocalDateTime createdAt
) {}
