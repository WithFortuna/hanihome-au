package com.hanihome.hanihome_au_api.domain.user.repository;

import com.hanihome.hanihome_au_api.domain.user.entity.User;
import com.hanihome.hanihome_au_api.domain.user.valueobject.Email;
import com.hanihome.hanihome_au_api.domain.user.valueobject.UserId;

import java.util.Optional;

public interface UserRepository {
    
    User save(User user);
    
    Optional<User> findById(UserId id);
    
    Optional<User> findByEmail(Email email);
    
    boolean existsByEmail(Email email);
    
    void delete(User user);
}