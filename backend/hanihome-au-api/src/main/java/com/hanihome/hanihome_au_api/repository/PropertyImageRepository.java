package com.hanihome.hanihome_au_api.repository;

import com.hanihome.hanihome_au_api.domain.entity.PropertyImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyImageRepository extends JpaRepository<PropertyImage, Long> {

    List<PropertyImage> findByPropertyId(Long propertyId);

    List<PropertyImage> findByPropertyIdOrderByImageOrder(Long propertyId);

    PropertyImage findByPropertyIdAndIsMainTrue(Long propertyId);

    PropertyImage findFirstByPropertyIdOrderByImageOrder(Long propertyId);

    List<PropertyImage> findByPropertyIdAndIdIn(Long propertyId, List<Long> imageIds);

    long countByPropertyId(Long propertyId);

    boolean existsByPropertyIdAndIsMainTrue(Long propertyId);

    void deleteByPropertyId(Long propertyId);

    @Query("SELECT MAX(pi.imageOrder) FROM PropertyImage pi WHERE pi.propertyId = :propertyId")
    Integer findMaxImageOrderByPropertyId(@Param("propertyId") Long propertyId);

    @Query("SELECT pi FROM PropertyImage pi WHERE pi.propertyId IN :propertyIds AND pi.isMain = true")
    List<PropertyImage> findMainImagesByPropertyIds(@Param("propertyIds") List<Long> propertyIds);
}