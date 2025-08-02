package com.hanihome.hanihome_au_api.repository;

import com.hanihome.hanihome_au_api.domain.entity.Property;
import com.hanihome.hanihome_au_api.dto.request.PropertySearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PropertyRepositoryCustom {
    
    Page<Property> searchPropertiesWithCriteria(PropertySearchCriteria criteria, Pageable pageable);
    
    List<Property> findPropertiesNearby(Double latitude, Double longitude, Double radiusKm, int limit);
    
    List<Property> findSimilarProperties(Long propertyId, int limit);
    
    List<Object[]> getPropertyStatistics();
    
    List<Object[]> getPriceRangeStatistics();
    
    List<Property> findPropertiesWithExpiringSoonAvailability(int daysAhead);
    
    long countPropertiesWithCriteria(PropertySearchCriteria criteria);
    
    List<Property> findNearbyProperties(java.math.BigDecimal latitude, java.math.BigDecimal longitude, Double radiusKm, int limit);
}