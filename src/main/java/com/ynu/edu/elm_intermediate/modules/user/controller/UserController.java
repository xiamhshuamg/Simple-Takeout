package com.ynu.edu.elm_intermediate.modules.user.controller;

import com.ynu.edu.elm_intermediate.common.AppException;
import com.ynu.edu.elm_intermediate.common.Result;
import com.ynu.edu.elm_intermediate.config.AuthInterceptor;
import com.ynu.edu.elm_intermediate.entity.Customer;
import com.ynu.edu.elm_intermediate.modules.user.dto.LoginRequest;
import com.ynu.edu.elm_intermediate.modules.user.dto.RegisterRequest;
import com.ynu.edu.elm_intermediate.modules.user.service.UserService;
import com.ynu.edu.elm_intermediate.modules.user.vo.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest req) {
        return Result.ok(userService.register(req), "注册成功");
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return Result.ok(userService.login(req), "登录成功");
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.ok(null, "已退出");
    }

    @GetMapping("/info")
    public Result<Map<String, Object>> info(HttpServletRequest req) {
        Integer uid = (Integer) req.getAttribute(AuthInterceptor.ATTR_CUSTOMER_ID);
        String role = (String) req.getAttribute(AuthInterceptor.ATTR_ROLE);

        if (uid == null) uid = (Integer) req.getAttribute("uid");
        if (role == null) role = (String) req.getAttribute("role");

        return Result.ok(userService.getUserInfo(uid, role));
    }

    /**
     * 更新个人资料（customer / admin）
     * 前端传：{ userName, avatar }
     */
    @PostMapping("/update")
    public Result<Void> update(@RequestBody Customer body, HttpServletRequest req) {
        Integer uid = (Integer) req.getAttribute(AuthInterceptor.ATTR_CUSTOMER_ID);
        String role = (String) req.getAttribute(AuthInterceptor.ATTR_ROLE);

        if (uid == null) uid = (Integer) req.getAttribute("uid");
        if (role == null) role = (String) req.getAttribute("role");

        if ("business".equals(role)) {
            throw new AppException(403, "商家资料请在店铺信息里修改", 403);
        }

        userService.updateProfile(uid, role, body.getUserName(), body.getAvatar());
        return Result.ok(null, "更新成功");
    }
}
