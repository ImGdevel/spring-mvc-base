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
    private final DiagnosticsFormatter diagnosticsFormatter;

    @EventListener(ApplicationReadyEvent.class)
    public void logStartupDiagnostics() {
        DiagnosticsContext context = diagnosticsCollector.collectAll();
        String message = diagnosticsFormatter.formatText(context);
        log.info(message);
    }
}
