package com.ynu.edu.elm_intermediate;

import org.junit.jupiter.api.extension.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TestReportExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback, TestWatcher, AfterAllCallback {

    private static final Map<String, Long> startNs = new ConcurrentHashMap<>();
    private static final List<Row> rows = Collections.synchronizedList(new ArrayList<>());

    private record Row(int id, String method, String desc, String status, long ms) {}

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        startNs.put(key(context), System.nanoTime());
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        // 如果测试异常，这里也会走；最终状态由 TestWatcher 决定
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
        addRow(context, "ABORT");
    }

    @Override
    public void afterAll(ExtensionContext context) {
        System.out.println();
        System.out.println("==================================");
        System.out.println("           TEST SUMMARY           ");
        System.out.println("==================================");

        // 按序号排序
        rows.sort(Comparator.comparingInt(Row::id));

        // 表头
        System.out.printf("%-6s %-10s %-50s %-8s %-8s%n",
                "序号", "测试方法", "测试描述", "状态", "耗时(ms)");
        System.out.println("--------------------------------------------------------------------------------------------");

        long pass = rows.stream().filter(r -> "PASS".equals(r.status)).count();
        long fail = rows.stream().filter(r -> "FAIL".equals(r.status)).count();
        long abort = rows.stream().filter(r -> "ABORT".equals(r.status)).count();

        for (Row r : rows) {
            System.out.printf("%-6d %-10s %-50s %-8s %-8d%n",
                    r.id, trim(r.method, 10), trim(r.desc, 50), r.status, r.ms);
        }

        System.out.println("--------------------------------------------------------------------------------------------");
        System.out.printf("TOTAL=%d  PASS=%d  FAIL=%d  ABORT=%d%n", rows.size(), pass, fail, abort);
        System.out.println("==================================");
        System.out.println();
    }

    private void addRow(ExtensionContext context, String status) {
        long ms = 0;
        Long s = startNs.remove(key(context));
        if (s != null) ms = (System.nanoTime() - s) / 1_000_000;

        Method testMethod = context.getRequiredTestMethod();
        TestCaseMeta meta = testMethod.getAnnotation(TestCaseMeta.class);

        int id = meta != null ? meta.id() : 9999;
        String method = meta != null ? meta.method() : "未标注";
        String desc = meta != null ? meta.desc() : testMethod.getName();

        rows.add(new Row(id, method, desc, status, ms));
    }

    private static String key(ExtensionContext context) {
        return context.getUniqueId();
    }

    private static String trim(String s, int max) {
        if (s == null) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max - 1) + "…";
    }
}
