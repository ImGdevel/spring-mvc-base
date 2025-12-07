package com.spring.mvc.base.common.monitoring;

import org.springframework.stereotype.Component;

@Component
public class TextDiagnosticsFormatStrategy implements DiagnosticsFormatStrategy {

    @Override
    public String getType() {
        return "text";
    }

    @Override
    public String format(DiagnosticsContext context) {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder();

        sb.append(nl)
                .append("┌─[ 애플리케이션 시작 진단 정보 ]────────────────────────────────────").append(nl);

        // 프로필
        sb.append("│ 프로필").append(nl);
        sb.append("│   - 활성 프로필 : ")
                .append(String.join(", ", context.getActiveProfiles()))
                .append(nl);

        // JVM / 메모리 / CPU
        DiagnosticsContext.JvmMemoryInfo jvm = context.getJvmMemoryInfo();
        sb.append("│").append(nl);
        sb.append("│ JVM / 메모리").append(nl);
        sb.append("│   - JVM 이름   : ").append(jvm.getVmName()).append(nl);
        sb.append("│   - Java 버전  : ").append(jvm.getJavaVersion()).append(nl);
        sb.append("│   - CPU 코어   : ").append(jvm.getProcessors()).append(nl);
        sb.append("│   - 힙 메모리  : init=")
                .append(jvm.getHeapInitMb()).append("MB, used=")
                .append(jvm.getHeapUsedMb()).append("MB, max=")
                .append(jvm.getHeapMaxMb()).append("MB").append(nl);

        // 스레드
        DiagnosticsContext.ThreadInfo threads = context.getThreadInfo();
        sb.append("│").append(nl);
        sb.append("│ 스레드").append(nl);
        sb.append("│   - 현재 개수   : ").append(threads.getCurrent()).append(nl);
        sb.append("│   - 피크        : ").append(threads.getPeak()).append(nl);
        sb.append("│   - 데몬        : ").append(threads.getDaemon()).append(nl);

        // GC
        sb.append("│").append(nl);
        sb.append("│ GC").append(nl);
        if (context.getGcInfos().isEmpty()) {
            sb.append("│   - GC 정보 없음").append(nl);
        } else {
            for (DiagnosticsContext.GcInfo gc : context.getGcInfos()) {
                sb.append("│   - 이름=").append(gc.getName())
                        .append(", 수=").append(gc.getCollectionCount())
                        .append(", 시간=").append(gc.getCollectionTimeMs()).append("ms")
                        .append(nl);
            }
        }

        // 데이터소스
        DiagnosticsContext.DataSourceInfo ds = context.getDataSourceInfo();
        sb.append("│").append(nl);
        sb.append("│ 데이터소스 (HikariCP)").append(nl);
        if (ds != null) {
            sb.append("│   - 풀 이름     : ").append(ds.getPoolName()).append(nl);
            sb.append("│   - minIdle     : ").append(ds.getMinIdle()).append(nl);
            sb.append("│   - maxPoolSize : ").append(ds.getMaxPoolSize()).append(nl);
            sb.append("│   - connTimeout : ").append(ds.getConnectionTimeoutMs()).append("ms").append(nl);
            sb.append("│   - idleTimeout : ").append(ds.getIdleTimeoutMs()).append("ms").append(nl);
            sb.append("│   - maxLifetime : ").append(ds.getMaxLifetimeMs()).append("ms").append(nl);
        } else {
            sb.append("│   - HikariDataSource 사용 불가").append(nl);
        }

        // Redis
        DiagnosticsContext.RedisInfo redis = context.getRedisInfo();
        sb.append("│").append(nl);
        sb.append("│ Redis").append(nl);
        if (redis != null) {
            sb.append("│   - host        : ").append(redis.getHost()).append(nl);
            sb.append("│   - port        : ").append(redis.getPort()).append(nl);
        } else {
            sb.append("│   - RedisProperties 사용 불가").append(nl);
        }

        sb.append("└────────────────────────────────────────────────────────────────────").append(nl);

        return sb.toString();
    }
}

