package com.hanihome.hanihome_au_api.domain.entity;

import com.hanihome.hanihome_au_api.domain.enums.PropertyStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "property_status_history", indexes = {
    @Index(name = "idx_property_status_history_property_id", columnList = "propertyId"),
    @Index(name = "idx_property_status_history_created_date", columnList = "createdDate"),
    @Index(name = "idx_property_status_history_status", columnList = "newStatus")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PropertyStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long propertyId;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyStatus newStatus;

    @Column(nullable = false)
    private Long changedBy;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;
}