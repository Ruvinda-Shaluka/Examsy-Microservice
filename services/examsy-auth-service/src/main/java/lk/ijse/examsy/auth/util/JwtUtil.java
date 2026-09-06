package lk.ijse.examsy.auth.util;

import io.jsonwebtoken.Claims;
import lk.ijse.examsy.common.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adapter delegating to centralized JwtTokenProvider from examsy-common.
 * Guarantees uniform secret validation and zero hardcoded defaults.
 */
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtTokenProvider tokenProvider;

    public String generateToken(String username, String role, Integer userId) {
        return tokenProvider.generateToken(username, role, userId);
    }

    public String generateToken(String username) {
        return tokenProvider.generateToken(username);
    }

    public boolean validateToken(String token) {
        return tokenProvider.validateToken(token);
    }

    public Claims extractAllClaims(String token) {
        return tokenProvider.getClaimsFromToken(token);
    }

    public String extractUsername(String token) {
        return tokenProvider.getUsernameFromToken(token);
    }

    public String extractRole(String token) {
        return tokenProvider.getRoleFromToken(token);
    }

    public Integer extractUserId(String token) {
        return tokenProvider.getUserIdFromToken(token);
    }
}
