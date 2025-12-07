package com.spring.mvc.base.common.monitoring;

import com.spring.mvc.base.infra.redis.config.RedisProperties;
import com.zaxxer.hikari.HikariDataSource;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupDiagnosticsLogger {

    private final Environment environment;
    private final ObjectProvider<HikariDataSource> dataSourceProvider;
    private final ObjectProvider<RedisProperties> redisPropertiesProvider;

    @EventListener(ApplicationReadyEvent.class)
    public void logStartupDiagnostics() {
        log.info("====== Application Startup Diagnostics ======");
        logProfiles();
        logJvmAndMemory();
        logThreads();
        logGarbageCollectors();
        logDataSource();
        logRedis();
        log.info("====== Application Startup Diagnostics End ======");
    }

    private void logProfiles() {
        String[] activeProfiles = environment.getActiveProfiles();
        log.info("Active profiles: {}", (Object) activeProfiles);
    }

    private void logJvmAndMemory() {
        Runtime runtime = Runtime.getRuntime();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heap = memoryBean.getHeapMemoryUsage();

        log.info("JVM: {}, version: {}", System.getProperty("java.vm.name"), System.getProperty("java.version"));
        log.info("Processors: {}", runtime.availableProcessors());
        log.info("Heap memory (init/used/max) MB: {}/{}/{}",
                toMb(heap.getInit()),
                toMb(heap.getUsed()),
                toMb(heap.getMax()));
    }

    private void logThreads() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        log.info("Threads: current={}, peak={}, daemon={}",
                threadBean.getThreadCount(),
                threadBean.getPeakThreadCount(),
                threadBean.getDaemonThreadCount());
    }

    private void logGarbageCollectors() {
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            log.info("GC: name={}, collections={}, timeMs={}",
                    gc.getName(),
                    gc.getCollectionCount(),
                    gc.getCollectionTime());
        }
    }

    private void logDataSource() {
        HikariDataSource dataSource = dataSourceProvider.getIfAvailable();
        if (dataSource == null) {
            log.info("DataSource: HikariDataSource not available");
            return;
        }

        log.info("HikariCP pool: name={}, minIdle={}, maxPoolSize={}, connectionTimeoutMs={}, idleTimeoutMs={}, maxLifetimeMs={}",
                dataSource.getPoolName(),
                dataSource.getMinimumIdle(),
                dataSource.getMaximumPoolSize(),
                dataSource.getConnectionTimeout(),
                dataSource.getIdleTimeout(),
                dataSource.getMaxLifetime());
    }

    private void logRedis() {
        RedisProperties redisProperties = redisPropertiesProvider.getIfAvailable();
        if (redisProperties == null) {
            log.info("Redis: RedisProperties not available");
            return;
        }
        log.info("Redis: host={}, port={}", redisProperties.getHost(), redisProperties.getPort());
    }

    private long toMb(long bytes) {
        return bytes <= 0 ? -1 : bytes / (1024 * 1024);
    }
}

