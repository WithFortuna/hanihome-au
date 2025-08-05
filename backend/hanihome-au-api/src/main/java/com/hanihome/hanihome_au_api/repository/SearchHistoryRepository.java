package com.hanihome.hanihome_au_api.repository;

import com.hanihome.hanihome_au_api.domain.entity.SearchHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    /**
     * Find search history by user ID ordered by last used date
     */
    Page<SearchHistory> findByUserIdOrderByLastUsedAtDesc(Long userId, Pageable pageable);

    /**
     * Find saved searches by user ID
     */
    Page<SearchHistory> findByUserIdAndIsSavedTrueOrderByLastUsedAtDesc(Long userId, Pageable pageable);

    /**
     * Find recent search history by user ID (non-saved)
     */
    Page<SearchHistory> findByUserIdAndIsSavedFalseOrderByLastUsedAtDesc(Long userId, Pageable pageable);

    /**
     * Find similar search by user ID and search criteria hash
     */
    @Query("SELECT sh FROM SearchHistory sh WHERE sh.userId = :userId " +
           "AND sh.keyword = :keyword " +
           "AND sh.city = :city " +
           "AND sh.minRentPrice = :minRentPrice " +
           "AND sh.maxRentPrice = :maxRentPrice " +
           "AND sh.isSaved = false " +
           "ORDER BY sh.lastUsedAt DESC")
    Optional<SearchHistory> findSimilarSearch(@Param("userId") Long userId,
                                            @Param("keyword") String keyword,
                                            @Param("city") String city,
                                            @Param("minRentPrice") java.math.BigDecimal minRentPrice,
                                            @Param("maxRentPrice") java.math.BigDecimal maxRentPrice);

    /**
     * Find most frequent searches by user
     */
    @Query("SELECT sh FROM SearchHistory sh WHERE sh.userId = :userId " +
           "AND sh.isSaved = false " +
           "ORDER BY sh.searchCount DESC, sh.lastUsedAt DESC")
    List<SearchHistory> findMostFrequentSearches(@Param("userId") Long userId, Pageable pageable);

    /**
     * Count total searches by user
     */
    long countByUserId(Long userId);

    /**
     * Count saved searches by user
     */
    long countByUserIdAndIsSavedTrue(Long userId);

    /**
     * Delete old search history (privacy protection)
     */
    @Modifying
    @Query("DELETE FROM SearchHistory sh WHERE sh.userId = :userId " +
           "AND sh.isSaved = false " +
           "AND sh.createdAt < :cutoffDate")
    int deleteOldSearchHistory(@Param("userId") Long userId, @Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Delete all non-saved search history for user
     */
    @Modifying
    @Query("DELETE FROM SearchHistory sh WHERE sh.userId = :userId AND sh.isSaved = false")
    int deleteAllNonSavedSearchHistory(@Param("userId") Long userId);

    /**
     * Find saved search by user ID and name
     */
    Optional<SearchHistory> findByUserIdAndSearchNameAndIsSavedTrue(Long userId, String searchName);

    /**
     * Check if search name already exists for user
     */
    boolean existsByUserIdAndSearchNameAndIsSavedTrue(Long userId, String searchName);
}