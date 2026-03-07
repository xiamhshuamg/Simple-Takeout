package com.ynu.edu.elm_intermediate;

import java.util.ArrayList;
import java.util.List;

public class TestReport {

    public static class Row {
        public int index;
        public String method;
        public String desc;
        public String status; // PASS / FAIL / SKIP
        public long costMs;
        public String remark;

        public Row(int index, String method, String desc, String status, long costMs, String remark) {
            this.index = index;
            this.method = method;
            this.desc = desc;
            this.status = status;
            this.costMs = costMs;
            this.remark = remark;
        }
    }

    private final List<Row> rows = new ArrayList<>();

    public void add(int index, String method, String desc, String status, long costMs, String remark) {
        rows.add(new Row(index, method, desc, status, costMs, remark == null ? "" : remark));
    }

    public int passCount() {
        return (int) rows.stream().filter(r -> "PASS".equalsIgnoreCase(r.status)).count();
    }

    public int failCount() {
        return (int) rows.stream().filter(r -> "FAIL".equalsIgnoreCase(r.status)).count();
    }

    public int skipCount() {
        return (int) rows.stream().filter(r -> "SKIP".equalsIgnoreCase(r.status)).count();
    }

    public int totalCount() {
        return rows.size();
    }

    public void print(String title) {
        System.out.println();
        System.out.println("======================================");
        System.out.println(title);
        System.out.println("序号 | 测试方法 | 测试描述 | 状态 | 耗时(ms) | 备注");
        System.out.println("--------------------------------------");
        for (Row r : rows) {
            System.out.printf("%-4d | %-6s | %-50s | %-4s | %-7d | %s%n",
                    r.index, r.method, r.desc, r.status, r.costMs, r.remark);
        }
        System.out.println("--------------------------------------");
        int pass = passCount();
        int total = totalCount();
        double rate = total == 0 ? 0.0 : (pass * 100.0 / total);
        System.out.printf("通过：%d / %d  (%.2f%%)  FAIL=%d  SKIP=%d%n", pass, total, rate, failCount(), skipCount());
        System.out.println("======================================");
        System.out.println();
    }
}
