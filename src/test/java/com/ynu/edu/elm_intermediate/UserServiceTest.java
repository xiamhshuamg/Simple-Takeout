package com.ynu.edu.elm_intermediate;

import com.ynu.edu.elm_intermediate.modules.user.dto.LoginRequest;
import com.ynu.edu.elm_intermediate.modules.user.dto.RegisterRequest;
import com.ynu.edu.elm_intermediate.modules.user.service.UserService;
import com.ynu.edu.elm_intermediate.modules.user.vo.LoginResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    @Transactional // 关键：测试完成后自动回滚，不污染数据库 [cite: 243, 270]
    public void testLoginSuccess() {
        // 1. 先注册一个新账号（确保数据存在）
        RegisterRequest reg = new RegisterRequest();
        reg.setAccount("18899998888"); // 使用数据库未占用的数字账号 [cite: 570]
        reg.setPassword("123456");
        reg.setRole("customer");
        userService.register(reg); // [cite: 575]

        // 2. 准备登录参数
        LoginRequest loginReq = new LoginRequest();
        loginReq.setPhone("18899998888");
        loginReq.setPassword("123456");

        // 3. 执行登录逻辑
        LoginResponse resp = userService.login(loginReq); // [cite: 532, 559]

        // 4. 断言验证结果 [cite: 609, 612]
        Assertions.assertNotNull(resp.getToken(), "登录成功应返回JWT Token");
        Assertions.assertEquals("customer", resp.getRole(), "身份应为用户");
        System.out.println("测试通过！用户Token: " + resp.getToken());
    }
}