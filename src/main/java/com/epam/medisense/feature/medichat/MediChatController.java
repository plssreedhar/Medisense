package com.epam.medisense.feature.medichat;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin
public class MediChatController {

    private final MediChatService service;

    public MediChatController(MediChatService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> chat(@RequestParam String sessionId,
                                  @RequestParam(required = false) String message,
                                  @RequestParam(required = false) MultipartFile file,
                                  @RequestParam(required = false) String policyId) {
        if (sessionId == null || sessionId.isBlank()) {
            return ResponseEntity.badRequest().body("sessionId is required");
        }
        if ((message == null || message.isBlank()) && (file == null || file.isEmpty())) {
            return ResponseEntity.badRequest().body("Please send a message or upload a file");
        }

        try {
            MediChatResponse response = service.chat(sessionId, message, file, policyId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body("An error occurred: " + ex.getMessage());
        }
    }
}
