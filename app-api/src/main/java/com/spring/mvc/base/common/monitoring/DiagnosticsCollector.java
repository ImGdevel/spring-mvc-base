package com.spring.mvc.base.common.monitoring;

import com.spring.mvc.base.infra.redis.config.RedisProperties;
import com.zaxxer.hikari.HikariDataSource;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiagnosticsCollector {

    private final Environment environment;
    private final ObjectProvider<HikariDataSource> dataSourceProvider;
    private final ObjectProvider<RedisProperties> redisPropertiesProvider;

    public String[] collectProfiles() {
        return environment.getActiveProfiles();
    }

    public DiagnosticsContext.JvmMemoryInfo collectJvmMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memoryBean.getHeapMemoryUsage();

        return DiagnosticsContext.JvmMemoryInfo.builder()
                .vmName(System.getProperty("java.vm.name"))
                .javaVersion(System.getProperty("java.version"))
                .processors(runtime.availableProcessors())
                .heapInitMb(toMb(heap.getInit()))
                .heapUsedMb(toMb(heap.getUsed()))
                .heapMaxMb(toMb(heap.getMax()))
                .build();
    }

    public DiagnosticsContext.ThreadInfo collectThreadInfo() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        return DiagnosticsContext.ThreadInfo.builder()
                .current(threadBean.getThreadCount())
                .peak(threadBean.getPeakThreadCount())
                .daemon(threadBean.getDaemonThreadCount())
                .build();
    }

    public List<DiagnosticsContext.GcInfo> collectGcInfos() {
        return ManagementFactory.getGarbageCollectorMXBeans()
                .stream()
                .map(gc -> DiagnosticsContext.GcInfo.builder()
                        .name(gc.getName())
                        .collectionCount(gc.getCollectionCount())
                        .collectionTimeMs(gc.getCollectionTime())
                        .build())
                .collect(Collectors.toList());
    }

    public DiagnosticsContext.DataSourceInfo collectDataSourceInfo() {
        HikariDataSource dataSource = dataSourceProvider.getIfAvailable();
        if (dataSource == null) {
            return null;
        }

        return DiagnosticsContext.DataSourceInfo.builder()
                .poolName(dataSource.getPoolName())
                .minIdle(dataSource.getMinimumIdle())
                .maxPoolSize(dataSource.getMaximumPoolSize())
                .connectionTimeoutMs(dataSource.getConnectionTimeout())
                .idleTimeoutMs(dataSource.getIdleTimeout())
                .maxLifetimeMs(dataSource.getMaxLifetime())
                .build();
    }

    public DiagnosticsContext.RedisInfo collectRedisInfo() {
        RedisProperties redisProperties = redisPropertiesProvider.getIfAvailable();
        if (redisProperties == null) {
            return null;
        }
        return DiagnosticsContext.RedisInfo.builder()
                .host(redisProperties.getHost())
                .port(redisProperties.getPort())
                .build();
    }

    public DiagnosticsContext collectAll() {
        return DiagnosticsContext.builder()
                .activeProfiles(collectProfiles())
                .jvmMemoryInfo(collectJvmMemoryInfo())
                .threadInfo(collectThreadInfo())
                .gcInfos(collectGcInfos())
                .dataSourceInfo(collectDataSourceInfo())
                .redisInfo(collectRedisInfo())
                .build();
    }

    private long toMb(long bytes) {
        return bytes <= 0 ? -1 : bytes / (1024 * 1024);
    }
}

