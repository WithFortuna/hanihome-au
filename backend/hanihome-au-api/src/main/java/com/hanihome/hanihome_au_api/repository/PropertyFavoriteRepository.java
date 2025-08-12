package com.hanihome.hanihome_au_api.repository;

import com.hanihome.hanihome_au_api.domain.entity.PropertyFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyFavoriteRepository extends JpaRepository<PropertyFavorite, Long> {

    Optional<PropertyFavorite> findByUserIdAndPropertyId(Long userId, Long propertyId);

    boolean existsByUserIdAndPropertyId(Long userId, Long propertyId);

    Page<PropertyFavorite> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<PropertyFavorite> findByUserIdAndCategory(Long userId, String category);

    @Query("SELECT pf FROM PropertyFavorite pf WHERE pf.userId = :userId AND pf.category = :category ORDER BY pf.createdAt DESC")
    Page<PropertyFavorite> findByUserIdAndCategoryOrderByCreatedAtDesc(@Param("userId") Long userId, 
                                                                       @Param("category") String category, 
                                                                       Pageable pageable);

    @Query("SELECT DISTINCT pf.category FROM PropertyFavorite pf WHERE pf.userId = :userId AND pf.category IS NOT NULL")
    List<String> findDistinctCategoriesByUserId(@Param("userId") Long userId);

    long countByUserId(Long userId);

    @Query("SELECT COUNT(pf) FROM PropertyFavorite pf WHERE pf.userId = :userId AND pf.category = :category")
    long countByUserIdAndCategory(@Param("userId") Long userId, @Param("category") String category);

    List<PropertyFavorite> findByUserIdAndNotificationEnabledTrue(Long userId);

    void deleteByUserIdAndPropertyId(Long userId, Long propertyId);

    @Query("SELECT pf.propertyId FROM PropertyFavorite pf WHERE pf.userId = :userId")
    List<Long> findPropertyIdsByUserId(@Param("userId") Long userId);
}