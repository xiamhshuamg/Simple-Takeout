package com.ynu.edu.elm_intermediate.modules.address.controller;

import com.ynu.edu.elm_intermediate.common.AppException;
import com.ynu.edu.elm_intermediate.common.Result;
import com.ynu.edu.elm_intermediate.config.AuthInterceptor;
import com.ynu.edu.elm_intermediate.entity.DeliveryAddress;
import com.ynu.edu.elm_intermediate.modules.address.service.AddressService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/address")
public class AddressController {

    private final AddressService addressService;
    public AddressController(AddressService addressService) {

        this.addressService = addressService;
    }

    @GetMapping("/list")
    public Result<List<DeliveryAddress>> list(HttpServletRequest req) {
        String role = (String) req.getAttribute("role");
        if ("business".equals(role)) {
            // 如果是商家，直接返回空列表
            return Result.ok(new ArrayList<>());
        }

        int uid = (int) req.getAttribute(AuthInterceptor.ATTR_CUSTOMER_ID);
        return Result.ok(addressService.listByCustomer(uid));
    }

    // 同理，add/delete/setDefault 也要加这个判断，防止商家恶意修改用户数据
    @PostMapping("/add")
    public Result<Void> add(@RequestBody DeliveryAddress body, HttpServletRequest req) {
        if ("business".equals((String)req.getAttribute("role")))
            throw new AppException(403, "商家无法添加收货地址");
        int uid = (int) req.getAttribute(AuthInterceptor.ATTR_CUSTOMER_ID);
        addressService.add(uid, body.getContactName(),
                body.getContactSex(), body.getContactTel(), body.getAddress());
        return Result.ok(null, "添加成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable int id, HttpServletRequest req) {
        int uid = (int) req.getAttribute(AuthInterceptor.ATTR_CUSTOMER_ID);
        addressService.delete(id, uid);
        return Result.ok(null, "删除成功");
    }

    //设置默认地址接口
    @PostMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable int id, HttpServletRequest req) {
        int uid = (int) req.getAttribute(AuthInterceptor.ATTR_CUSTOMER_ID);
        addressService.setDefault(id, uid);
        return Result.ok(null, "设置成功");
    }
}