package com.ynu.edu.elm_intermediate.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 全局配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    public WebConfig(AuthInterceptor authInterceptor) {
        this.authInterceptor = authInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册权限拦截器
        registry.addInterceptor(authInterceptor)
                .addPathPatterns(
                        "/order/**",
                        "/address/**",
                        "/user/**",
                        "/business/**",
                        "/admin/**",
                        // 只拦截上传接口，不拦截 /upload/** 静态资源（图片访问）
                        "/upload/avatar",
                        "/upload/food",
                        "/upload/common"
                )
                // 排除不需要登录的接口
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        // 商家公共接口
                        "/business/list",
                        "/business/*/foods"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 上传静态资源访问
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:C:/elm_upload/");
    }
}
