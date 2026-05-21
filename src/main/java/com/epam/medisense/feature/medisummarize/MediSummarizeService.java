package com.epam.medisense.feature.medisummarize;

import com.epam.medisense.feature.medichat.TranslationAgent;
import com.epam.medisense.feature.medisummarize.agents.ExtractionAgent;
import com.epam.medisense.feature.medisummarize.agents.SummarizationAgent;
import com.epam.medisense.feature.shared.DocumentValidationAgent;
import com.epam.medisense.feature.shared.ValidationResult;
import com.epam.medisense.storage.StorageService;
import com.epam.medisense.util.PdfTextExtractor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class MediSummarizeService {

    private final PdfTextExtractor pdfTextExtractor;
    private final ExtractionAgent extractionAgent;
    private final SummarizationAgent summarizationAgent;
    private final StorageService storageService;
    private final DocumentValidationAgent validationAgent;
    private final TranslationAgent translationAgent;
    private final SummarizeResultRepository summarizeResultRepository;

    public MediSummarizeService(PdfTextExtractor pdfTextExtractor,
                                ExtractionAgent extractionAgent,
                                SummarizationAgent summarizationAgent,
                                StorageService storageService,
                                DocumentValidationAgent validationAgent,
                                TranslationAgent translationAgent,
                                SummarizeResultRepository summarizeResultRepository) {
        this.pdfTextExtractor = pdfTextExtractor;
        this.extractionAgent = extractionAgent;
        this.summarizationAgent = summarizationAgent;
        this.storageService = storageService;
        this.validationAgent = validationAgent;
        this.translationAgent = translationAgent;
        this.summarizeResultRepository = summarizeResultRepository;
    }

    public MediSummarizeResponse summarize(MultipartFile file, String language) throws IOException {
        byte[] bytes = file.getBytes();

        String localPath = storageService.store(file.getOriginalFilename(), bytes);

        String rawText = pdfTextExtractor.extract(bytes);

        if (rawText == null || rawText.isBlank()) {
            throw new IllegalArgumentException("Could not extract text from PDF. Scanned PDFs are not supported.");
        }

        ValidationResult vr = validationAgent.validate(rawText, null, null);
        if (!vr.isValidDocument()) {
            throw new IllegalArgumentException("This does not appear to be a medical document. " + vr.getReason());
        }

        String extractedJson = extractionAgent.extract(rawText);
        String summary = summarizationAgent.summarize(extractedJson);
        summary = translationAgent.translate(summary, language);

        SummarizeResult result = new SummarizeResult();
        result.setFileName(file.getOriginalFilename());
        result.setLocalPath(localPath);
        result.setSummary(summary);
        summarizeResultRepository.save(result);

        return new MediSummarizeResponse(extractedJson, summary);
    }
}
