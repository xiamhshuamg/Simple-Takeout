package com.ynu.edu.elm_intermediate;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CaseInfo {
    int order();           // 序号
    String method();       // 测试方法（例如：正常token/错误token/未携带token）
    String desc();         // 测试描述
}
