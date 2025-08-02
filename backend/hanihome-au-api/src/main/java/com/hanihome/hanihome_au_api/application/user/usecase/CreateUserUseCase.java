package com.hanihome.hanihome_au_api.application.user.usecase;

import com.hanihome.hanihome_au_api.application.user.dto.CreateUserCommand;
import com.hanihome.hanihome_au_api.application.user.dto.UserResponseDto;
import com.hanihome.hanihome_au_api.domain.user.entity.User;
import com.hanihome.hanihome_au_api.domain.user.repository.UserRepository;
import com.hanihome.hanihome_au_api.domain.user.valueobject.Email;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserId;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateUserUseCase {
    private final UserRepository userRepository;

    public UserResponseDto execute(CreateUserCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Command cannot be null");
        }
        
        Email email = Email.of(command.getEmail());
        
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with email already exists: " + command.getEmail());
        }

        UserRole role;
        try {
            role = UserRole.valueOf(command.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid user role: " + command.getRole());
        }
        
        UserId userId = UserId.of(generateNewUserId());
        
        User user = User.create(userId, email, command.getName(), role);
        
        if (command.getPhoneNumber() != null && !command.getPhoneNumber().trim().isEmpty()) {
            user.updateProfile(command.getName(), command.getPhoneNumber());
        }

        User savedUser = userRepository.save(user);

        return mapToResponseDto(savedUser);
    }

    private Long generateNewUserId() {
        return System.currentTimeMillis();
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