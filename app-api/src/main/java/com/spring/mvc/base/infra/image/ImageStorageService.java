package com.spring.mvc.base.infra.image;

public interface ImageStorageService {
    ImageSignature generateUploadSignature(String type);
}