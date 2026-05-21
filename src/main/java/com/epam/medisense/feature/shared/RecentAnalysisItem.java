package com.epam.medisense.feature.shared;

import java.time.LocalDateTime;

public record RecentAnalysisItem(
        Long id,
        String source,
        String fileName,
        String verdict,
        String detail,
        LocalDateTime createdAt
) {}
