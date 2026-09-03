package com.skala.ailearning.lecture;

public record MaterialResponse(
        Long materialId,
        String title,
        String materialType,
        String fileUrl
) {
    public static MaterialResponse from(LectureMaterial material) {
        return new MaterialResponse(
                material.getMaterialId(),
                material.getTitle(),
                material.getMaterialType().name(),
                material.getFileUrl()
        );
    }
}
