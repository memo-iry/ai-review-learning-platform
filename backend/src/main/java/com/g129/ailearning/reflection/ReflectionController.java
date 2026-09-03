package com.g129.ailearning.reflection;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/reflections")
@Tag(name = "회고록", description = "학습 회고록 등록 및 조회 API")
public class ReflectionController {

    private final ReflectionService reflectionService;

    public ReflectionController(ReflectionService reflectionService) {
        this.reflectionService = reflectionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "회고록 등록")
    public ReflectionResponse create(@Valid @RequestBody ReflectionRequest request) {
        return reflectionService.create(request);
    }

    @GetMapping("/{reflectionId}")
    @Operation(summary = "회고록 상세 조회")
    public ReflectionResponse findOne(@PathVariable Long reflectionId) {
        return reflectionService.findOne(reflectionId);
    }
}
