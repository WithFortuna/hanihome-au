package com.hanihome.hanihome_au_api.infrastructure.persistence.property;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyJpaRepository extends JpaRepository<PropertyJpaEntity, Long> {
    
    List<PropertyJpaEntity> findByLandlordId(Long landlordId);
    
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
    
    @Query("SELECT p FROM PropertyJpaEntity p WHERE p.rentPrice BETWEEN :minBudget AND :maxBudget")
    List<PropertyJpaEntity> findByBudgetRange(@Param("minBudget") Double minBudget, 
                                             @Param("maxBudget") Double maxBudget);
    
    @Query("""
        SELECT p FROM PropertyJpaEntity p 
        WHERE p.bedrooms >= :minBedrooms 
        AND p.bathrooms >= :minBathrooms 
        AND p.floorArea >= :minFloorArea 
        AND p.rentPrice <= :maxBudget
        """)
    List<PropertyJpaEntity> findBySpecsAndBudget(@Param("minBedrooms") Integer minBedrooms,
                                                @Param("minBathrooms") Integer minBathrooms,
                                                @Param("minFloorArea") Double minFloorArea,
                                                @Param("maxBudget") Double maxBudget);
    
    @Query("SELECT COUNT(p) FROM PropertyJpaEntity p WHERE p.status = :status")
    long countByStatus(@Param("status") PropertyJpaEntity.PropertyStatusEnum status);
    
    @Query(value = """
        SELECT AVG(p.rent_price) FROM PropertyJpaEntity p 
        WHERE p.latitude IS NOT NULL 
        AND p.longitude IS NOT NULL
        AND (6371 * acos(cos(radians(:latitude)) * cos(radians(p.latitude)) 
        * cos(radians(p.longitude) - radians(:longitude)) 
        + sin(radians(:latitude)) * sin(radians(p.latitude)))) < :radiusKm
        """, nativeQuery = true)
    Double calculateAverageRentInArea(@Param("latitude") Double latitude, 
                                     @Param("longitude") Double longitude, 
                                     @Param("radiusKm") Double radiusKm);
    
    boolean existsByLandlordIdAndStatus(Long landlordId, PropertyJpaEntity.PropertyStatusEnum status);
    
    @Query("""
        SELECT p FROM PropertyJpaEntity p 
        WHERE p.propertyType = :propertyType 
        AND p.bedrooms = :bedrooms 
        AND p.bathrooms = :bathrooms 
        AND ABS(p.rentPrice - :rentPrice) <= :priceThreshold
        AND p.id != :excludeId
        ORDER BY ABS(p.rentPrice - :rentPrice)
        """)
    List<PropertyJpaEntity> findSimilarProperties(@Param("propertyType") PropertyJpaEntity.PropertyTypeEnum propertyType,
                                                 @Param("bedrooms") Integer bedrooms,
                                                 @Param("bathrooms") Integer bathrooms,
                                                 @Param("rentPrice") Double rentPrice,
                                                 @Param("priceThreshold") Double priceThreshold,
                                                 @Param("excludeId") Long excludeId,
                                                 org.springframework.data.domain.Pageable pageable);
}