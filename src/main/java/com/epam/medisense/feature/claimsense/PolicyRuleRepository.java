package com.epam.medisense.feature.claimsense;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PolicyRuleRepository extends JpaRepository<PolicyRule, Long> {

    Optional<PolicyRule> findByPolicyId(String policyId);
}
