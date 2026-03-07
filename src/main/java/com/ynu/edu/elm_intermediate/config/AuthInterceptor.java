package com.ynu.edu.elm_intermediate.config;

import com.ynu.edu.elm_intermediate.common.AppException;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 鉴权拦截器
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String ATTR_CUSTOMER_ID = "customerId";
    public static final String ATTR_ROLE = "role";
    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse resp, Object handler) {
        //放行浏览器自动发送的 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) return true;

        String auth = req.getHeader("Authorization");
        if (auth == null || auth.isBlank() || !auth.startsWith("Bearer ")) {
            throw new AppException(401, "未携带 Token 或格式错误，请先登录", 401);
        }

        String token = auth.substring("Bearer ".length()).trim();

        try {
            Claims claims = jwtUtil.parseClaims(token);
            // 防止 JWT 解析为 Long 导致 Integer 强转报错
            Object idObj = claims.get("id");
            Integer id = null;
            if (idObj instanceof Integer) {
                id = (Integer) idObj;
            } else if (idObj instanceof Number) {
                id = ((Number) idObj).intValue();
            }

            // 兼容旧 token
            if (id == null) {
                Object cidObj = claims.get("customerId");
                if (cidObj instanceof Number) {
                    id = ((Number) cidObj).intValue();
                }
            }

            if (id == null) {
                throw new AppException(401, "Token 无效", 401);
            }

            String role = claims.get("role", String.class);
            if (role == null || role.isBlank()) {
                role = claims.getSubject();
            }
            if (role == null || role.isBlank()) {
                role = "customer";
            }

            // 角色访问控制
            String uri = req.getRequestURI();
            if ("admin".equals(role)) {
                if (!isAdminAllowed(uri)) {
                    throw new AppException(403, "管理员无权操作该接口", 403);
                }
            } else {
                if (uri != null && uri.startsWith("/admin")) {
                    throw new AppException(403, "无权访问管理员接口", 403);
                }
            }

            req.setAttribute(ATTR_CUSTOMER_ID, id);
            req.setAttribute(ATTR_ROLE, role);
            req.setAttribute("uid", id);
            req.setAttribute("role", role);

            return true;
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace(); // 打印错误堆栈以便调试
            throw new AppException(401, "登录已过期，请重新登录", 401);
        }
    }

    private boolean isAdminAllowed(String uri) {
        if (uri == null) return false;
        if (uri.startsWith("/admin")) return true;
        if ("/user/info".equals(uri)) return true;
        if ("/user/update".equals(uri)) return true;
        if ("/user/logout".equals(uri)) return true;
        if ("/upload/avatar".equals(uri)) return true;
        return false;
    }
}