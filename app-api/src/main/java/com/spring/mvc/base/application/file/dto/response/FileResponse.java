package com.spring.mvc.base.application.file.dto.response;

import com.spring.mvc.base.domain.file.entity.File;
import com.spring.mvc.base.domain.file.entity.FileStatus;
import com.spring.mvc.base.domain.file.entity.FileType;
import java.time.Instant;

public record FileResponse(
        Long id,
        FileType fileType,
        String originalName,
        String storageKey,
        String url,
        Long size,
        String mimeType,
        FileStatus status,
        Boolean isDeleted,
        Instant createdAt,
        Instant updatedAt
) {
    public static FileResponse from(File file) {
        return new FileResponse(
                file.getId(),
                file.getFileType(),
                file.getOriginalName(),
                file.getStorageKey(),
                file.getUrl(),
                file.getSize(),
                file.getMimeType(),
                file.getStatus(),
                file.isDeleted(),
                file.getCreatedAt(),
                file.getUpdatedAt()
        );
    }
}
