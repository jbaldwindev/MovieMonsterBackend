package com.MovieMonster.demo.Security;

import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class SecurityConstants {
    public static final long JWT_EXPIRATION = 700000;

    @Value("${jwt.secret}")
    public String JWT_SECRET;

    public String getJwtSecret() {
        return JWT_SECRET;
    }

    public SecretKey getJwtSigningKey() {
        try {
            byte[] secretBytes = JWT_SECRET.getBytes(StandardCharsets.UTF_8);
            byte[] derivedKey = MessageDigest.getInstance("SHA-512").digest(secretBytes);
            return Keys.hmacShaKeyFor(derivedKey);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Unable to initialize JWT signing key", ex);
        }
    }
}
