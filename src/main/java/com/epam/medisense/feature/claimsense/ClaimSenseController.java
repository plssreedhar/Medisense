package com.epam.medisense.feature.claimsense;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/claims")
@CrossOrigin
public class ClaimSenseController {

    private final ClaimSenseService service;
    private final PolicyRuleRepository policyRuleRepository;
    private final ClaimResultRepository claimResultRepository;

    public ClaimSenseController(ClaimSenseService service, PolicyRuleRepository policyRuleRepository,
                                ClaimResultRepository claimResultRepository) {
        this.service = service;
        this.policyRuleRepository = policyRuleRepository;
        this.claimResultRepository = claimResultRepository;
    }

    @GetMapping("/recent")
    public List<RecentClaimItem> recentClaims() {
        return claimResultRepository.findTop5ByOrderByCreatedAtDesc().stream()
                .map(r -> new RecentClaimItem(
                        r.getId(), r.getFileName(), r.getVerdict(),
                        r.getReason(), r.getTotalAmount(), r.getClaimableAmount(), r.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @GetMapping("/policies")
    public ResponseEntity<?> getPolicies() {
        List<Map<String, String>> policies = policyRuleRepository.findAll().stream()
                .map(p -> Map.of("policyId", p.getPolicyId(), "name", p.getName()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(policies);
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyze(@RequestParam String policyId,
                                     @RequestParam String userName,
                                     @RequestParam("bills") List<MultipartFile> bills,
                                     @RequestParam(required = false, defaultValue = "English") String language) {
        if (policyId == null || policyId.isBlank()) {
            return ResponseEntity.badRequest().body("Policy ID is required");
        }
        if (userName == null || userName.isBlank()) {
            return ResponseEntity.badRequest().body("User name is required for patient verification");
        }
        if (bills == null || bills.isEmpty()) {
            return ResponseEntity.badRequest().body("No bills uploaded");
        }

        try {
            return ResponseEntity.ok(service.analyze(policyId, userName, bills, language));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body("An error occurred: " + ex.getMessage());
        }
    }
}
