package com.spring.mvc.base.application.comment;

import com.spring.mvc.base.application.comment.dto.request.CommentCreateRequest;
import com.spring.mvc.base.application.comment.dto.request.CommentUpdateRequest;
import com.spring.mvc.base.domain.post.CommentFixture;

public final class CommentRequestFixture {

    private CommentRequestFixture() {}

    public static CommentCreateRequest createRequest() {
        return new CommentCreateRequest(CommentFixture.DEFAULT_CONTENT);
    }

    public static CommentCreateRequest createRequest(String content) {
        return new CommentCreateRequest(content);
    }

    public static CommentUpdateRequest updateRequest() {
        return new CommentUpdateRequest(CommentFixture.UPDATED_CONTENT);
    }

    public static CommentUpdateRequest updateRequest(String content) {
        return new CommentUpdateRequest(content);
    }

    public static CommentCreateRequest createRequestWithoutContent() {
        return new CommentCreateRequest(null);
    }

    public static CommentUpdateRequest updateRequestWithoutContent() {
        return new CommentUpdateRequest(null);
    }
}
