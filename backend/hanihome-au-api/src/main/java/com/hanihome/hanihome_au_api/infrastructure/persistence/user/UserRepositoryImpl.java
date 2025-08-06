package com.hanihome.hanihome_au_api.infrastructure.persistence.user;

import com.hanihome.hanihome_au_api.domain.user.entity.User;
import com.hanihome.hanihome_au_api.domain.user.repository.UserRepository;
import com.hanihome.hanihome_au_api.domain.user.valueobject.Email;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserId;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserRole;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserRepositoryImpl implements UserRepository {
    
    private final UserJpaRepository userJpaRepository;

    public UserRepositoryImpl(UserJpaRepository userJpaRepository) {
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = mapToEntity(user);
        UserJpaEntity savedEntity = userJpaRepository.save(entity);
        return mapToDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(UserId id) {
        return userJpaRepository.findById(id.getValue())
                .map(this::mapToDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return userJpaRepository.findByEmail(email.getValue())
                .map(this::mapToDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return userJpaRepository.existsByEmail(email.getValue());
    }

    @Override
    public long count() {
        return userJpaRepository.count();
    }

    @Override
    public void delete(User user) {
        userJpaRepository.deleteById(user.getId().getValue());
    }

    private UserJpaEntity mapToEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId().getValue());
        entity.setEmail(user.getEmail().getValue());
        entity.setName(user.getName());
        entity.setPhone(user.getPhoneNumber());
        entity.setRole(UserJpaEntity.UserRoleEnum.valueOf(user.getRole().name()));
        entity.setIsEmailVerified(user.isEmailVerified());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        entity.setLastLoginAt(user.getLastLoginAt());
        
        // Set default OAuth provider since domain doesn't have these yet
        entity.setOauthProvider(UserJpaEntity.OAuthProviderEnum.GOOGLE);
        entity.setOauthProviderId("default");
        entity.setIsActive(true);
        
        return entity;
    }

    private User mapToDomain(UserJpaEntity entity) {
        UserId userId = UserId.of(entity.getId());
        Email email = Email.of(entity.getEmail());
        UserRole role = UserRole.valueOf(entity.getRole().name());
        
        User user = User.create(userId, email, entity.getName(), role);
        
        if (entity.getPhone() != null) {
            user.updateProfile(entity.getName(), entity.getPhone());
        }
        
        if (entity.getIsEmailVerified()) {
            user.verifyEmail();
        }
        
        if (entity.getLastLoginAt() != null) {
            user.recordLogin();
        }
        
        return user;
    }
}