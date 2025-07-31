package com.hanihome.hanihome_au_api.repository;

import com.hanihome.hanihome_au_api.domain.entity.UserPreferredRegion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPreferredRegionRepository extends JpaRepository<UserPreferredRegion, Long> {

    /**
     * Find all preferred regions for a user
     */
    @Query("SELECT upr FROM UserPreferredRegion upr WHERE upr.user.id = :userId ORDER BY upr.priority ASC")
    List<UserPreferredRegion> findByUserIdOrderByPriority(@Param("userId") Long userId);

    /**
     * Find active preferred regions for a user
     */
    @Query("SELECT upr FROM UserPreferredRegion upr WHERE upr.user.id = :userId AND upr.isActive = true ORDER BY upr.priority ASC")
    List<UserPreferredRegion> findActiveByUserIdOrderByPriority(@Param("userId") Long userId);

    /**
     * Find preferred region by user ID and region name
     */
    @Query("SELECT upr FROM UserPreferredRegion upr WHERE upr.user.id = :userId AND upr.regionName = :regionName")
    Optional<UserPreferredRegion> findByUserIdAndRegionName(@Param("userId") Long userId, @Param("regionName") String regionName);

    /**
     * Find regions by state
     */
    @Query("SELECT upr FROM UserPreferredRegion upr WHERE upr.user.id = :userId AND upr.state = :state AND upr.isActive = true")
    List<UserPreferredRegion> findByUserIdAndState(@Param("userId") Long userId, @Param("state") String state);

    /**
     * Find regions within distance from coordinates
     */
    @Query(value = """
        SELECT * FROM user_preferred_regions upr 
        WHERE upr.user_id = :userId 
        AND upr.is_active = true 
        AND upr.latitude IS NOT NULL 
        AND upr.longitude IS NOT NULL
        AND ST_DWithin(
            ST_MakePoint(upr.longitude, upr.latitude)::geography,
            ST_MakePoint(:longitude, :latitude)::geography,
            :distanceMeters
        )
        ORDER BY ST_Distance(
            ST_MakePoint(upr.longitude, upr.latitude)::geography,
            ST_MakePoint(:longitude, :latitude)::geography
        )
        """, nativeQuery = true)
    List<UserPreferredRegion> findRegionsWithinDistance(
            @Param("userId") Long userId,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("distanceMeters") Double distanceMeters
    );

    /**
     * Count active regions for a user
     */
    @Query("SELECT COUNT(upr) FROM UserPreferredRegion upr WHERE upr.user.id = :userId AND upr.isActive = true")
    Long countActiveRegionsByUserId(@Param("userId") Long userId);

    /**
     * Delete all regions for a user
     */
    void deleteByUserId(Long userId);

    /**
     * Find highest priority number for a user (for adding new regions)
     */
    @Query("SELECT COALESCE(MAX(upr.priority), 0) FROM UserPreferredRegion upr WHERE upr.user.id = :userId")
    Integer findMaxPriorityByUserId(@Param("userId") Long userId);

    /**
     * Check if region exists for user
     */
    boolean existsByUserIdAndRegionName(Long userId, String regionName);
}