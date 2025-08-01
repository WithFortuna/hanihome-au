package com.hanihome.platform.security.infrastructure.oauth2;

import com.hanihome.platform.user.domain.entity.User;
import com.hanihome.platform.user.domain.enums.OAuthProvider;
import com.hanihome.platform.user.domain.enums.UserRole;
import com.hanihome.platform.user.domain.repository.UserRepository;
import com.hanihome.platform.security.infrastructure.oauth2.user.OAuth2UserInfo;
import com.hanihome.platform.security.infrastructure.oauth2.user.OAuth2UserInfoFactory;
import com.hanihome.platform.user.infrastructure.security.UserPrincipal;
import com.hanihome.platform.system.infrastructure.security.SecurityAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Custom OAuth2 User Service
 * Migrated to new DDD structure with enhanced security auditing
 * Handles OAuth2 user authentication and registration
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final SecurityAuditService securityAuditService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oauth2User);
        } catch (Exception e) {
            log.error("Error processing OAuth2 user", e);
            
            // Log security event for failed OAuth2 processing
            String clientIp = getClientIpAddress();
            securityAuditService.logSecurityEvent(null, "OAUTH2_PROCESSING_ERROR", 
                "Failed to process OAuth2 user authentication", 
                "Provider: " + userRequest.getClientRegistration().getRegistrationId() + 
                ", IP: " + clientIp + ", Error: " + e.getMessage());
            
            throw new OAuth2AuthenticationException("Processing OAuth2 user failed");
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuthProvider provider = OAuthProvider.fromRegistrationId(registrationId);
        String clientIp = getClientIpAddress();
        
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oauth2User.getAttributes());

        if (userInfo.getEmail() == null || userInfo.getEmail().isEmpty()) {
            securityAuditService.logSecurityEvent(null, "OAUTH2_EMAIL_MISSING", 
                "OAuth2 provider did not provide email", 
                "Provider: " + registrationId + ", IP: " + clientIp);
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(userInfo.getEmail());
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (!user.getOauthProvider().equals(provider)) {
                securityAuditService.logSecurityEvent(user.getId(), "OAUTH2_PROVIDER_MISMATCH", 
                    "User attempted to login with different OAuth2 provider", 
                    "Expected: " + user.getOauthProvider() + ", Attempted: " + provider + ", IP: " + clientIp);
                
                throw new OAuth2AuthenticationException(
                    "Looks like you're signed up with " + user.getOauthProvider() + 
                    " account. Please use your " + user.getOauthProvider() + " account to login."
                );
            }
            user = updateExistingUser(user, userInfo, clientIp);
        } else {
            user = registerNewUser(userRequest, userInfo, provider, clientIp);
        }

        // Log successful OAuth2 authentication
        securityAuditService.logOAuth2Event(user.getEmail(), provider.name(), 
            "OAUTH2_LOGIN_SUCCESS", clientIp, true);

        return UserPrincipal.create(user, oauth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest userRequest, OAuth2UserInfo userInfo, 
                               OAuthProvider provider, String clientIp) {
        log.info("Registering new user with email: {}", userInfo.getEmail());
        
        User user = User.builder()
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .profileImageUrl(userInfo.getImageUrl())
                .oauthProvider(provider)
                .oauthProviderId(userInfo.getId())
                .role(UserRole.TENANT) // Default role
                .isActive(true)
                .isEmailVerified(true) // OAuth providers usually verify emails
                .lastLoginAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        
        // Log new user registration
        securityAuditService.logSecurityEvent(savedUser.getId(), "USER_REGISTERED", 
            "New user registered via OAuth2", 
            "Provider: " + provider + ", IP: " + clientIp);
        
        securityAuditService.logUserAction(savedUser.getId(), "ACCOUNT_CREATED", 
            "Account created via OAuth2 registration", 
            "Provider: " + provider);

        return savedUser;
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo userInfo, String clientIp) {
        log.info("Updating existing user with email: {}", userInfo.getEmail());
        
        // Update profile information if changed
        boolean updated = false;
        StringBuilder updateDetails = new StringBuilder();
        
        if (!existingUser.getName().equals(userInfo.getName())) {
            existingUser.setName(userInfo.getName());
            updateDetails.append("Name updated; ");
            updated = true;
        }
        
        if (userInfo.getImageUrl() != null && !userInfo.getImageUrl().equals(existingUser.getProfileImageUrl())) {
            existingUser.setProfileImageUrl(userInfo.getImageUrl());
            updateDetails.append("Profile image updated; ");
            updated = true;
        }

        // Update last login time
        existingUser.updateLastLoginAt();
        updateDetails.append("Last login updated; ");
        updated = true;

        // Ensure user is active
        if (!existingUser.getIsActive()) {
            existingUser.activate();
            updateDetails.append("Account reactivated; ");
            updated = true;
            
            // Log account reactivation
            securityAuditService.logSecurityEvent(existingUser.getId(), "ACCOUNT_REACTIVATED", 
                "User account reactivated during OAuth2 login", 
                "Provider: " + existingUser.getOauthProvider() + ", IP: " + clientIp);
        }

        if (updated) {
            User savedUser = userRepository.save(existingUser);
            
            // Log profile updates if any significant changes occurred
            if (updateDetails.length() > 0) {
                securityAuditService.logUserAction(savedUser.getId(), "PROFILE_UPDATED", 
                    "Profile updated during OAuth2 login", updateDetails.toString());
            }
            
            return savedUser;
        }
        
        return existingUser;
    }

    /**
     * Extract client IP address from current request
     */
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attributes.getRequest();
            
            // Check various headers for real IP
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            
            String xRealIp = request.getHeader("X-Real-IP");
            if (xRealIp != null && !xRealIp.isEmpty()) {
                return xRealIp;
            }
            
            return request.getRemoteAddr();
        } catch (Exception e) {
            log.debug("Could not extract client IP address: {}", e.getMessage());
            return "unknown";
        }
    }
}