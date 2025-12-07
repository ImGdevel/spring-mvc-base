package com.spring.mvc.base.application.file.dto.request;

import com.spring.mvc.base.domain.file.entity.FileType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PresignRequest(
        @NotNull(message = "fileType is required")
        FileType fileType,

        @NotBlank(message = "originalName is required")
        String originalName,

        @NotBlank(message = "mimeType is required")
        String mimeType
) {
}
