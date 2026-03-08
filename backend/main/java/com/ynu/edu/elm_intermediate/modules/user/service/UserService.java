package com.ynu.edu.elm_intermediate.modules.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ynu.edu.elm_intermediate.common.AppException;
import com.ynu.edu.elm_intermediate.config.JwtUtil;
import com.ynu.edu.elm_intermediate.entity.Admin;
import com.ynu.edu.elm_intermediate.entity.Business;
import com.ynu.edu.elm_intermediate.entity.Customer;
import com.ynu.edu.elm_intermediate.mapper.AdminMapper;
import com.ynu.edu.elm_intermediate.mapper.BusinessMapper;
import com.ynu.edu.elm_intermediate.mapper.CustomerMapper;
import com.ynu.edu.elm_intermediate.modules.user.dto.LoginRequest;
import com.ynu.edu.elm_intermediate.modules.user.dto.RegisterRequest;
import com.ynu.edu.elm_intermediate.modules.user.vo.LoginResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    private final CustomerMapper customerMapper;
    private final BusinessMapper businessMapper;
    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(CustomerMapper customerMapper,
                       BusinessMapper businessMapper,
                       AdminMapper adminMapper,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.customerMapper = customerMapper;
        this.businessMapper = businessMapper;
        this.adminMapper = adminMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest req) {
        String accountStr = req.getPhone();
        String rawPwd = req.getPassword();

        // 1) 先尝试按“数字账号”登录（用户/商家）
        Long accountNum = tryParseLong(accountStr);
        if (accountNum != null) {
            // 用户
            Customer c = customerMapper.selectOne(
                    new LambdaQueryWrapper<Customer>().eq(Customer::getCustomerAccount, accountNum)
            );
            if (c != null && passwordMatch(rawPwd, c.getPassword())) {
                String token = jwtUtil.generateToken(c.getCustomerId(), accountNum, "customer");
                return new LoginResponse(c.getCustomerId(), token, "customer", c.getUserName());
            }

            // 商家
            Business b = businessMapper.selectOne(
                    new LambdaQueryWrapper<Business>().eq(Business::getBusinessAccount, accountNum)
            );
            if (b != null && passwordMatch(rawPwd, b.getPassword())) {
                String token = jwtUtil.generateToken(b.getBusinessId(), accountNum, "business");
                return new LoginResponse(b.getBusinessId(), token, "business", b.getBusinessName());
            }
        }

        // 2) 再尝试管理员（adminAccount 是 varchar，允许非数字）
        Admin a = adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>().eq(Admin::getAdminAccount, accountStr)
        );
        if (a != null && passwordMatch(rawPwd, a.getPassword())) {
            long adminAccountNum = accountNum != null ? accountNum : 0L; // 转不了就 0
            String token = jwtUtil.generateToken(a.getAdminId(), adminAccountNum, "admin");
            return new LoginResponse(a.getAdminId(), token, "admin", a.getAdminName());
        }

        throw new AppException(40012, "账号或密码错误");
    }

    public LoginResponse register(RegisterRequest req) {
        String role = (req.getRole() == null || req.getRole().isBlank()) ? "customer" : req.getRole();

        Long account = tryParseLong(req.getAccount());
        if (account == null) {
            throw new AppException(40002, "账号必须是纯数字（手机号/数字ID）");
        }

        // 用户账号 和 商家账号 不能重复
        boolean existsInCustomer = customerMapper.selectCount(
                new LambdaQueryWrapper<Customer>().eq(Customer::getCustomerAccount, account)
        ) > 0;
        boolean existsInBusiness = businessMapper.selectCount(
                new LambdaQueryWrapper<Business>().eq(Business::getBusinessAccount, account)
        ) > 0;

        if (existsInCustomer || existsInBusiness) {
            throw new AppException(40003, "账号已存在（用户账号与商家账号不能相同）");
        }

        String hash = passwordEncoder.encode(req.getPassword());

        if ("customer".equals(role)) {
            Customer c = new Customer();
            c.setCustomerAccount(account);
            c.setPassword(hash);
            c.setUserName("新用户");
            c.setBalance(BigDecimal.ZERO); // 你实体是 BigDecimal
            customerMapper.insert(c);

            String token = jwtUtil.generateToken(c.getCustomerId(), account, "customer");
            return new LoginResponse(c.getCustomerId(), token, "customer", c.getUserName());
        }

        if ("business".equals(role)) {
            Business b = new Business();
            b.setBusinessAccount(account);
            b.setPassword(hash);
            b.setBusinessName("新商家"); // businessName NOT NULL
            businessMapper.insert(b);

            String token = jwtUtil.generateToken(b.getBusinessId(), account, "business");
            return new LoginResponse(b.getBusinessId(), token, "business", b.getBusinessName());
        }

        throw new AppException(40002, "不支持的注册角色：" + role);
    }

    public Map<String, Object> getUserInfo(Integer uid, String role) {
        if (uid == null) throw new AppException(401, "未登录", 401);

        Map<String, Object> map = new HashMap<>();
        map.put("id", uid);
        map.put("role", role);

        if ("business".equals(role)) {
            Business b = businessMapper.selectById(uid);
            if (b != null) {
                map.put("name", b.getBusinessName());
                map.put("businessName", b.getBusinessName());
                map.put("avatar", b.getBusinessImg());
                map.put("businessImg", b.getBusinessImg());
                map.put("isOpen", b.getIsOpen());
            }
            return map;
        }

        if ("admin".equals(role)) {
            Admin a = adminMapper.selectById(uid);
            if (a != null) {
                map.put("name", a.getAdminName());
                map.put("adminName", a.getAdminName());
                map.put("avatar", a.getAvatar());
            }
            return map;
        }

        // 默认 customer
        Customer c = customerMapper.selectById(uid);
        if (c != null) {
            map.put("name", c.getUserName());
            map.put("userName", c.getUserName());
            map.put("avatar", c.getAvatar());
            map.put("balance", c.getBalance());
        }
        return map;
    }

    //按角色更新资料（customer/admin）
    public void updateProfile(Integer uid, String role, String userName, String avatar) {
        if (uid == null) throw new AppException(401, "未登录", 401);

        if ("admin".equals(role)) {
            Admin update = new Admin();
            update.setAdminId(uid);
            if (userName != null) update.setAdminName(userName);
            if (avatar != null) update.setAvatar(avatar);
            adminMapper.updateById(update);
            return;
        }

        if ("business".equals(role)) {
            throw new AppException(403, "商家资料请在店铺信息里修改", 403);
        }

        // customer
        Customer update = new Customer();
        update.setCustomerId(uid);
        if (userName != null) update.setUserName(userName);
        if (avatar != null) update.setAvatar(avatar);
        customerMapper.updateById(update);
    }

    /**
     * 旧版：兼容你项目里原来只更新 customer 的调用
     */
    public void updateProfile(Integer uid, String userName, String avatar) {
        updateProfile(uid, "customer", userName, avatar);
    }


    private Long tryParseLong(String s) {
        if (s == null) return null;
        try {
            return Long.parseLong(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private boolean passwordMatch(String raw, String stored) {
        if (raw == null || stored == null) return false;

        String db = stored.trim();
        if (db.startsWith("$2a$") || db.startsWith("$2b$") || db.startsWith("$2y$")) {
            return passwordEncoder.matches(raw, db);
        }
        return raw.equals(db);
    }
}
