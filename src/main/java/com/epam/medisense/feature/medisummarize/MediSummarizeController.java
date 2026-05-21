package com.epam.medisense.feature.medisummarize;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/summarize")
public class MediSummarizeController {

    private final MediSummarizeService service;

    public MediSummarizeController(MediSummarizeService service) {
        this.service = service;
    }

    @PostMapping
    @CrossOrigin
    public ResponseEntity<?> summarize(@RequestParam("file") MultipartFile file,
                                       @RequestParam(required = false, defaultValue = "English") String language) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file uploaded");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            return ResponseEntity.badRequest().body("Only PDF files are accepted");
        }

        try {
            MediSummarizeResponse response = service.summarize(file, language);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body("An error occurred: " + ex.getMessage());
        }
    }
}
