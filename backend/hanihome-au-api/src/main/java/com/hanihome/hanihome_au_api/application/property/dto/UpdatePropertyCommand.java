package com.hanihome.hanihome_au_api.application.property.dto;

import com.hanihome.hanihome_au_api.domain.property.valueobject.PropertySpecs;
import com.hanihome.hanihome_au_api.domain.shared.valueobject.Money;

/**
 * Command object for updating property details
 * Contains only the fields that can be updated
 */
public class UpdatePropertyCommand {
    private final String title;
    private final String description;
    private final PropertySpecs specs;
    private final Money rentPrice;
    private final Money depositAmount;

    public UpdatePropertyCommand(String title, String description, PropertySpecs specs, 
                               Money rentPrice, Money depositAmount) {
        this.title = title;
        this.description = description;
        this.specs = specs;
        this.rentPrice = rentPrice;
        this.depositAmount = depositAmount;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public PropertySpecs getSpecs() {
        return specs;
    }

    public Money getRentPrice() {
        return rentPrice;
    }

    public Money getDepositAmount() {
        return depositAmount;
    }
}