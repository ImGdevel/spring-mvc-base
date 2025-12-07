package com.spring.mvc.base.common.monitoring;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class DiagnosticsFormatter {

    private final Map<String, DiagnosticsFormatStrategy> strategies;

    public DiagnosticsFormatter(List<DiagnosticsFormatStrategy> strategies) {
        this.strategies = strategies.stream()
                .collect(Collectors.toMap(
                        s -> s.getType().toLowerCase(Locale.ROOT),
                        Function.identity()
                ));
    }

    public String format(DiagnosticsContext context, String type) {
        DiagnosticsFormatStrategy strategy = strategies.get(type.toLowerCase(Locale.ROOT));
        if (strategy == null) {
            throw new IllegalArgumentException("지원하지 않는 진단 포맷 타입입니다: " + type);
        }
        return strategy.format(context);
    }

    public String formatText(DiagnosticsContext context) {
        return format(context, "text");
    }

    public String formatHtml(DiagnosticsContext context) {
        return format(context, "html");
    }
}
