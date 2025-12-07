package com.spring.mvc.base.fake;

import com.spring.mvc.base.infra.image.ImageSignature;
import com.spring.mvc.base.infra.image.ImageStorageService;

public class FakeImageStorageService implements ImageStorageService {

    private static final long FIXED_TIMESTAMP = 1234567890L;

    @Override
    public ImageSignature generateUploadSignature(String type) {
        return new ImageSignature(
                "test-api-key",
                "test-cloud-name",
                FIXED_TIMESTAMP,
                "signed-test-signature",
                "test-upload-preset",
                type.equals("profile") ? "profiles" : "posts"
        );
    }
}
