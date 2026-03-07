package com.ynu.edu.elm_intermediate.modules.business.controller;

import com.ynu.edu.elm_intermediate.common.AppException;
import com.ynu.edu.elm_intermediate.common.Result;
import com.ynu.edu.elm_intermediate.config.AuthInterceptor;
import com.ynu.edu.elm_intermediate.entity.Admin;
import com.ynu.edu.elm_intermediate.entity.Customer;
import com.ynu.edu.elm_intermediate.entity.Food;
import com.ynu.edu.elm_intermediate.mapper.AdminMapper;
import com.ynu.edu.elm_intermediate.mapper.CustomerMapper;
import com.ynu.edu.elm_intermediate.mapper.FoodMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
public class UploadController {

    private static final String UPLOAD_ROOT_DIR = "C:/elm_upload/";

    private final CustomerMapper customerMapper;
    private final FoodMapper foodMapper;
    private final AdminMapper adminMapper;

    public UploadController(CustomerMapper customerMapper, FoodMapper foodMapper, AdminMapper adminMapper) {
        this.customerMapper = customerMapper;
        this.foodMapper = foodMapper;
        this.adminMapper = adminMapper;
    }

    @PostMapping("/avatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file,
                                       @RequestParam(value = "userId", required = false) Integer ignoreUserId,
                                       HttpServletRequest req) {
        Integer uid = (Integer) req.getAttribute(AuthInterceptor.ATTR_CUSTOMER_ID);
        String role = (String) req.getAttribute(AuthInterceptor.ATTR_ROLE);
        if (uid == null) uid = (Integer) req.getAttribute("uid");
        if (role == null) role = (String) req.getAttribute("role");

        if (!"customer".equals(role) && !"admin".equals(role)) {
            throw new AppException(403, "无权上传头像", 403);
        }

        String fileUrl = saveFile(file, "avatar");

        if ("admin".equals(role)) {
            Admin update = new Admin();
            update.setAdminId(uid);
            update.setAvatar(fileUrl);
            adminMapper.updateById(update);
        } else {
            Customer update = new Customer();
            update.setCustomerId(uid);
            update.setAvatar(fileUrl);
            customerMapper.updateById(update);
        }

        return Result.ok(fileUrl);
    }
     // 上传菜品图片
    @PostMapping("/food")
    public Result<String> uploadFood(@RequestParam("file") MultipartFile file,
                                     @RequestParam("foodId") int foodId,
                                     HttpServletRequest req) {
        String role = (String) req.getAttribute(AuthInterceptor.ATTR_ROLE);
        if (role == null) role = (String) req.getAttribute("role");
        if (!"business".equals(role)) throw new AppException(403, "无权操作", 403);

        String fileUrl = saveFile(file, "food");

        Food update = new Food();
        update.setFoodId(foodId);
        update.setFoodImg(fileUrl);
        foodMapper.updateById(update);

        return Result.ok(fileUrl);
    }

    //通用上传（仅 business，用于上传店铺图片等）
    @PostMapping("/common")
    public Result<String> uploadCommon(@RequestParam("file") MultipartFile file, HttpServletRequest req) {
        String role = (String) req.getAttribute(AuthInterceptor.ATTR_ROLE);
        if (role == null) role = (String) req.getAttribute("role");
        if (!"business".equals(role)) throw new AppException(403, "无权操作", 403);

        String fileUrl = saveFile(file, "common");
        return Result.ok(fileUrl);
    }

    private String saveFile(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) throw new AppException(400, "上传文件不能为空");
        //获取原始文件名并提取后缀
        String originalName = file.getOriginalFilename();
        String suffix = "";
        if (originalName != null && originalName.contains(".")) {
            suffix = originalName.substring(originalName.lastIndexOf("."));
        }
        //生成唯一文件名
        String newFileName = UUID.randomUUID().toString().replace("-", "") + suffix;
        File dir = new File(UPLOAD_ROOT_DIR + subDir);
        //创建目录
        if (!dir.exists() && !dir.mkdirs()) {
            throw new AppException(500, "创建上传目录失败");
        }
        //保存文件到磁盘
        try {
            file.transferTo(new File(dir, newFileName));
        } catch (IOException e) {
            e.printStackTrace();
            throw new AppException(500, "文件写入磁盘失败");
        }
        //返回相对URL路径
        return "/upload/" + subDir + "/" + newFileName;
    }
}
