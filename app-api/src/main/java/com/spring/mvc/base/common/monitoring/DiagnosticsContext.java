package com.spring.mvc.base.common.monitoring;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiagnosticsContext {

    private final String[] activeProfiles;
    private final JvmMemoryInfo jvmMemoryInfo;
    private final ThreadInfo threadInfo;
    private final List<GcInfo> gcInfos;
    private final DataSourceInfo dataSourceInfo;
    private final RedisInfo redisInfo;

    @Getter
    @Builder
    public static class JvmMemoryInfo {
        private final String vmName;
        private final String javaVersion;
        private final int processors;
        private final long heapInitMb;
        private final long heapUsedMb;
        private final long heapMaxMb;
    }

    @Getter
    @Builder
    public static class ThreadInfo {
        private final int current;
        private final int peak;
        private final int daemon;
    }

    @Getter
    @Builder
    public static class GcInfo {
        private final String name;
        private final long collectionCount;
        private final long collectionTimeMs;
    }

    @Getter
    @Builder
    public static class DataSourceInfo {
        private final String poolName;
        private final int minIdle;
        private final int maxPoolSize;
        private final long connectionTimeoutMs;
        private final long idleTimeoutMs;
        private final long maxLifetimeMs;
    }

    @Getter
    @Builder
    public static class RedisInfo {
        private final String host;
        private final Integer port;
    }
}

