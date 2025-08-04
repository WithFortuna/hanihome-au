package com.hanihome.hanihome_au_api.domain.entity;

import com.hanihome.hanihome_au_api.domain.shared.entity.AggregateRoot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "property_favorites", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "property_id"}),
       indexes = {
           @Index(name = "idx_property_favorites_user_id", columnList = "user_id"),
           @Index(name = "idx_property_favorites_property_id", columnList = "property_id"),
           @Index(name = "idx_property_favorites_created_at", columnList = "created_at")
       })
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PropertyFavorite extends AggregateRoot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "property_id", nullable = false)
    private Long propertyId;

    @Column(name = "category")
    private String category;

    @Column(name = "notes", length = 500)
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "notification_enabled", nullable = false)
    private Boolean notificationEnabled = true;

    public PropertyFavorite(Long userId, Long propertyId) {
        this.userId = userId;
        this.propertyId = propertyId;
        this.notificationEnabled = true;
    }

    public PropertyFavorite(Long userId, Long propertyId, String category, String notes) {
        this(userId, propertyId);
        this.category = category;
        this.notes = notes;
    }

    public void updateCategory(String category) {
        this.category = category;
    }

    public void updateNotes(String notes) {
        this.notes = notes;
    }

    public void enableNotification() {
        this.notificationEnabled = true;
    }

    public void disableNotification() {
        this.notificationEnabled = false;
    }
}