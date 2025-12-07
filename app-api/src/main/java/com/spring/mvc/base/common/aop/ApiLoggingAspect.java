package com.spring.mvc.base.common.aop;

import com.spring.mvc.base.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.atomic.AtomicLong;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@ConditionalOnProperty(name = "aop.api-logging.enabled", havingValue = "true", matchIfMissing = true)
public class ApiLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger("spring.aop.API");

    private static final AtomicLong REQUEST_ID_COUNTER = new AtomicLong(0);

    @Value("${aop.api-logging.include-stacktrace.business:false}")
    private boolean includeBusinessStackTrace;

    @Value("${aop.api-logging.include-stacktrace.system:true}")
    private boolean includeSystemStackTrace;

    @Pointcut("execution(* com.spring.mvc.base.application.*.controller.*.*(..))")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object logApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        long requestId = REQUEST_ID_COUNTER.incrementAndGet();
        String id = String.format("%03d", requestId); // 3자리 ID
        long startTime = System.currentTimeMillis();

        // Request 로그
        log.info("ID={} | Endpoint={} {} | IP={}",
                id,
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteAddr());

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            // Response 로그
            log.info("ID={} | Time={}ms", id, duration);

            return result;
        } catch (BusinessException e) {
            long duration = System.currentTimeMillis() - startTime;

            if (includeBusinessStackTrace) {
                log.warn("ID={} | Time={}ms | Status=BUSINESS_ERROR | Code={} | Message={}",
                        id, duration, e.getErrorCode(), e.getMessage(), e);
            } else {
                log.warn("ID={} | Time={}ms | Status=BUSINESS_ERROR | Code={} | Message={}",
                        id, duration, e.getErrorCode(), e.getMessage());
            }

            // 비즈니스 예외는 예상 가능한 오류이므로 알림 대상이 아니다.
            throw e;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;

            if (includeSystemStackTrace) {
                log.error("ID={} | Time={}ms | Status=UNHANDLED_ERROR | Error={}",
                        id, duration, e.getMessage(), e);
            } else {
                log.error("ID={} | Time={}ms | Status=UNHANDLED_ERROR | Error={}",
                        id, duration, e.getMessage());
            }

            // TODO: 시스템 예외(500)는 Slack/Email 등으로 알림 연동을 고려한다.
            throw e;
        }
    }
}
