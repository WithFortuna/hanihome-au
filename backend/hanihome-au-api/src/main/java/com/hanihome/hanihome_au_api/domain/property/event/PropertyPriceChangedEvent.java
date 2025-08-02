package com.hanihome.hanihome_au_api.domain.property.event;

import com.hanihome.hanihome_au_api.domain.property.valueobject.PropertyId;
import com.hanihome.hanihome_au_api.domain.shared.valueobject.Money;

import java.time.LocalDateTime;

/**
 * Domain event raised when property price changes significantly
 */
public class PropertyPriceChangedEvent {
    private final PropertyId propertyId;
    private final Money oldPrice;
    private final Money newPrice;
    private final LocalDateTime occurredAt;

    public PropertyPriceChangedEvent(PropertyId propertyId, Money oldPrice, Money newPrice) {
        this.propertyId = propertyId;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.occurredAt = LocalDateTime.now();
    }

    public PropertyId getPropertyId() {
        return propertyId;
    }

    public Money getOldPrice() {
        return oldPrice;
    }

    public Money getNewPrice() {
        return newPrice;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public Money getPriceChange() {
        return newPrice.subtract(oldPrice);
    }

    public boolean isPriceIncrease() {
        return newPrice.isGreaterThan(oldPrice);
    }
}