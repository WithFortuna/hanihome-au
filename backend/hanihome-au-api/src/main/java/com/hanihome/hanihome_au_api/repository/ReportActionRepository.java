package com.hanihome.hanihome_au_api.repository;

import com.hanihome.hanihome_au_api.domain.entity.ReportAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportActionRepository extends JpaRepository<ReportAction, Long> {

    List<ReportAction> findByReportIdOrderByCreatedAtDesc(Long reportId);
    
    List<ReportAction> findByPerformedByOrderByCreatedAtDesc(Long adminId);

    @Query("SELECT ra FROM ReportAction ra WHERE ra.actionType = :actionType AND ra.createdAt >= :startDate ORDER BY ra.createdAt DESC")
    List<ReportAction> findByActionTypeAndCreatedAtAfter(@Param("actionType") ReportAction.ActionType actionType, 
                                                        @Param("startDate") LocalDateTime startDate);

    @Query("SELECT COUNT(ra) FROM ReportAction ra WHERE ra.performedBy = :adminId AND ra.createdAt >= :startDate")
    Long countActionsByAdminSince(@Param("adminId") Long adminId, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT ra.actionType, COUNT(ra) FROM ReportAction ra WHERE ra.createdAt >= :startDate GROUP BY ra.actionType")
    List<Object[]> getActionTypeStatistics(@Param("startDate") LocalDateTime startDate);
}