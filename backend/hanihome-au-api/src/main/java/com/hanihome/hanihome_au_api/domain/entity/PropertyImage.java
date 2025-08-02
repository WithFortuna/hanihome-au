package com.hanihome.hanihome_au_api.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "property_images", indexes = {
    @Index(name = "idx_property_id", columnList = "propertyId"),
    @Index(name = "idx_image_order", columnList = "imageOrder")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PropertyImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "property_id", nullable = false)
    private Long propertyId;


    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(length = 500)
    private String thumbnailUrl;

    @Column(name = "image_order", nullable = false)
    @Builder.Default
    private Integer imageOrder = 0;

    @Column(length = 200)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isMain = false;

    @Column
    private Long fileSize;

    @Column(length = 50)
    private String contentType;

    @Column(length = 100)
    private String originalFileName;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    public void setAsMain() {
        this.isMain = true;
    }

    public void unsetAsMain() {
        this.isMain = false;
    }
}