package com.skala.ailearning.lecture;

import com.skala.ailearning.common.NotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lectures")
public class LectureController {

    private final LectureRepository lectureRepository;
    private final LectureMaterialRepository materialRepository;

    public LectureController(LectureRepository lectureRepository,
                             LectureMaterialRepository materialRepository) {
        this.lectureRepository = lectureRepository;
        this.materialRepository = materialRepository;
    }

    @GetMapping
    public List<LectureResponse> getLectures() {
        return lectureRepository.findAllByOrderByLectureDateDesc().stream()
                .map(LectureResponse::from)
                .toList();
    }

    @GetMapping("/{lectureId}")
    public LectureResponse getLecture(@PathVariable Long lectureId) {
        return lectureRepository.findById(lectureId)
                .map(LectureResponse::from)
                .orElseThrow(() -> new NotFoundException("강의를 찾을 수 없습니다: " + lectureId));
    }

    @GetMapping("/{lectureId}/materials")
    public List<MaterialResponse> getMaterials(@PathVariable Long lectureId) {
        if (!lectureRepository.existsById(lectureId)) {
            throw new NotFoundException("강의를 찾을 수 없습니다: " + lectureId);
        }
        return materialRepository.findByLectureLectureId(lectureId).stream()
                .map(MaterialResponse::from)
                .toList();
    }
}
