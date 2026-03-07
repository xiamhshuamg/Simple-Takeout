package com.ynu.edu.elm_intermediate;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TestCaseMeta {
    /** 序号：用于最终汇总表格排序 */
    int id();

    /** 测试方法：例如 黑盒/等价类/边界值/异常路径/鉴权 */
    String method();

    /** 测试描述：例如 “正常token访问受保护接口应成功” */
    String desc();
}
