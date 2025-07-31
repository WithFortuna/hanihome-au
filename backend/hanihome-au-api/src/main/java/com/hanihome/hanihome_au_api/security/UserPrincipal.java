package com.hanihome.hanihome_au_api.security;

import com.hanihome.hanihome_au_api.domain.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class UserPrincipal implements OAuth2User, UserDetails {
    private Long id;
    private String email;
    private String name;
    private String role;
    private boolean isActive;
    private Collection<? extends GrantedAuthority> authorities;
    private Map<String, Object> attributes;

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority(user.getRole().getAuthority())
        );

        return new UserPrincipal(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getRole().name(),
            user.getIsActive(),
            authorities,
            null
        );
    }

    public static UserPrincipal create(User user, Map<String, Object> attributes) {
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        return new UserPrincipal(
            userPrincipal.getId(),
            userPrincipal.getEmail(),
            userPrincipal.getName(),
            userPrincipal.getRole(),
            userPrincipal.isActive(),
            userPrincipal.getAuthorities(),
            attributes
        );
    }

    @Override
    public String getPassword() {
        return null; // OAuth2 users don't have passwords
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return String.valueOf(id);
    }
}