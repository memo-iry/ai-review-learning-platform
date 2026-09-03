package com.g129.ailearning.material;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/courses/{courseId}/documents")
@Tag(name = "강의자료", description = "교육과정별 강의자료 조회 API")
public class LearningDocumentController {

    private final LearningDocumentRepository documentRepository;

    public LearningDocumentController(LearningDocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    @GetMapping
    @Operation(summary = "과정별 강의자료 조회")
    public List<DocumentResponse> findByCourse(@PathVariable Long courseId) {
        return documentRepository.findByCourseIdOrderByIdAsc(courseId).stream()
                .map(document -> new DocumentResponse(
                        document.getId(), document.getName(), document.getText(), document.getFileUrl()))
                .toList();
    }

    public record DocumentResponse(Long documentId, String documentName, String documentText, String fileUrl) {
    }
}
