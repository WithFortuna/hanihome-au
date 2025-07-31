package com.hanihome.hanihome_au_api.security.oauth2;

import com.hanihome.hanihome_au_api.domain.entity.User;
import com.hanihome.hanihome_au_api.domain.enums.OAuthProvider;
import com.hanihome.hanihome_au_api.domain.enums.UserRole;
import com.hanihome.hanihome_au_api.repository.UserRepository;
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

        Optional<User> userOptional = userRepository.findByEmail(userInfo.getEmail());
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (!user.getOauthProvider().equals(provider)) {
                throw new OAuth2AuthenticationException(
                    "Looks like you're signed up with " + user.getOauthProvider() + 
                    " account. Please use your " + user.getOauthProvider() + " account to login."
                );
            }
            user = updateExistingUser(user, userInfo);
        } else {
            user = registerNewUser(userRequest, userInfo, provider);
        }

        return UserPrincipal.create(user, oauth2User.getAttributes());
    }

    private User registerNewUser(OAuth2UserRequest userRequest, OAuth2UserInfo userInfo, OAuthProvider provider) {
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

        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo userInfo) {
        log.info("Updating existing user with email: {}", userInfo.getEmail());
        
        // Update profile information if changed
        boolean updated = false;
        
        if (!existingUser.getName().equals(userInfo.getName())) {
            existingUser.setName(userInfo.getName());
            updated = true;
        }
        
        if (userInfo.getImageUrl() != null && !userInfo.getImageUrl().equals(existingUser.getProfileImageUrl())) {
            existingUser.setProfileImageUrl(userInfo.getImageUrl());
            updated = true;
        }

        // Update last login time
        existingUser.updateLastLoginAt();
        updated = true;

        // Ensure user is active
        if (!existingUser.getIsActive()) {
            existingUser.activate();
            updated = true;
        }

        if (updated) {
            return userRepository.save(existingUser);
        }
        
        return existingUser;
    }
}