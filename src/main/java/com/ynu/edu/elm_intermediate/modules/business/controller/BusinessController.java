package com.ynu.edu.elm_intermediate.modules.business.controller;

import com.ynu.edu.elm_intermediate.common.AppException;
import com.ynu.edu.elm_intermediate.common.Result;
import com.ynu.edu.elm_intermediate.config.AuthInterceptor;
import com.ynu.edu.elm_intermediate.modules.business.service.BusinessService;
import com.ynu.edu.elm_intermediate.modules.business.vo.BusinessVO;
import com.ynu.edu.elm_intermediate.modules.business.vo.FoodVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/business")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    //获取商家列表
    @GetMapping("/list")
    public Result<List<BusinessVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng
    ) {
        return Result.ok(businessService.list(keyword, lat, lng));
    }

    //获取指定商家详情
    @GetMapping("/{id}")
    public Result<BusinessVO> detail(@PathVariable int id) {
        businessService.addVisitLog(id);
        return Result.ok(businessService.getBusinessInfo(id));
    }

    //获取商家菜单
    @GetMapping("/{id}/foods")
    public Result<List<FoodVO>> foods(@PathVariable int id) {
        return Result.ok(businessService.foods(id));
    }

    //获取商家所有菜单
    @GetMapping("/{id}/foods-all")
    public Result<List<FoodVO>> allFoods(@PathVariable int id, HttpServletRequest req) {
        return Result.ok(businessService.foodsAll(id));
    }

    //商家后台接口
    private int checkAuth(HttpServletRequest req) {
        String role = (String) req.getAttribute("role");
        if (!"business".equals(role)) throw new AppException(403, "无权操作", 403);
        return (int) req.getAttribute(AuthInterceptor.ATTR_CUSTOMER_ID);
    }

    @GetMapping("/my-info")
    public Result<BusinessVO> myInfo(HttpServletRequest req) {
        // 这里是商家自己看自己，不需要调用 addVisitLog，所以访客数不会加 1
        return Result.ok(businessService.getBusinessInfo(checkAuth(req)));
    }

    // 商家修改店铺信息
    @PostMapping("/update-info")
    public Result<Void> updateInfo(@RequestBody BusinessVO vo, HttpServletRequest req) {
        businessService.updateInfo(checkAuth(req), vo);
        return Result.ok(null, "更新成功");
    }

    // 商家修改店铺信息 (部分更新，如只改名字或头像)
    @PostMapping("/update-profile")
    public Result<Void> updateProfile(@RequestBody BusinessVO vo, HttpServletRequest req) {
        int bid = checkAuth(req);
        businessService.updateInfo(bid, vo);
        return Result.ok(null, "店铺信息更新成功");
    }

    @PostMapping("/food/save")
    public Result<Void> saveFood(@RequestBody FoodVO food, HttpServletRequest req) {
        businessService.saveFood(checkAuth(req), food);
        return Result.ok(null, "保存成功");
    }

    @DeleteMapping("/food/{id}")
    public Result<Void> deleteFood(@PathVariable int id, HttpServletRequest req) {
        businessService.removeFood(checkAuth(req), id);
        return Result.ok(null, "删除成功");
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(HttpServletRequest req) {
        return Result.ok(businessService.getStats(checkAuth(req)));
    }

    // 商家 设置营业/打烊
    @PostMapping("/update-status")
    public Result<Void> updateStatus(@RequestBody Map<String, Integer> body, HttpServletRequest req) {
        int bid = checkAuth(req);
        Integer isOpen = body.get("isOpen");
        if (isOpen == null) throw new AppException(400, "状态不能为空");

        businessService.updateBusinessStatus(bid, isOpen);
        return Result.ok(null, isOpen == 1 ? "店铺已营业，将在首页显示" : "店铺已休息，已从首页隐藏");
    }

    // 商家 菜品上架/下架
    @PostMapping("/food/status")
    public Result<Void> updateFoodStatus(@RequestBody Map<String, Integer> body, HttpServletRequest req) {
        int bid = checkAuth(req);
        Integer foodId = body.get("foodId");
        Integer status = body.get("status");

        if (foodId == null || status == null) throw new AppException(400, "参数错误");

        businessService.updateFoodStatus(bid, foodId, status);
        return Result.ok(null, status == 1 ? "菜品已上架" : "菜品已下架");
    }
}