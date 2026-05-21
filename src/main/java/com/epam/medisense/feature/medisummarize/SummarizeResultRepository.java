package com.epam.medisense.feature.medisummarize;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SummarizeResultRepository extends JpaRepository<SummarizeResult, Long> {
    List<SummarizeResult> findTop5ByOrderByCreatedAtDesc();
}
