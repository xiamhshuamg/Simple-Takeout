package com.ynu.edu.elm_intermediate.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private final JwtProperties props;

    public JwtUtil(JwtProperties props) {

        this.props = props;
    }

    public String generateToken(int id, long account, String role) {
        Date now = new Date();// 获取当前时间
        Date exp = new Date(now.getTime() + props.getExpireSeconds() * 1000);

        return Jwts.builder()
                .setSubject(role) //存放角色
                .setIssuedAt(now)//签发时间
                .setExpiration(exp)
                .claim("id", id)           // 用户ID
                .claim("customerId", id)
                .claim("role", role)       //明确存入角色
                .claim("account", account)//账号
                .signWith(Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();//生成字符串
    }
    //解析token
    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
