package com.spring.mvc.base.application.common.controller;

import com.spring.mvc.base.common.dto.api.ApiResponse;
import com.spring.mvc.base.common.monitoring.DiagnosticsCollector;
import com.spring.mvc.base.common.monitoring.DiagnosticsContext;
import com.spring.mvc.base.common.monitoring.DiagnosticsFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/diagnostics")
@RequiredArgsConstructor
public class DiagnosticsController {

    private final DiagnosticsCollector diagnosticsCollector;
    private final DiagnosticsFormatter diagnosticsFormatter;

    @GetMapping
    public ApiResponse<DiagnosticsContext> getDiagnostics() {
        DiagnosticsContext context = diagnosticsCollector.collectAll();
        return ApiResponse.success(context);
    }

    @GetMapping(value = "/html", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getDiagnosticsHtml() {
        DiagnosticsContext context = diagnosticsCollector.collectAll();
        String html = diagnosticsFormatter.formatHtml(context);
        return ResponseEntity.ok(html);
    }
}
