package com.spring.mvc.base.common.monitoring;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupDiagnosticsLogger {

    private final DiagnosticsCollector diagnosticsCollector;

    @EventListener(ApplicationReadyEvent.class)
    public void logStartupDiagnostics() {
        DiagnosticsContext context = diagnosticsCollector.collectAll();

        log.info("====== Application Startup Diagnostics ======");

        log.info("Active profiles: {}", (Object) context.getActiveProfiles());

        DiagnosticsContext.JvmMemoryInfo jvm = context.getJvmMemoryInfo();
        log.info("JVM: {}, version: {}", jvm.getVmName(), jvm.getJavaVersion());
        log.info("Processors: {}", jvm.getProcessors());
        log.info("Heap memory (init/used/max) MB: {}/{}/{}",
                jvm.getHeapInitMb(),
                jvm.getHeapUsedMb(),
                jvm.getHeapMaxMb());

        DiagnosticsContext.ThreadInfo threads = context.getThreadInfo();
        log.info("Threads: current={}, peak={}, daemon={}",
                threads.getCurrent(),
                threads.getPeak(),
                threads.getDaemon());

        for (DiagnosticsContext.GcInfo gc : context.getGcInfos()) {
            log.info("GC: name={}, collections={}, timeMs={}",
                    gc.getName(),
                    gc.getCollectionCount(),
                    gc.getCollectionTimeMs());
        }

        DiagnosticsContext.DataSourceInfo ds = context.getDataSourceInfo();
        if (ds != null) {
            log.info("HikariCP pool: name={}, minIdle={}, maxPoolSize={}, connectionTimeoutMs={}, idleTimeoutMs={}, maxLifetimeMs={}",
                    ds.getPoolName(),
                    ds.getMinIdle(),
                    ds.getMaxPoolSize(),
                    ds.getConnectionTimeoutMs(),
                    ds.getIdleTimeoutMs(),
                    ds.getMaxLifetimeMs());
        } else {
            log.info("DataSource: HikariDataSource not available");
        }

        DiagnosticsContext.RedisInfo redis = context.getRedisInfo();
        if (redis != null) {
            log.info("Redis: host={}, port={}", redis.getHost(), redis.getPort());
        } else {
            log.info("Redis: RedisProperties not available");
        }

        log.info("====== Application Startup Diagnostics End ======");
    }
}

