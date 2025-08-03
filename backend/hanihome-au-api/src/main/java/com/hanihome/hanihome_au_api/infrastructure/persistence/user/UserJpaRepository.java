package com.hanihome.hanihome_au_api.infrastructure.persistence.user;

import com.hanihome.hanihome_au_api.infrastructure.persistence.user.UserJpaEntity.OAuthProviderEnum;
import com.hanihome.hanihome_au_api.infrastructure.persistence.user.UserJpaEntity.UserRoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
    
    Optional<UserJpaEntity> findByEmail(String email);
    
    Optional<UserJpaEntity> findByOauthProviderAndOauthProviderId(OAuthProviderEnum oauthProvider, String oauthProviderId);
    
    List<UserJpaEntity> findByRole(UserRoleEnum role);
    
    List<UserJpaEntity> findByIsActiveTrue();
    
    List<UserJpaEntity> findByIsActiveFalse();
    
    List<UserJpaEntity> findByIsEmailVerifiedFalse();
    
    @Query("SELECT u FROM UserJpaEntity u WHERE u.lastLoginAt < :before")
    List<UserJpaEntity> findInactiveUsersSince(@Param("before") LocalDateTime before);
    
    @Query("SELECT u FROM UserJpaEntity u WHERE u.createdAt >= :from AND u.createdAt <= :to")
    List<UserJpaEntity> findUsersCreatedBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    
    @Query("SELECT COUNT(u) FROM UserJpaEntity u WHERE u.role = :role AND u.isActive = true")
    long countActiveUsersByRole(@Param("role") UserRoleEnum role);
    
    @Query("SELECT u FROM UserJpaEntity u WHERE u.name LIKE %:keyword% OR u.email LIKE %:keyword%")
    List<UserJpaEntity> searchByKeyword(@Param("keyword") String keyword);
    
    boolean existsByEmail(String email);
    
    boolean existsByOauthProviderAndOauthProviderId(OAuthProviderEnum oauthProvider, String oauthProviderId);
    
    @Query("SELECT u FROM UserJpaEntity u WHERE u.role IN :roles AND u.isActive = true")
    List<UserJpaEntity> findActiveUsersByRoles(@Param("roles") List<UserRoleEnum> roles);
}