package com.spring.mvc.base.application.post.controller;

import com.spring.mvc.base.application.post.controller.docs.TagApiDocs;
import com.spring.mvc.base.application.post.service.PostTagService;
import com.spring.mvc.base.common.dto.api.ApiResponse;
import com.spring.mvc.base.domain.post.entity.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController implements TagApiDocs {

    private final PostTagService postTagService;

    @GetMapping("/popular")
    public ApiResponse<List<Tag>> getPopularTags(
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<Tag> tags = postTagService.getTopTags(limit);
        return ApiResponse.success(tags, "popular_tags_retrieved");
    }
}
