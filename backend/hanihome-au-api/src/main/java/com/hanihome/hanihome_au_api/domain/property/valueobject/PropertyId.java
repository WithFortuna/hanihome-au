package com.hanihome.hanihome_au_api.domain.property.valueobject;

import com.hanihome.hanihome_au_api.domain.shared.valueobject.BaseId;

public class PropertyId extends BaseId<Long> {
    
    public PropertyId(Long value) {
        super(value);
    }
    
    public static PropertyId of(Long value) {
        return new PropertyId(value);
    }
}