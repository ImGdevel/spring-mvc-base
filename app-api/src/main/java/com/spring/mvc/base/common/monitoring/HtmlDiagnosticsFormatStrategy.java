package com.spring.mvc.base.common.monitoring;

import org.springframework.stereotype.Component;

@Component
public class HtmlDiagnosticsFormatStrategy implements DiagnosticsFormatStrategy {

    @Override
    public String getType() {
        return "html";
    }

    @Override
    public String format(DiagnosticsContext context) {
        String nl = System.lineSeparator();
        StringBuilder sb = new StringBuilder();

        sb.append("<!DOCTYPE html>").append(nl)
                .append("<html lang=\"ko\">").append(nl)
                .append("<head>").append(nl)
                .append("  <meta charset=\"UTF-8\"/>").append(nl)
                .append("  <title>애플리케이션 진단 정보</title>").append(nl)
                .append("  <style>").append(nl)
                .append("    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; background:#0f172a; color:#e5e7eb; padding:24px; }").append(nl)
                .append("    .card { max-width: 960px; margin:0 auto; background:#020617; border-radius:12px; padding:24px 28px; box-shadow:0 10px 40px rgba(0,0,0,0.45); }").append(nl)
                .append("    h1 { margin:0 0 8px; font-size:20px; color:#fbbf24; }").append(nl)
                .append("    .subtitle { margin:0 0 20px; font-size:12px; color:#9ca3af; }").append(nl)
                .append("    .section { margin-top:18px; }").append(nl)
                .append("    .section-title { font-size:13px; font-weight:600; color:#38bdf8; margin-bottom:6px; text-transform:uppercase; letter-spacing:.04em; }").append(nl)
                .append("    table { width:100%; border-collapse:collapse; font-size:13px; }").append(nl)
                .append("    th, td { padding:4px 0; vertical-align:top; }").append(nl)
                .append("    th { width:140px; color:#9ca3af; font-weight:500; text-align:left; padding-right:12px; }").append(nl)
                .append("    td { color:#e5e7eb; }").append(nl)
                .append("    .mono { font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace; }").append(nl)
                .append("    .pill { display:inline-block; padding:2px 8px; border-radius:999px; background:#1e293b; margin-right:4px; font-size:11px; }").append(nl)
                .append("    .pill--profile { background:#1e293b; color:#e5e7eb; }").append(nl)
                .append("    .pill--gc { background:#0369a1; color:#e0f2fe; }").append(nl)
                .append("  </style>").append(nl)
                .append("</head>").append(nl)
                .append("<body>").append(nl)
                .append("<div class=\"card\">").append(nl)
                .append("  <h1>애플리케이션 진단 정보</h1>").append(nl)
                .append("  <p class=\"subtitle\">현재 JVM 및 인프라 상태 스냅샷입니다.</p>").append(nl);

        // 프로필
        sb.append("  <div class=\"section\">").append(nl)
                .append("    <div class=\"section-title\">Profiles</div>").append(nl)
                .append("    <div>");
        for (String profile : context.getActiveProfiles()) {
            sb.append("<span class=\"pill pill--profile mono\">")
                    .append(escape(profile))
                    .append("</span>");
        }
        sb.append("</div>").append(nl)
                .append("  </div>").append(nl);

        // JVM / 메모리 / CPU
        DiagnosticsContext.JvmMemoryInfo jvm = context.getJvmMemoryInfo();
        sb.append("  <div class=\"section\">").append(nl)
                .append("    <div class=\"section-title\">JVM &amp; Memory</div>").append(nl)
                .append("    <table>").append(nl)
                .append("      <tr><th>JVM 이름</th><td class=\"mono\">").append(escape(jvm.getVmName())).append("</td></tr>").append(nl)
                .append("      <tr><th>Java 버전</th><td class=\"mono\">").append(escape(jvm.getJavaVersion())).append("</td></tr>").append(nl)
                .append("      <tr><th>CPU 코어</th><td class=\"mono\">").append(jvm.getProcessors()).append("</td></tr>").append(nl)
                .append("      <tr><th>힙 메모리</th><td class=\"mono\">init=")
                .append(jvm.getHeapInitMb()).append("MB, used=")
                .append(jvm.getHeapUsedMb()).append("MB, max=")
                .append(jvm.getHeapMaxMb()).append("MB</td></tr>").append(nl)
                .append("    </table>").append(nl)
                .append("  </div>").append(nl);

        // 스레드
        DiagnosticsContext.ThreadInfo threads = context.getThreadInfo();
        sb.append("  <div class=\"section\">").append(nl)
                .append("    <div class=\"section-title\">Threads</div>").append(nl)
                .append("    <table>").append(nl)
                .append("      <tr><th>현재 개수</th><td class=\"mono\">").append(threads.getCurrent()).append("</td></tr>").append(nl)
                .append("      <tr><th>피크</th><td class=\"mono\">").append(threads.getPeak()).append("</td></tr>").append(nl)
                .append("      <tr><th>데몬</th><td class=\"mono\">").append(threads.getDaemon()).append("</td></tr>").append(nl)
                .append("    </table>").append(nl)
                .append("  </div>").append(nl);

        // GC
        sb.append("  <div class=\"section\">").append(nl)
                .append("    <div class=\"section-title\">Garbage Collectors</div>").append(nl);
        if (context.getGcInfos().isEmpty()) {
            sb.append("    <p class=\"mono\" style=\"color:#9ca3af;\">GC 정보 없음</p>").append(nl);
        } else {
            sb.append("    <table>").append(nl);
            for (DiagnosticsContext.GcInfo gc : context.getGcInfos()) {
                sb.append("      <tr>").append(nl)
                        .append("        <th>").append("<span class=\"pill pill--gc mono\">")
                        .append(escape(gc.getName()))
                        .append("</span></th>").append(nl)
                        .append("        <td class=\"mono\">수=")
                        .append(gc.getCollectionCount())
                        .append(", 시간=")
                        .append(gc.getCollectionTimeMs())
                        .append("ms</td>").append(nl)
                        .append("      </tr>").append(nl);
            }
            sb.append("    </table>").append(nl);
        }
        sb.append("  </div>").append(nl);

        // 데이터소스
        DiagnosticsContext.DataSourceInfo ds = context.getDataSourceInfo();
        sb.append("  <div class=\"section\">").append(nl)
                .append("    <div class=\"section-title\">DataSource (HikariCP)</div>").append(nl);
        if (ds != null) {
            sb.append("    <table>").append(nl)
                    .append("      <tr><th>풀 이름</th><td class=\"mono\">").append(escape(ds.getPoolName())).append("</td></tr>").append(nl)
                    .append("      <tr><th>minIdle</th><td class=\"mono\">").append(ds.getMinIdle()).append("</td></tr>").append(nl)
                    .append("      <tr><th>maxPoolSize</th><td class=\"mono\">").append(ds.getMaxPoolSize()).append("</td></tr>").append(nl)
                    .append("      <tr><th>connTimeout</th><td class=\"mono\">").append(ds.getConnectionTimeoutMs()).append("ms</td></tr>").append(nl)
                    .append("      <tr><th>idleTimeout</th><td class=\"mono\">").append(ds.getIdleTimeoutMs()).append("ms</td></tr>").append(nl)
                    .append("      <tr><th>maxLifetime</th><td class=\"mono\">").append(ds.getMaxLifetimeMs()).append("ms</td></tr>").append(nl)
                    .append("    </table>").append(nl);
        } else {
            sb.append("    <p class=\"mono\" style=\"color:#f97316;\">HikariDataSource 사용 불가</p>").append(nl);
        }
        sb.append("  </div>").append(nl);

        // Redis
        DiagnosticsContext.RedisInfo redis = context.getRedisInfo();
        sb.append("  <div class=\"section\">").append(nl)
                .append("    <div class=\"section-title\">Redis</div>").append(nl);
        if (redis != null) {
            sb.append("    <table>").append(nl)
                    .append("      <tr><th>host</th><td class=\"mono\">").append(escape(redis.getHost())).append("</td></tr>").append(nl)
                    .append("      <tr><th>port</th><td class=\"mono\">").append(redis.getPort()).append("</td></tr>").append(nl)
                    .append("    </table>").append(nl);
        } else {
            sb.append("    <p class=\"mono\" style=\"color:#f97316;\">RedisProperties 사용 불가</p>").append(nl);
        }
        sb.append("  </div>").append(nl);

        sb.append("</div>").append(nl)
                .append("</body>").append(nl)
                .append("</html>").append(nl);

        return sb.toString();
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

