package com.hanihome.hanihome_au_api.repository;

import com.hanihome.hanihome_au_api.domain.entity.User;
import com.hanihome.hanihome_au_api.domain.enums.OAuthProvider;
import com.hanihome.hanihome_au_api.domain.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByOauthProviderAndOauthProviderId(OAuthProvider oauthProvider, String oauthProviderId);

    List<User> findByRole(UserRole role);

    List<User> findByIsActiveTrue();

    List<User> findByIsActiveFalse();

    List<User> findByIsEmailVerifiedFalse();

    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :before")
    List<User> findInactiveUsersSince(@Param("before") LocalDateTime before);

    @Query("SELECT u FROM User u WHERE u.createdAt >= :from AND u.createdAt <= :to")
    List<User> findUsersCreatedBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.isActive = true")
    long countActiveUsersByRole(@Param("role") UserRole role);

    @Query("SELECT u FROM User u WHERE u.name LIKE %:keyword% OR u.email LIKE %:keyword%")
    List<User> searchByKeyword(@Param("keyword") String keyword);

    boolean existsByEmail(String email);

    boolean existsByOauthProviderAndOauthProviderId(OAuthProvider oauthProvider, String oauthProviderId);

    @Query("SELECT u FROM User u WHERE u.role IN :roles AND u.isActive = true")
    List<User> findActiveUsersByRoles(@Param("roles") List<UserRole> roles);
}