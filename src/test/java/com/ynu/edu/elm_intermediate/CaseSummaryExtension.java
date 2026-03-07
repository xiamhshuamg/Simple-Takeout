package com.ynu.edu.elm_intermediate;

import org.junit.jupiter.api.extension.*;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class CaseSummaryExtension implements BeforeTestExecutionCallback, TestWatcher, AfterAllCallback {

    private static final Map<String, Long> START = new ConcurrentHashMap<>();
    private static final List<Row> ROWS = new CopyOnWriteArrayList<>();

    private record Row(int order, String testName, String method, String desc,
                       String status, long costMs) {}

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        START.put(key(context), System.currentTimeMillis());
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        addRow(context, "PASS");
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        addRow(context, "FAIL");
    }

    @Override
    public void testAborted(ExtensionContext context, Throwable cause) {
        addRow(context, "SKIP");
    }

    @Override
    public void testDisabled(ExtensionContext context, java.util.Optional<String> reason) {
        // disabled 通常不会走 beforeTestExecution，所以这里耗时给 0
        addRow(context, "SKIP", 0L);
    }

    private void addRow(ExtensionContext context, String status) {
        long cost = System.currentTimeMillis() - START.getOrDefault(key(context), System.currentTimeMillis());
        addRow(context, status, cost);
    }

    private void addRow(ExtensionContext context, String status, long costMs) {
        Method m = context.getRequiredTestMethod();
        CaseInfo info = m.getAnnotation(CaseInfo.class);

        int order = info != null ? info.order() : 999;
        String method = info != null ? info.method() : "-";
        String desc = info != null ? info.desc() : context.getDisplayName();
        String testName = m.getName();

        ROWS.add(new Row(order, testName, method, desc, status, costMs));
    }

    private String key(ExtensionContext context) {
        return context.getUniqueId();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        // 排序输出
        var sorted = ROWS.stream()
                .sorted((a, b) -> Integer.compare(a.order, b.order))
                .toList();

        long pass = sorted.stream().filter(r -> "PASS".equals(r.status)).count();
        long fail = sorted.stream().filter(r -> "FAIL".equals(r.status)).count();
        long skip = sorted.stream().filter(r -> "SKIP".equals(r.status)).count();
        long total = sorted.size();

        System.out.println();
        System.out.println("========================================");
        System.out.println(" BlackBox Test Summary @ " + LocalDateTime.now());
        System.out.println("========================================");
        System.out.printf("Total=%d | PASS=%d | FAIL=%d | SKIP=%d%n", total, pass, fail, skip);
        System.out.println("----------------------------------------");

        // 表头
        System.out.printf("%-4s | %-12s | %-14s | %-30s | %-6s | %-6s%n",
                "序号", "测试名", "测试方法", "测试描述", "状态", "耗时ms");
        System.out.println("-------------------------------------------------------------------------------------------");

        for (Row r : sorted) {
            System.out.printf("%-4d | %-12s | %-14s | %-30s | %-6s | %-6d%n",
                    r.order, cut(r.testName, 12), cut(r.method, 14), cut(r.desc, 30), r.status, r.costMs);
        }

        System.out.println("========================================");
        System.out.println();
    }

    private static String cut(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, Math.max(0, max - 1)) + "…";
    }
}
