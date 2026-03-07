package com.ynu.edu.elm_intermediate.modules.order.controller;

import com.ynu.edu.elm_intermediate.common.AppException;
import com.ynu.edu.elm_intermediate.common.Result;
import com.ynu.edu.elm_intermediate.config.AuthInterceptor;
import com.ynu.edu.elm_intermediate.modules.order.dto.OrderCreateRequest;
import com.ynu.edu.elm_intermediate.modules.order.dto.OrderPayRequest;
import com.ynu.edu.elm_intermediate.modules.order.service.OrderService;
import com.ynu.edu.elm_intermediate.modules.order.vo.OrderVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    //从请求中获取角色
    private String getRole(HttpServletRequest req) {
        //从拦截器中放入的属性中得到
        String role = (String) req.getAttribute(AuthInterceptor.ATTR_ROLE);
        if (role == null) role = (String) req.getAttribute("role");
        return role;
    }

    private int getUid(HttpServletRequest req) {
        Integer uid = (Integer) req.getAttribute(AuthInterceptor.ATTR_CUSTOMER_ID);
        if (uid == null) uid = (Integer) req.getAttribute("uid");
        return uid == null ? 0 : uid;
    }

    @PostMapping("/create")
    public Result<Map<String, Object>> create(
            @Valid @RequestBody
            OrderCreateRequest req, HttpServletRequest servletRequest) {
        String role = getRole(servletRequest);
        if (!"customer".equals(role)) {
            throw new AppException(403, "只有用户可以下单", 403);
        }
        int customerId = getUid(servletRequest);
        return Result.ok(orderService.create(customerId, req), "下单成功");
    }

    @PostMapping("/pay")
    public Result<Map<String, Object>> pay(@Valid @RequestBody OrderPayRequest req,
                                           HttpServletRequest servletRequest) {
        String role = getRole(servletRequest);
        if (!"customer".equals(role)) {
            throw new AppException(403, "只有用户可以支付", 403);
        }
        int customerId = getUid(servletRequest);
        return Result.ok(orderService.pay(customerId, req), "支付成功");
    }

    @GetMapping("/list")
    public Result<List<OrderVO>> list(HttpServletRequest servletRequest) {
        String role = getRole(servletRequest);
        if (!"customer".equals(role)) {
            throw new AppException(403, "只有用户可以查看自己的订单", 403);
        }
        int customerId = getUid(servletRequest);
        return Result.ok(orderService.list(customerId));
    }

    @GetMapping("/merchant-list")
    public Result<List<OrderVO>> merchantList(HttpServletRequest req) {
        String role = getRole(req);
        if (!"business".equals(role)) {
            throw new AppException(403, "只有商家可以查看店铺订单", 403);
        }
        int bid = getUid(req); // 这里其实是 businessId
        return Result.ok(orderService.listByBusiness(bid));
    }

    // 获取指定订单详情（用于支付页倒计时校验）
    @GetMapping("/{orderId}")
    public Result<OrderVO> getDetail(@PathVariable int orderId, HttpServletRequest req) {
        String role = getRole(req);
        int uid = getUid(req);
        // 简单校验：必须是用户自己或者商家
        return Result.ok(orderService.getOrderDetail(orderId, uid, role));
    }
}
