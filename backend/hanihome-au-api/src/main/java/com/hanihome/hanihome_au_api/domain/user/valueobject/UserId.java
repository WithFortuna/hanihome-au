package com.hanihome.hanihome_au_api.domain.user.valueobject;

import com.hanihome.hanihome_au_api.domain.shared.valueobject.BaseId;

public class UserId extends BaseId<Long> {
    
    public UserId(Long value) {
        super(value);
    }
    
    public static UserId of(Long value) {
        return new UserId(value);
    }
}