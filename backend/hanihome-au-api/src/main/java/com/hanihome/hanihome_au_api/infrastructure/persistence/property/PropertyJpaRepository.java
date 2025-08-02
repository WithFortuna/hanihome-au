package com.hanihome.hanihome_au_api.infrastructure.persistence.property;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyJpaRepository extends JpaRepository<PropertyJpaEntity, Long> {
    
    List<PropertyJpaEntity> findByOwnerId(Long ownerId);
    
    List<PropertyJpaEntity> findByStatus(PropertyJpaEntity.PropertyStatusEnum status);
    
    List<PropertyJpaEntity> findByPropertyType(PropertyJpaEntity.PropertyTypeEnum propertyType);
    
    @Query("SELECT p FROM PropertyJpaEntity p WHERE p.status = 'ACTIVE'")
    List<PropertyJpaEntity> findAvailableProperties();
    
    @Query(value = """
        SELECT * FROM properties p 
        WHERE p.latitude IS NOT NULL 
        AND p.longitude IS NOT NULL
        AND (6371 * acos(cos(radians(:latitude)) * cos(radians(p.latitude)) 
        * cos(radians(p.longitude) - radians(:longitude)) 
        + sin(radians(:latitude)) * sin(radians(p.latitude)))) < :radiusKm
        """, nativeQuery = true)
    List<PropertyJpaEntity> findNearByLocation(@Param("latitude") Double latitude, 
                                              @Param("longitude") Double longitude, 
                                              @Param("radiusKm") Double radiusKm);
}