package com.spring.mvc.base.domain.post.repository;

import com.spring.mvc.base.domain.post.dto.PostSearchCondition;
import com.spring.mvc.base.domain.post.dto.PostSummaryQueryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostQueryRepository {

    Page<PostSummaryQueryDto> searchPosts(PostSearchCondition condition, Pageable pageable);

}
