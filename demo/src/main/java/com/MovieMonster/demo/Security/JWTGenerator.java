package com.MovieMonster.demo.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JWTGenerator {
    @Autowired
    private SecurityConstants securityConstants;

    public String generateToken(Authentication authentication, long expirationMs) {
        return generateToken(authentication, expirationMs, "access");
    }

    public String generateToken(Authentication authentication, long expirationMs, String tokenType) {
        String username = authentication.getName();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("token_type", tokenType)
                .signWith(securityConstants.getJwtSigningKey())
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }

    public String getTokenType(String token) {
        Claims claims = parseClaims(token);
        return claims.get("token_type", String.class);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }


    public String generateTokenFromUsername(String username, long expirationMs) {
        return generateTokenFromUsername(username, expirationMs, "access");
    }

    public String generateTokenFromUsername(String username, long expirationMs, String tokenType) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("token_type", tokenType)
                .signWith(securityConstants.getJwtSigningKey())
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .setSigningKey(securityConstants.getJwtSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
