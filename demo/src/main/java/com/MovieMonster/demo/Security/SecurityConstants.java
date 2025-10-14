package com.MovieMonster.demo.Security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SecurityConstants {
    public static final long JWT_EXPIRATION = 700000;

    @Value("${jwt.secret}")
    public String JWT_SECRET;

    public String getJwtSecret() {
        return JWT_SECRET;
    }
}
