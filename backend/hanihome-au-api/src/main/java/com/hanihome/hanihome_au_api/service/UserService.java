package com.hanihome.hanihome_au_api.service;

import com.hanihome.hanihome_au_api.application.user.service.UserApplicationService;
import com.hanihome.hanihome_au_api.application.user.dto.UserResponseDto;
import com.hanihome.hanihome_au_api.domain.user.entity.User;
import com.hanihome.hanihome_au_api.domain.user.repository.UserRepository;
import com.hanihome.hanihome_au_api.domain.user.valueobject.Email;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserId;
import com.hanihome.hanihome_au_api.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserApplicationService userApplicationService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(Email.of(email))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return UserPrincipal.create(user);
    }

    public UserPrincipal loadUserById(Long id) {
        User user = userRepository.findById(UserId.of(id))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        return UserPrincipal.create(user);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(UserId.of(id));
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(Email.of(email));
    }

    // Delegate business operations to application service
    @Transactional
    public UserResponseDto updateUserProfile(Long userId, String name, String phone) {
        return userApplicationService.updateUserProfile(userId, name, phone);
    }

    @Transactional
    public UserResponseDto updateUserRole(Long userId, String newRole) {
        return userApplicationService.updateUserRole(userId, newRole);
    }

    @Transactional
    public void verifyEmail(Long userId) {
        userApplicationService.verifyEmail(userId);
    }

    @Transactional  
    public void verifyPhone(Long userId) {
        userApplicationService.verifyPhone(userId);
    }

    @Transactional
    public void recordLogin(Long userId) {
        userApplicationService.recordLogin(userId);
    }

    public boolean existsByEmail(String email) {
        return userApplicationService.existsByEmail(email);
    }

    public boolean hasPermission(Long userId, String permission) {
        return userApplicationService.hasPermission(userId, permission);
    }

    public boolean canManageProperty(Long userId) {
        return userApplicationService.canManageProperty(userId);
    }
}