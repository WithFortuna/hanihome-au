package com.hanihome.hanihome_au_api.service;

import com.hanihome.hanihome_au_api.domain.entity.Property;
import com.hanihome.hanihome_au_api.domain.entity.PropertyStatusHistory;
import com.hanihome.hanihome_au_api.domain.enums.PropertyStatus;
import com.hanihome.hanihome_au_api.exception.PropertyException;
import com.hanihome.hanihome_au_api.repository.PropertyRepository;
import com.hanihome.hanihome_au_api.repository.PropertyStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PropertyStatusService {

    private final PropertyRepository propertyRepository;
    private final PropertyStatusHistoryRepository statusHistoryRepository;

    private static final Map<PropertyStatus, Set<PropertyStatus>> ALLOWED_TRANSITIONS = Map.of(
        PropertyStatus.PENDING_APPROVAL, Set.of(PropertyStatus.ACTIVE, PropertyStatus.REJECTED),
        PropertyStatus.ACTIVE, Set.of(PropertyStatus.INACTIVE, PropertyStatus.COMPLETED, PropertyStatus.SUSPENDED),
        PropertyStatus.INACTIVE, Set.of(PropertyStatus.ACTIVE, PropertyStatus.SUSPENDED),
        PropertyStatus.REJECTED, Set.of(PropertyStatus.PENDING_APPROVAL),
        PropertyStatus.SUSPENDED, Set.of(PropertyStatus.ACTIVE, PropertyStatus.INACTIVE),
        PropertyStatus.COMPLETED, Set.of() // Final state, no transitions allowed
    );

    public Property changePropertyStatus(Long propertyId, PropertyStatus newStatus, Long changedBy, String reason, String notes) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyException.PropertyNotFoundException(propertyId));

        PropertyStatus currentStatus = property.getStatus();
        
        // Validate status transition
        validateStatusTransition(currentStatus, newStatus);
        
        // Apply status change using the property's methods
        applyStatusChange(property, newStatus, changedBy);
        
        // Save property first
        Property savedProperty = propertyRepository.save(property);
        
        // Record status history
        recordStatusChange(propertyId, currentStatus, newStatus, changedBy, reason, notes);
        
        log.info("Changed property {} status from {} to {} by user {}", 
                propertyId, currentStatus, newStatus, changedBy);
        
        return savedProperty;
    }

    public Property activateProperty(Long propertyId, Long activatedBy) {
        return changePropertyStatus(propertyId, PropertyStatus.ACTIVE, activatedBy, 
                "Property activated", null);
    }

    public Property deactivateProperty(Long propertyId, Long deactivatedBy, String reason) {
        return changePropertyStatus(propertyId, PropertyStatus.INACTIVE, deactivatedBy, 
                reason != null ? reason : "Property deactivated", null);
    }

    public Property completeProperty(Long propertyId, Long completedBy, String notes) {
        return changePropertyStatus(propertyId, PropertyStatus.COMPLETED, completedBy, 
                "Property transaction completed", notes);
    }

    public Property suspendProperty(Long propertyId, Long suspendedBy, String reason) {
        return changePropertyStatus(propertyId, PropertyStatus.SUSPENDED, suspendedBy, 
                reason != null ? reason : "Property suspended", null);
    }

    public Property approveProperty(Long propertyId, Long approvedBy, String notes) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyException.PropertyNotFoundException(propertyId));

        if (!property.isPendingApproval()) {
            throw new PropertyException.PropertyStatusException(
                    "Property must be in PENDING_APPROVAL status to be approved. Current status: " + property.getStatus());
        }

        property.approve(approvedBy);
        Property savedProperty = propertyRepository.save(property);
        
        recordStatusChange(propertyId, PropertyStatus.PENDING_APPROVAL, PropertyStatus.ACTIVE, 
                approvedBy, "Property approved", notes);
        
        log.info("Approved property {} by user {}", propertyId, approvedBy);
        return savedProperty;
    }

    public Property rejectProperty(Long propertyId, Long rejectedBy, String reason) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new PropertyException.PropertyNotFoundException(propertyId));

        if (!property.isPendingApproval()) {
            throw new PropertyException.PropertyStatusException(
                    "Property must be in PENDING_APPROVAL status to be rejected. Current status: " + property.getStatus());
        }

        property.reject();
        if (reason != null) {
            property.setAdminNotes(reason);
        }
        Property savedProperty = propertyRepository.save(property);
        
        recordStatusChange(propertyId, PropertyStatus.PENDING_APPROVAL, PropertyStatus.REJECTED, 
                rejectedBy, reason != null ? reason : "Property rejected", null);
        
        log.info("Rejected property {} by user {} for reason: {}", propertyId, rejectedBy, reason);
        return savedProperty;
    }

    @Transactional(readOnly = true)
    public List<PropertyStatusHistory> getPropertyStatusHistory(Long propertyId) {
        return statusHistoryRepository.findByPropertyIdOrderByCreatedDateDesc(propertyId);
    }

    @Transactional(readOnly = true)
    public Page<PropertyStatusHistory> getPropertyStatusHistory(Long propertyId, Pageable pageable) {
        return statusHistoryRepository.findByPropertyIdOrderByCreatedDateDesc(propertyId, pageable);
    }

    @Transactional(readOnly = true)
    public PropertyStatus getLastKnownStatus(Long propertyId) {
        return statusHistoryRepository.findTopByPropertyIdOrderByCreatedDateDesc(propertyId)
                .map(PropertyStatusHistory::getNewStatus)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getStatusChangeStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        return statusHistoryRepository.getStatusChangeStatistics(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<PropertyStatusHistory> getRecentStatusChanges(PropertyStatus status, LocalDateTime since) {
        return statusHistoryRepository.findRecentStatusChanges(status, since);
    }

    @Transactional(readOnly = true)
    public boolean canTransitionTo(PropertyStatus currentStatus, PropertyStatus newStatus) {
        if (currentStatus == newStatus) {
            return false; // No need to transition to same status
        }
        
        Set<PropertyStatus> allowedTransitions = ALLOWED_TRANSITIONS.get(currentStatus);
        return allowedTransitions != null && allowedTransitions.contains(newStatus);
    }

    @Transactional(readOnly = true)
    public Set<PropertyStatus> getAllowedTransitions(PropertyStatus currentStatus) {
        return ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());
    }

    public void bulkStatusChange(List<Long> propertyIds, PropertyStatus newStatus, Long changedBy, String reason) {
        List<Property> properties = propertyRepository.findAllById(propertyIds);
        
        if (properties.size() != propertyIds.size()) {
            throw new PropertyException.PropertyNotFoundException(-1L); // Some properties not found
        }

        for (Property property : properties) {
            PropertyStatus currentStatus = property.getStatus();
            
            if (canTransitionTo(currentStatus, newStatus)) {
                applyStatusChange(property, newStatus, changedBy);
                recordStatusChange(property.getId(), currentStatus, newStatus, changedBy, reason, null);
            } else {
                log.warn("Cannot transition property {} from {} to {}", 
                        property.getId(), currentStatus, newStatus);
            }
        }
        
        propertyRepository.saveAll(properties);
        log.info("Bulk status change completed for {} properties to status {} by user {}", 
                properties.size(), newStatus, changedBy);
    }

    public void cleanupStatusHistory(Long propertyId) {
        statusHistoryRepository.deleteByPropertyId(propertyId);
        log.info("Cleaned up status history for property: {}", propertyId);
    }

    private void validateStatusTransition(PropertyStatus currentStatus, PropertyStatus newStatus) {
        if (!canTransitionTo(currentStatus, newStatus)) {
            throw new PropertyException.PropertyStatusException(
                    String.format("Invalid status transition from %s to %s", currentStatus, newStatus));
        }
    }

    private void applyStatusChange(Property property, PropertyStatus newStatus, Long changedBy) {
        switch (newStatus) {
            case ACTIVE -> property.activate();
            case INACTIVE -> property.deactivate();
            case COMPLETED -> property.complete();
            case SUSPENDED -> property.suspend();
            case REJECTED -> property.reject();
            case PENDING_APPROVAL -> property.setStatus(PropertyStatus.PENDING_APPROVAL);
            default -> throw new PropertyException.PropertyStatusException("Unknown status: " + newStatus);
        }
    }

    private void recordStatusChange(Long propertyId, PropertyStatus previousStatus, PropertyStatus newStatus, 
                                  Long changedBy, String reason, String notes) {
        PropertyStatusHistory history = PropertyStatusHistory.builder()
                .propertyId(propertyId)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .changedBy(changedBy)
                .reason(reason)
                .notes(notes)
                .build();
                
        statusHistoryRepository.save(history);
    }
}