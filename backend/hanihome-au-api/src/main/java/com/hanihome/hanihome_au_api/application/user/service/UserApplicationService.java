package com.hanihome.hanihome_au_api.application.user.service;

import com.hanihome.hanihome_au_api.application.user.dto.CreateUserCommand;
import com.hanihome.hanihome_au_api.application.user.dto.UserResponseDto;
import com.hanihome.hanihome_au_api.application.user.usecase.CreateUserUseCase;
import com.hanihome.hanihome_au_api.domain.user.entity.User;
import com.hanihome.hanihome_au_api.domain.user.repository.UserRepository;
import com.hanihome.hanihome_au_api.domain.user.valueobject.Email;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserId;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserRole;
import com.hanihome.hanihome_au_api.domain.user.exception.UserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserApplicationService {

    private final UserRepository userRepository;
    private final CreateUserUseCase createUserUseCase;

    public UserResponseDto createUser(CreateUserCommand command) {
        return createUserUseCase.execute(command);
    }

    public Optional<UserResponseDto> findById(Long id) {
        return userRepository.findById(UserId.of(id))
                .map(this::mapToResponseDto);
    }

    public Optional<UserResponseDto> findByEmail(String email) {
        return userRepository.findByEmail(Email.of(email))
                .map(this::mapToResponseDto);
    }

    public List<UserResponseDto> findByRole(String role) {
        UserRole userRole = UserRole.valueOf(role.toUpperCase());
        // Note: This would need to be implemented in UserRepository if needed
        throw new UnsupportedOperationException("findByRole not yet implemented in DDD UserRepository");
    }

    public List<UserResponseDto> findActiveUsers() {
        // Note: This would need to be implemented in UserRepository if needed  
        throw new UnsupportedOperationException("findActiveUsers not yet implemented in DDD UserRepository");
    }

    @Transactional
    public UserResponseDto updateUserProfile(Long userId, String name, String phoneNumber) {
        User user = userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> new UserException("User not found with id: " + userId));

        user.updateProfile(name, phoneNumber);
        User savedUser = userRepository.save(user);
        
        log.info("User profile updated: {}", userId);
        return mapToResponseDto(savedUser);
    }

    @Transactional  
    public UserResponseDto updateUserRole(Long userId, String newRole) {
        User user = userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> new UserException("User not found with id: " + userId));

        UserRole role = UserRole.valueOf(newRole.toUpperCase());
        user.changeRole(role);
        User savedUser = userRepository.save(user);
        
        log.info("User role updated: {} to {}", userId, newRole);
        return mapToResponseDto(savedUser);
    }

    @Transactional
    public void verifyEmail(Long userId) {
        User user = userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> new UserException("User not found with id: " + userId));

        user.verifyEmail();
        userRepository.save(user);
        log.info("User email verified: {}", userId);
    }

    @Transactional
    public void verifyPhone(Long userId) {
        User user = userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> new UserException("User not found with id: " + userId));

        user.verifyPhone();
        userRepository.save(user);
        log.info("User phone verified: {}", userId);
    }

    @Transactional
    public void recordLogin(Long userId) {
        User user = userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> new UserException("User not found with id: " + userId));

        user.recordLogin();
        userRepository.save(user);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(Email.of(email));
    }

    public boolean hasPermission(Long userId, String permission) {
        User user = userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> new UserException("User not found with id: " + userId));
        
        return user.hasPermission(permission);
    }

    public boolean canManageProperty(Long userId) {
        User user = userRepository.findById(UserId.of(userId))
                .orElseThrow(() -> new UserException("User not found with id: " + userId));
        
        return user.canManageProperty();
    }

    private UserResponseDto mapToResponseDto(User user) {
        return new UserResponseDto(
            user.getId().getValue(),
            user.getEmail().getValue(),
            user.getName(),
            user.getPhoneNumber(),
            user.getRole().name(),
            user.isEmailVerified(),
            user.isPhoneVerified(),
            user.getCreatedAt(),
            user.getLastLoginAt()
        );
    }
}