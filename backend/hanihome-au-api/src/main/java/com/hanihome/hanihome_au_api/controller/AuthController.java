package com.hanihome.hanihome_au_api.controller;

import com.hanihome.hanihome_au_api.dto.request.RefreshTokenRequest;
import com.hanihome.hanihome_au_api.dto.response.ApiResponse;
import com.hanihome.hanihome_au_api.dto.response.JwtAuthenticationResponse;
import com.hanihome.hanihome_au_api.security.UserPrincipal;
import com.hanihome.hanihome_au_api.security.jwt.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        try {
            String newAccessToken = jwtTokenProvider.refreshAccessToken(request.getRefreshToken());
            Long userId = jwtTokenProvider.getUserIdFromTokenAsLong(request.getRefreshToken());
            String role = jwtTokenProvider.getRoleFromToken(newAccessToken);

            JwtAuthenticationResponse response = JwtAuthenticationResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(request.getRefreshToken())
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getAccessTokenExpiration())
                    .userId(userId)
                    .role(role)
                    .build();

            return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
        } catch (JwtException e) {
            log.error("Token refresh failed", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Token refresh failed: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        
        if (userPrincipal != null) {
            // Revoke all user tokens
            jwtTokenProvider.revokeAllUserTokens(userPrincipal.getId());
            
            // Blacklist current access token if provided
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String accessToken = authorizationHeader.substring(7);
                jwtTokenProvider.blacklistToken(accessToken);
            }
            
            log.info("User logged out: {}", userPrincipal.getId());
        }

        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserPrincipal>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        return ResponseEntity.ok(ApiResponse.success("Current user retrieved successfully", userPrincipal));
    }

    @PostMapping("/revoke")
    public ResponseEntity<ApiResponse<Void>> revokeRefreshToken(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        if (userPrincipal != null) {
            jwtTokenProvider.revokeRefreshToken(userPrincipal.getId());
            log.info("Refresh token revoked for user: {}", userPrincipal.getId());
        }

        return ResponseEntity.ok(ApiResponse.success("Refresh token revoked successfully"));
    }
}