package com.skala.ailearning.material;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/lectures/{lectureId}/materials")
@Tag(name = "강의자료", description = "강의별 자료 조회 API")
public class LectureMaterialController {
    private final LectureMaterialRepository materialRepository;
    public LectureMaterialController(LectureMaterialRepository materialRepository) { this.materialRepository = materialRepository; }

    @GetMapping
    @Operation(summary = "강의별 자료 조회")
    public List<MaterialResponse> findByLecture(@PathVariable Long lectureId) {
        return materialRepository.findByLectureIdOrderByIdAsc(lectureId).stream()
                .map(material -> new MaterialResponse(material.getId(), material.getTitle(), material.getMaterialType(), material.getFileUrl()))
                .toList();
    }
    public record MaterialResponse(Long materialId, String title, MaterialType materialType, String fileUrl) {}
}
