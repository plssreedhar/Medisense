package com.epam.medisense.feature.claimsense;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimResultRepository extends JpaRepository<ClaimResult, Long> {

    List<ClaimResult> findBySessionId(String sessionId);

    List<ClaimResult> findTop5ByOrderByCreatedAtDesc();
}
