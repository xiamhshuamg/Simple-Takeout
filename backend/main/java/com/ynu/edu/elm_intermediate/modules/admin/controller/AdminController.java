package com.ynu.edu.elm_intermediate.modules.admin.controller;

import com.ynu.edu.elm_intermediate.common.AppException;
import com.ynu.edu.elm_intermediate.common.Result;
import com.ynu.edu.elm_intermediate.config.AuthInterceptor;
import com.ynu.edu.elm_intermediate.entity.Business;
import com.ynu.edu.elm_intermediate.entity.Customer;
import com.ynu.edu.elm_intermediate.modules.admin.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员后台接口：
 * 仅允许 role=admin 访问（列表 + 删除）。
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    private void checkAdmin(HttpServletRequest req) {
        String role = (String) req.getAttribute(AuthInterceptor.ATTR_ROLE);
        if (role == null) role = (String) req.getAttribute("role");
        if (!"admin".equals(role)) {
            throw new AppException(403, "无权访问管理员接口", 403);
        }
    }

    @GetMapping("/customers")
    public Result<List<Customer>> customers(HttpServletRequest req) {
        checkAdmin(req);
        return Result.ok(adminService.listCustomers());
    }

    @GetMapping("/businesses")
    public Result<List<Business>> businesses(HttpServletRequest req) {
        checkAdmin(req);
        return Result.ok(adminService.listBusinesses());
    }

    @DeleteMapping("/customers/{id}")
    public Result<Void> deleteCustomer(@PathVariable int id, HttpServletRequest req) {
        checkAdmin(req);
        adminService.deleteCustomer(id);
        return Result.ok(null, "删除成功");
    }

    @DeleteMapping("/businesses/{id}")
    public Result<Void> deleteBusiness(@PathVariable int id, HttpServletRequest req) {
        checkAdmin(req);
        adminService.deleteBusiness(id);
        return Result.ok(null, "删除成功");
    }
}
