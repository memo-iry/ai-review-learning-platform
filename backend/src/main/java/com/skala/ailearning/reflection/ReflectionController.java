package com.skala.ailearning.reflection;

import com.skala.ailearning.ai.AnalysisResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reflections")
public class ReflectionController {

    private final ReflectionService reflectionService;

    public ReflectionController(ReflectionService reflectionService) {
        this.reflectionService = reflectionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReflectionResponse create(@Valid @RequestBody ReflectionRequest request) {
        return reflectionService.create(request);
    }

    @PostMapping("/{reflectionId}/analyze")
    public AnalysisResponse analyze(@PathVariable Long reflectionId) {
        return reflectionService.analyze(reflectionId);
    }
}
