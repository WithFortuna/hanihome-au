package com.hanihome.hanihome_au_api.security.oauth2;

import com.hanihome.hanihome_au_api.domain.user.entity.User;
import com.hanihome.hanihome_au_api.domain.user.valueobject.Email;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserId;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserRole;
import com.hanihome.hanihome_au_api.domain.user.valueobject.OAuthProvider;
import com.hanihome.hanihome_au_api.domain.user.repository.UserRepository;
import com.hanihome.hanihome_au_api.application.user.service.UserApplicationService;
import com.hanihome.hanihome_au_api.security.oauth2.user.OAuth2UserInfo;
import com.hanihome.hanihome_au_api.security.oauth2.user.OAuth2UserInfoFactory;
import com.hanihome.hanihome_au_api.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserApplicationService userApplicationService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        try {
            return processOAuth2User(userRequest, oauth2User);
        } catch (Exception e) {
            log.error("Error processing OAuth2 user", e);
            throw new OAuth2AuthenticationException("Processing OAuth2 user failed");
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuthProvider provider = OAuthProvider.fromRegistrationId(registrationId);
        
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oauth2User.getAttributes());

        if (userInfo.getEmail() == null || userInfo.getEmail().isEmpty()) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(Email.of(userInfo.getEmail()));
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            // Note: OAuth provider validation would be needed here when DDD User supports OAuth
            user = updateExistingUser(user, userInfo);
        } else {
            user = registerNewUser(userRequest, userInfo, provider);
        }

        return UserPrincipal.create(user, oauth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest userRequest, OAuth2UserInfo userInfo, OAuthProvider provider) {
        log.info("Registering new user with email: {}", userInfo.getEmail());
        
        // Generate new user ID (should ideally come from a domain service)
        UserId userId = UserId.of(System.currentTimeMillis());
        Email email = Email.of(userInfo.getEmail());
        UserRole role = UserRole.TENANT; // Default role
        
        User user = User.create(userId, email, userInfo.getName(), role);
        
        // Verify email since OAuth providers usually verify emails
        user.verifyEmail();
        
        // Record the initial login
        user.recordLogin();
        
        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo userInfo) {
        log.info("Updating existing user with email: {}", userInfo.getEmail());
        
        // Update profile information if changed
        if (!existingUser.getName().equals(userInfo.getName())) {
            existingUser.updateProfile(userInfo.getName(), existingUser.getPhoneNumber());
        }
        
        // Update last login time
        existingUser.recordLogin();
        
        // Ensure email is verified since they logged in via OAuth
        existingUser.verifyEmail();
        
        return userRepository.save(existingUser);
    }
}