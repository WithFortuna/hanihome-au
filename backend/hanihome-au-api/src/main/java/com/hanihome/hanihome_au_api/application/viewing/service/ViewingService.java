package com.hanihome.hanihome_au_api.application.viewing.service;

import com.hanihome.hanihome_au_api.application.viewing.dto.CreateViewingCommand;
import com.hanihome.hanihome_au_api.application.viewing.dto.UpdateViewingCommand;
import com.hanihome.hanihome_au_api.application.viewing.dto.ViewingResponseDto;
import com.hanihome.hanihome_au_api.application.notification.service.SSENotificationService;
import com.hanihome.hanihome_au_api.domain.entity.Viewing;
import com.hanihome.hanihome_au_api.domain.enums.ViewingStatus;
import com.hanihome.hanihome_au_api.repository.ViewingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ViewingService {

    private final ViewingRepository viewingRepository;
    private final ViewingConflictService conflictService;
    private final SSENotificationService notificationService;

    /**
     * Create a new viewing request
     */
    public ViewingResponseDto createViewing(CreateViewingCommand command) {
        log.info("Creating viewing for property: {} by tenant: {}", command.getPropertyId(), command.getTenantUserId());

        // Check for existing active viewing
        List<Viewing> existingViewings = viewingRepository.findActiveViewingsByTenantAndProperty(
                command.getPropertyId(), command.getTenantUserId());
        
        if (!existingViewings.isEmpty()) {
            throw new IllegalStateException("You already have an active viewing request for this property");
        }

        // Validate scheduled time is in the future
        if (command.getScheduledAt().isBefore(LocalDateTime.now().plusHours(1))) {
            throw new IllegalArgumentException("Viewing must be scheduled at least 1 hour in advance");
        }

        Viewing viewing = Viewing.builder()
                .propertyId(command.getPropertyId())
                .tenantUserId(command.getTenantUserId())
                .landlordUserId(command.getLandlordUserId())
                .agentUserId(command.getAgentUserId())
                .scheduledAt(command.getScheduledAt())
                .durationMinutes(command.getDurationMinutes() != null ? command.getDurationMinutes() : 60)
                .tenantNotes(command.getTenantNotes())
                .contactPhone(command.getContactPhone())
                .contactEmail(command.getContactEmail())
                .status(ViewingStatus.REQUESTED)
                .build();

        try {
            // Use conflict prevention service to create viewing safely
            viewing = conflictService.createViewingWithConflictPrevention(viewing);
            log.info("Created viewing: {}", viewing.getId());
            
            // Send notification to landlord about new viewing request
            sendViewingNotification(viewing, SSENotificationService.ViewingNotificationType.VIEWING_REQUESTED);
            
            return convertToDto(viewing);
            
        } catch (ViewingConflictService.ViewingConflictException e) {
            log.warn("Viewing creation failed due to conflict: {}", e.getMessage());
            throw new IllegalStateException("The requested time slot is no longer available");
        }
    }

    /**
     * Confirm a viewing request
     */
    public ViewingResponseDto confirmViewing(Long viewingId, Long confirmedByUserId) {
        log.info("Confirming viewing: {} by user: {}", viewingId, confirmedByUserId);

        Viewing viewing = viewingRepository.findById(viewingId)
                .orElseThrow(() -> new IllegalArgumentException("Viewing not found"));

        // Verify user has permission to confirm
        if (!viewing.getLandlordUserId().equals(confirmedByUserId) && 
            !viewing.getAgentUserId().equals(confirmedByUserId)) {
            throw new IllegalArgumentException("Only landlord or agent can confirm viewing");
        }

        viewing.confirm(confirmedByUserId);
        viewing = viewingRepository.save(viewing);

        // Send notification to tenant about confirmed viewing
        sendViewingNotification(viewing, SSENotificationService.ViewingNotificationType.VIEWING_CONFIRMED);

        return convertToDto(viewing);
    }

    /**
     * Cancel a viewing
     */
    public ViewingResponseDto cancelViewing(Long viewingId, Long cancelledByUserId, String reason) {
        log.info("Cancelling viewing: {} by user: {} for reason: {}", viewingId, cancelledByUserId, reason);

        Viewing viewing = viewingRepository.findById(viewingId)
                .orElseThrow(() -> new IllegalArgumentException("Viewing not found"));

        // Verify user has permission to cancel
        if (!viewing.getTenantUserId().equals(cancelledByUserId) && 
            !viewing.getLandlordUserId().equals(cancelledByUserId) && 
            !viewing.getAgentUserId().equals(cancelledByUserId)) {
            throw new IllegalArgumentException("Only involved parties can cancel viewing");
        }

        viewing.cancel(cancelledByUserId, reason);
        viewing = viewingRepository.save(viewing);

        // Send notification to all parties about cancelled viewing
        sendViewingNotification(viewing, SSENotificationService.ViewingNotificationType.VIEWING_CANCELLED);

        return convertToDto(viewing);
    }

    /**
     * Update viewing details
     */
    public ViewingResponseDto updateViewing(UpdateViewingCommand command) {
        log.info("Updating viewing: {}", command.getViewingId());

        Viewing viewing = viewingRepository.findById(command.getViewingId())
                .orElseThrow(() -> new IllegalArgumentException("Viewing not found"));

        // Verify user has permission to update
        if (!viewing.getTenantUserId().equals(command.getUserId()) && 
            !viewing.getLandlordUserId().equals(command.getUserId()) && 
            !viewing.getAgentUserId().equals(command.getUserId())) {
            throw new IllegalArgumentException("Only involved parties can update viewing");
        }

        // Handle rescheduling with conflict prevention
        if (command.getScheduledAt() != null && !command.getScheduledAt().equals(viewing.getScheduledAt())) {
            try {
                Integer newDuration = command.getDurationMinutes() != null ? 
                    command.getDurationMinutes() : viewing.getDurationMinutes();
                
                viewing = conflictService.updateViewingWithConflictPrevention(
                    viewing, command.getScheduledAt(), newDuration);
                    
            } catch (ViewingConflictService.ViewingConflictException e) {
                log.warn("Viewing update failed due to conflict: {}", e.getMessage());
                throw new IllegalStateException("The requested time slot is not available");
            }
        }

        // Update other fields
        if (command.getDurationMinutes() != null) {
            viewing.setDurationMinutes(command.getDurationMinutes());
        }
        if (command.getTenantNotes() != null) {
            viewing.setTenantNotes(command.getTenantNotes());
        }
        if (command.getLandlordNotes() != null) {
            viewing.setLandlordNotes(command.getLandlordNotes());
        }
        if (command.getAgentNotes() != null) {
            viewing.setAgentNotes(command.getAgentNotes());
        }
        if (command.getContactPhone() != null) {
            viewing.setContactPhone(command.getContactPhone());
        }
        if (command.getContactEmail() != null) {
            viewing.setContactEmail(command.getContactEmail());
        }

        viewing = viewingRepository.save(viewing);
        return convertToDto(viewing);
    }

    /**
     * Complete a viewing
     */
    public ViewingResponseDto completeViewing(Long viewingId, Long completedByUserId) {
        log.info("Completing viewing: {} by user: {}", viewingId, completedByUserId);

        Viewing viewing = viewingRepository.findById(viewingId)
                .orElseThrow(() -> new IllegalArgumentException("Viewing not found"));

        // Verify user has permission to complete
        if (!viewing.getLandlordUserId().equals(completedByUserId) && 
            !viewing.getAgentUserId().equals(completedByUserId)) {
            throw new IllegalArgumentException("Only landlord or agent can mark viewing as completed");
        }

        viewing.complete();
        viewing = viewingRepository.save(viewing);

        return convertToDto(viewing);
    }

    /**
     * Add feedback to a completed viewing
     */
    public ViewingResponseDto addFeedback(Long viewingId, Long tenantUserId, Integer rating, String comment) {
        log.info("Adding feedback to viewing: {} by tenant: {}", viewingId, tenantUserId);

        Viewing viewing = viewingRepository.findById(viewingId)
                .orElseThrow(() -> new IllegalArgumentException("Viewing not found"));

        if (!viewing.getTenantUserId().equals(tenantUserId)) {
            throw new IllegalArgumentException("Only the tenant can provide feedback");
        }

        viewing.addFeedback(rating, comment);
        viewing = viewingRepository.save(viewing);

        return convertToDto(viewing);
    }

    /**
     * Get viewings by tenant
     */
    @Transactional(readOnly = true)
    public Page<ViewingResponseDto> getViewingsByTenant(Long tenantUserId, Pageable pageable) {
        log.debug("Getting viewings for tenant: {}", tenantUserId);
        
        Page<Viewing> viewings = viewingRepository.findByTenantUserIdOrderByScheduledAtDesc(tenantUserId, pageable);
        return viewings.map(this::convertToDto);
    }

    /**
     * Get viewings by landlord
     */
    @Transactional(readOnly = true)
    public Page<ViewingResponseDto> getViewingsByLandlord(Long landlordUserId, Pageable pageable) {
        log.debug("Getting viewings for landlord: {}", landlordUserId);
        
        Page<Viewing> viewings = viewingRepository.findByLandlordUserIdOrderByScheduledAtDesc(landlordUserId, pageable);
        return viewings.map(this::convertToDto);
    }

    /**
     * Get viewings by property
     */
    @Transactional(readOnly = true)
    public Page<ViewingResponseDto> getViewingsByProperty(Long propertyId, Pageable pageable) {
        log.debug("Getting viewings for property: {}", propertyId);
        
        Page<Viewing> viewings = viewingRepository.findByPropertyIdOrderByScheduledAtDesc(propertyId, pageable);
        return viewings.map(this::convertToDto);
    }

    /**
     * Get viewing by ID
     */
    @Transactional(readOnly = true)
    public ViewingResponseDto getViewing(Long viewingId) {
        log.debug("Getting viewing: {}", viewingId);
        
        Viewing viewing = viewingRepository.findById(viewingId)
                .orElseThrow(() -> new IllegalArgumentException("Viewing not found"));
        
        return convertToDto(viewing);
    }

    /**
     * Get available time slots for a property
     */
    @Transactional(readOnly = true)
    public List<LocalDateTime> getAvailableTimeSlots(Long propertyId, LocalDateTime fromDate, LocalDateTime toDate) {
        log.debug("Getting available time slots for property: {} from {} to {}", propertyId, fromDate, toDate);
        
        List<Viewing> existingViewings = viewingRepository.findActiveViewingsInTimeRange(propertyId, fromDate, toDate);
        
        // This is a simplified implementation - in reality you'd generate time slots based on
        // business hours, landlord availability, etc. and then filter out conflicting times
        // For now, we'll just return the conflicting times so the frontend can avoid them
        List<LocalDateTime> bookedTimes = existingViewings.stream()
                .map(Viewing::getScheduledAt)
                .collect(Collectors.toList());
        
        // In a real implementation, you'd generate all possible time slots and filter out booked ones
        // This is a placeholder implementation
        return List.of(); // Return empty list for now
    }

    private ViewingResponseDto convertToDto(Viewing viewing) {
        return ViewingResponseDto.builder()
                .id(viewing.getId())
                .propertyId(viewing.getPropertyId())
                .tenantUserId(viewing.getTenantUserId())
                .landlordUserId(viewing.getLandlordUserId())
                .agentUserId(viewing.getAgentUserId())
                .scheduledAt(viewing.getScheduledAt())
                .durationMinutes(viewing.getDurationMinutes())
                .status(viewing.getStatus())
                .tenantNotes(viewing.getTenantNotes())
                .landlordNotes(viewing.getLandlordNotes())
                .agentNotes(viewing.getAgentNotes())
                .contactPhone(viewing.getContactPhone())
                .contactEmail(viewing.getContactEmail())
                .confirmedAt(viewing.getConfirmedAt())
                .cancelledAt(viewing.getCancelledAt())
                .cancellationReason(viewing.getCancellationReason())
                .cancelledByUserId(viewing.getCancelledByUserId())
                .completedAt(viewing.getCompletedAt())
                .feedbackRating(viewing.getFeedbackRating())
                .feedbackComment(viewing.getFeedbackComment())
                .rescheduledFromViewingId(viewing.getRescheduledFromViewingId())
                .createdAt(viewing.getCreatedAt())
                .updatedAt(viewing.getUpdatedAt())
                // Computed fields
                .scheduledEndTime(viewing.getScheduledEndTime())
                .canBeCancelled(viewing.canBeCancelled())
                .canBeRescheduled(viewing.canBeRescheduled())
                .isInPast(viewing.isInPast())
                .requiresFeedback(viewing.getStatus() == ViewingStatus.COMPLETED && viewing.getFeedbackRating() == null)
                .build();
    }
    
    /**
     * Send viewing notification to relevant parties
     */
    private void sendViewingNotification(Viewing viewing, SSENotificationService.ViewingNotificationType notificationType) {
        try {
            Map<String, Object> notificationData = Map.of(
                "viewingId", viewing.getId(),
                "propertyId", viewing.getPropertyId(),
                "propertyTitle", "Property #" + viewing.getPropertyId(), // TODO: Get actual property title
                "scheduledAt", viewing.getScheduledAt().toString(),
                "status", viewing.getStatus().name()
            );
            
            // Determine who should receive the notification based on the type
            switch (notificationType) {
                case VIEWING_REQUESTED:
                    // Notify landlord and agent (if present)
                    notificationService.sendViewingNotification(viewing.getLandlordUserId(), notificationType, notificationData);
                    if (viewing.getAgentUserId() != null) {
                        notificationService.sendViewingNotification(viewing.getAgentUserId(), notificationType, notificationData);
                    }
                    break;
                    
                case VIEWING_CONFIRMED:
                case VIEWING_REMINDER:
                    // Notify tenant
                    notificationService.sendViewingNotification(viewing.getTenantUserId(), notificationType, notificationData);
                    break;
                    
                case VIEWING_CANCELLED:
                case VIEWING_RESCHEDULED:
                    // Notify all parties
                    notificationService.sendViewingNotification(viewing.getTenantUserId(), notificationType, notificationData);
                    notificationService.sendViewingNotification(viewing.getLandlordUserId(), notificationType, notificationData);
                    if (viewing.getAgentUserId() != null) {
                        notificationService.sendViewingNotification(viewing.getAgentUserId(), notificationType, notificationData);
                    }
                    break;
                    
                case VIEWING_COMPLETED:
                    // Notify tenant for feedback request
                    notificationService.sendViewingNotification(viewing.getTenantUserId(), notificationType, notificationData);
                    break;
            }
            
        } catch (Exception e) {
            log.warn("Failed to send viewing notification: {}", e.getMessage());
            // Don't fail the operation if notification fails
        }
    }
}