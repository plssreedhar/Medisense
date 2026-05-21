package com.epam.medisense.feature.shared;

import com.epam.medisense.feature.medisummarize.SummarizeResultRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class RecentAnalysisController {

    private final SummarizeResultRepository summarizeResultRepository;

    public RecentAnalysisController(SummarizeResultRepository summarizeResultRepository) {
        this.summarizeResultRepository = summarizeResultRepository;
    }

    @GetMapping("/recent-analyses")
    public List<RecentAnalysisItem> recentAnalyses() {
        return summarizeResultRepository.findTop5ByOrderByCreatedAtDesc().stream()
                .map(r -> new RecentAnalysisItem(r.getId(), "MediSummarize", r.getFileName(), null, r.getSummary(), r.getCreatedAt()))
                .collect(Collectors.toList());
    }
}
