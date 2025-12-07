package com.spring.mvc.base.application.post;

import com.spring.mvc.base.application.post.dto.request.PostCreateRequest;
import com.spring.mvc.base.application.post.dto.request.PostUpdateRequest;
import com.spring.mvc.base.domain.post.PostFixture;

public final class PostRequestFixture {

    private PostRequestFixture() {}

    public static PostCreateRequest createRequest() {
        return new PostCreateRequest(PostFixture.DEFAULT_TITLE, PostFixture.DEFAULT_CONTENT, null, null, null, null, null, null, null, null);
    }

    public static PostCreateRequest createRequest(String title, String content) {
        return new PostCreateRequest(title, content, null, null, null, null, null, null, null, null);
    }

    public static PostCreateRequest createRequest(String title, String content, String imageUrl) {
        return new PostCreateRequest(title, content, imageUrl, null, null, null, null, null, null, null);
    }

    public static PostUpdateRequest updateRequest() {
        return new PostUpdateRequest(PostFixture.UPDATED_TITLE, PostFixture.UPDATED_CONTENT, null, null, null, null, null, null, null);
    }

    public static PostUpdateRequest updateRequest(String title, String content) {
        return new PostUpdateRequest(title, content, null, null, null, null, null, null, null);
    }

    public static PostUpdateRequest updateRequest(String title, String content, String imageUrl) {
        return new PostUpdateRequest(title, content, imageUrl, null, null, null, null, null, null);
    }

    public static PostCreateRequest createRequestWithoutTitle() {
        return new PostCreateRequest(null, PostFixture.DEFAULT_CONTENT, null, null, null, null, null, null, null, null);
    }

    public static PostCreateRequest createRequestWithoutContent() {
        return new PostCreateRequest(PostFixture.DEFAULT_TITLE, null, null, null, null, null, null, null, null, null);
    }

    public static PostCreateRequest createRequestWithInvalidImage(String invalidUrl) {
        return new PostCreateRequest(PostFixture.DEFAULT_TITLE, PostFixture.DEFAULT_CONTENT, invalidUrl, null, null, null, null, null, null, null);
    }

    public static PostUpdateRequest updateRequestWithInvalidThumbnail(String invalidUrl) {
        return new PostUpdateRequest(PostFixture.UPDATED_TITLE, PostFixture.UPDATED_CONTENT, null, invalidUrl, null, null, null, null, null);
    }
}
