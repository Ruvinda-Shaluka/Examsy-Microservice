package lk.ijse.examsy.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;

/**
 * Enterprise JWT Token Provider for cryptographic token validation,
 * claim extraction, and signature verification.
 * Follows OWASP recommendations: zero hardcoded secrets in source code,
 * fail-fast startup on secret deficiency, and explicit token error telemetry.
 */
@Component
@Slf4j
public class JwtTokenProvider {

    // Strictly injected from Config Server or environment variable; no hardcoded in-code fallback!
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private long expiration;

    private Key signingKey;

    public String generateToken(String username, String role, Integer userId) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .claim("userId", userId)
                .setIssuedAt(new java.util.Date())
                .setExpiration(new java.util.Date(System.currentTimeMillis() + expiration))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new java.util.Date())
                .setExpiration(new java.util.Date(System.currentTimeMillis() + expiration))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    @PostConstruct
    public void validateAndInitializeKey() {
        if (jwtSecret == null || jwtSecret.trim().length() < 32) {
            log.error("FATAL: 'jwt.secret' is missing or weaker than 256 bits (32 characters)!");
            throw new IllegalStateException(
                    "FATAL: 'jwt.secret' must be configured via Config Server or environment variable (JWT_SECRET) " +
                    "and must be at least 256 bits (32 characters) long for secure HMAC-SHA256 signing.");
        }
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.trim().getBytes(StandardCharsets.UTF_8));
        log.info("JWT Signing Key initialized successfully with 256-bit cryptographic strength.");
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("Invalid JWT signature or malformed token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token format: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty or null: {}", e.getMessage());
        }
        return false;
    }

    public Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public String getRoleFromToken(String token) {
        Object role = getClaimsFromToken(token).get("role");
        return role != null ? role.toString() : null;
    }

    public Integer getUserIdFromToken(String token) {
        Object userId = getClaimsFromToken(token).get("userId");
        if (userId instanceof Number) {
            return ((Number) userId).intValue();
        }
        return null;
    }
}
