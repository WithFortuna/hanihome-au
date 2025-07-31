package com.hanihome.api.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final RedisTemplate<String, String> redisTemplate;

    public JwtTokenProvider(
            @Value("${jwt.secret:myDefaultSecretKeyForJWTTokenGeneration123456789}") String jwtSecret,
            @Value("${jwt.expiration:86400000}") long accessTokenExpiration,
            @Value("${jwt.refresh-expiration:604800000}") long refreshTokenExpiration,
            RedisTemplate<String, String> redisTemplate) {
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.redisTemplate = redisTemplate;
    }

    public String createAccessToken(String email, String role, Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpirationTime(expiryDate)
                .claim("role", role)
                .claim("userId", userId)
                .claim("type", "ACCESS")
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public String createRefreshToken(String email, Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpiration);

        String refreshToken = Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpirationTime(expiryDate)
                .claim("userId", userId)
                .claim("type", "REFRESH")
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        // Store refresh token in Redis with expiration
        String redisKey = "refresh_token:" + userId;
        redisTemplate.opsForValue().set(redisKey, refreshToken, 
                Duration.ofMillis(refreshTokenExpiration));

        return refreshToken;
    }

    public String getUserEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("userId", Long.class);
    }

    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("role", String.class);
    }

    public String getTokenTypeFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("type", String.class);
    }

    public boolean validateToken(String token) {
        try {
            // Check if token is blacklisted
            if (isTokenBlacklisted(token)) {
                log.warn("Token is blacklisted");
                return false;
            }

            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty");
        }
        return false;
    }

    public boolean validateRefreshToken(String refreshToken, Long userId) {
        try {
            if (!validateToken(refreshToken)) {
                return false;
            }

            String tokenType = getTokenTypeFromToken(refreshToken);
            if (!"REFRESH".equals(tokenType)) {
                log.warn("Token is not a refresh token");
                return false;
            }

            // Check if refresh token exists in Redis
            String redisKey = "refresh_token:" + userId;
            String storedToken = redisTemplate.opsForValue().get(redisKey);
            
            return refreshToken.equals(storedToken);
        } catch (Exception ex) {
            log.error("Error validating refresh token", ex);
            return false;
        }
    }

    public String refreshAccessToken(String refreshToken) {
        if (!validateToken(refreshToken)) {
            throw new JwtException("Invalid refresh token");
        }

        String email = getUserEmailFromToken(refreshToken);
        Long userId = getUserIdFromToken(refreshToken);
        
        if (!validateRefreshToken(refreshToken, userId)) {
            throw new JwtException("Refresh token not found or expired");
        }

        // Get user role - you might want to fetch fresh from database
        String role = "TENANT"; // Default - in production, fetch from DB
        
        return createAccessToken(email, role, userId);
    }

    public void blacklistToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Date expiration = claims.getExpiration();
            long timeToExpiry = expiration.getTime() - System.currentTimeMillis();
            
            if (timeToExpiry > 0) {
                String blacklistKey = "blacklist:" + token;
                redisTemplate.opsForValue().set(blacklistKey, "true", timeToExpiry, TimeUnit.MILLISECONDS);
            }
        } catch (Exception ex) {
            log.error("Error blacklisting token", ex);
        }
    }

    public void revokeRefreshToken(Long userId) {
        String redisKey = "refresh_token:" + userId;
        redisTemplate.delete(redisKey);
    }

    public void revokeAllUserTokens(Long userId) {
        // Revoke refresh token
        revokeRefreshToken(userId);
        
        // Add user to revoked users list (for access token validation)
        String revokedKey = "revoked_user:" + userId;
        redisTemplate.opsForValue().set(revokedKey, "true", accessTokenExpiration, TimeUnit.MILLISECONDS);
    }

    private boolean isTokenBlacklisted(String token) {
        String blacklistKey = "blacklist:" + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
    }

    private boolean isUserRevoked(Long userId) {
        String revokedKey = "revoked_user:" + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(revokedKey));
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}