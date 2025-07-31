package com.hanihome.hanihome_au_api.repository;

import com.hanihome.hanihome_au_api.domain.entity.PropertyStatusHistory;
import com.hanihome.hanihome_au_api.domain.enums.PropertyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PropertyStatusHistoryRepository extends JpaRepository<PropertyStatusHistory, Long> {

    List<PropertyStatusHistory> findByPropertyIdOrderByCreatedDateDesc(Long propertyId);

    Page<PropertyStatusHistory> findByPropertyIdOrderByCreatedDateDesc(Long propertyId, Pageable pageable);

    Optional<PropertyStatusHistory> findTopByPropertyIdOrderByCreatedDateDesc(Long propertyId);

    List<PropertyStatusHistory> findByPropertyIdAndNewStatus(Long propertyId, PropertyStatus newStatus);

    List<PropertyStatusHistory> findByChangedBy(Long changedBy);

    Page<PropertyStatusHistory> findByChangedBy(Long changedBy, Pageable pageable);

    @Query("SELECT psh FROM PropertyStatusHistory psh WHERE psh.propertyId IN :propertyIds ORDER BY psh.createdDate DESC")
    List<PropertyStatusHistory> findByPropertyIdsOrderByCreatedDateDesc(@Param("propertyIds") List<Long> propertyIds);

    @Query("SELECT psh FROM PropertyStatusHistory psh WHERE psh.createdDate BETWEEN :startDate AND :endDate ORDER BY psh.createdDate DESC")
    List<PropertyStatusHistory> findByCreatedDateBetweenOrderByCreatedDateDesc(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT psh.newStatus, COUNT(psh) FROM PropertyStatusHistory psh " +
           "WHERE psh.createdDate BETWEEN :startDate AND :endDate " +
           "GROUP BY psh.newStatus")
    List<Object[]> getStatusChangeStatistics(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT COUNT(psh) FROM PropertyStatusHistory psh " +
           "WHERE psh.propertyId = :propertyId AND psh.newStatus = :status")
    long countStatusChanges(@Param("propertyId") Long propertyId, @Param("status") PropertyStatus status);

    @Query("SELECT psh FROM PropertyStatusHistory psh " +
           "WHERE psh.newStatus = :status AND psh.createdDate >= :since " +
           "ORDER BY psh.createdDate DESC")
    List<PropertyStatusHistory> findRecentStatusChanges(
        @Param("status") PropertyStatus status, 
        @Param("since") LocalDateTime since
    );

    void deleteByPropertyId(Long propertyId);
}