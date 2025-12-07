package com.spring.mvc.base.domain.common.policy;

import com.spring.mvc.base.common.exception.BusinessException;
import com.spring.mvc.base.common.exception.code.CommonErrorCode;
import org.springframework.stereotype.Component;


@Component
public class OwnershipPolicy {

    /**
     * 리소스 소유권 검증
     */
    public void validateOwnership(Long resourceOwnerId, Long requesterId) {
        if (!resourceOwnerId.equals(requesterId)) {
            throw new BusinessException(CommonErrorCode.NO_PERMISSION);
        }
    }
}
