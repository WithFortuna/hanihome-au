package com.hanihome.hanihome_au_api.presentation.web.viewing;

import com.hanihome.hanihome_au_api.application.viewing.dto.CreateViewingCommand;
import com.hanihome.hanihome_au_api.application.viewing.dto.UpdateViewingCommand;
import com.hanihome.hanihome_au_api.application.viewing.dto.ViewingResponseDto;
import com.hanihome.hanihome_au_api.application.viewing.service.ViewingService;
import com.hanihome.hanihome_au_api.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/viewings")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Viewings", description = "Property viewing management APIs")
public class ViewingController {

    private final ViewingService viewingService;

    @PostMapping
    @Operation(summary = "Create viewing request", description = "Create a new property viewing request")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Viewing request created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "User not authenticated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Time slot conflict")
    })
    public ResponseEntity<ApiResponse<ViewingResponseDto>> createViewing(
            @Valid @RequestBody CreateViewingRequest request,
            Authentication authentication) {

        Long tenantUserId = extractUserIdFromAuthentication(authentication);

        CreateViewingCommand command = CreateViewingCommand.builder()
                .propertyId(request.getPropertyId())
                .tenantUserId(tenantUserId)
                .landlordUserId(request.getLandlordUserId())
                .agentUserId(request.getAgentUserId())
                .scheduledAt(request.getScheduledAt())
                .durationMinutes(request.getDurationMinutes())
                .tenantNotes(request.getTenantNotes())
                .contactPhone(request.getContactPhone())
                .contactEmail(request.getContactEmail())
                .build();

        ViewingResponseDto viewing = viewingService.createViewing(command);

        return ResponseEntity.status(201)
                .body(ApiResponse.success("Viewing request created successfully", viewing));
    }

    @GetMapping("/{viewingId}")
    @Operation(summary = "Get viewing details", description = "Retrieve viewing details by ID")
    public ResponseEntity<ApiResponse<ViewingResponseDto>> getViewing(@PathVariable Long viewingId) {

        ViewingResponseDto viewing = viewingService.getViewing(viewingId);

        return ResponseEntity.ok(ApiResponse.success("Viewing retrieved successfully", viewing));
    }

    @GetMapping("/my-viewings")
    @Operation(summary = "Get user's viewings", description = "Retrieve viewings for the authenticated user")
    public ResponseEntity<ApiResponse<Page<ViewingResponseDto>>> getMyViewings(
            @Parameter(description = "User role") @RequestParam(defaultValue = "tenant") String role,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {

        Long userId = extractUserIdFromAuthentication(authentication);
        Pageable pageable = PageRequest.of(page, size);

        Page<ViewingResponseDto> viewings;
        if ("landlord".equals(role)) {
            viewings = viewingService.getViewingsByLandlord(userId, pageable);
        } else {
            viewings = viewingService.getViewingsByTenant(userId, pageable);
        }

        return ResponseEntity.ok(ApiResponse.success("Viewings retrieved successfully", viewings));
    }

    @GetMapping("/property/{propertyId}")
    @Operation(summary = "Get property viewings", description = "Retrieve viewings for a specific property")
    public ResponseEntity<ApiResponse<Page<ViewingResponseDto>>> getPropertyViewings(
            @PathVariable Long propertyId,
            @Parameter(description = "Page number") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ViewingResponseDto> viewings = viewingService.getViewingsByProperty(propertyId, pageable);

        return ResponseEntity.ok(ApiResponse.success("Property viewings retrieved successfully", viewings));
    }

    @PutMapping("/{viewingId}/confirm")
    @Operation(summary = "Confirm viewing", description = "Confirm a viewing request")
    public ResponseEntity<ApiResponse<ViewingResponseDto>> confirmViewing(
            @PathVariable Long viewingId,
            Authentication authentication) {

        Long userId = extractUserIdFromAuthentication(authentication);
        ViewingResponseDto viewing = viewingService.confirmViewing(viewingId, userId);

        return ResponseEntity.ok(ApiResponse.success("Viewing confirmed successfully", viewing));
    }

    @PutMapping("/{viewingId}/cancel")
    @Operation(summary = "Cancel viewing", description = "Cancel a viewing request")
    public ResponseEntity<ApiResponse<ViewingResponseDto>> cancelViewing(
            @PathVariable Long viewingId,
            @RequestBody CancelViewingRequest request,
            Authentication authentication) {

        Long userId = extractUserIdFromAuthentication(authentication);
        ViewingResponseDto viewing = viewingService.cancelViewing(viewingId, userId, request.getReason());

        return ResponseEntity.ok(ApiResponse.success("Viewing cancelled successfully", viewing));
    }

    @PutMapping("/{viewingId}")
    @Operation(summary = "Update viewing", description = "Update viewing details")
    public ResponseEntity<ApiResponse<ViewingResponseDto>> updateViewing(
            @PathVariable Long viewingId,
            @Valid @RequestBody UpdateViewingRequest request,
            Authentication authentication) {

        Long userId = extractUserIdFromAuthentication(authentication);

        UpdateViewingCommand command = UpdateViewingCommand.builder()
                .viewingId(viewingId)
                .userId(userId)
                .scheduledAt(request.getScheduledAt())
                .durationMinutes(request.getDurationMinutes())
                .tenantNotes(request.getTenantNotes())
                .landlordNotes(request.getLandlordNotes())
                .agentNotes(request.getAgentNotes())
                .contactPhone(request.getContactPhone())
                .contactEmail(request.getContactEmail())
                .build();

        ViewingResponseDto viewing = viewingService.updateViewing(command);

        return ResponseEntity.ok(ApiResponse.success("Viewing updated successfully", viewing));
    }

    @PutMapping("/{viewingId}/complete")
    @Operation(summary = "Complete viewing", description = "Mark viewing as completed")
    public ResponseEntity<ApiResponse<ViewingResponseDto>> completeViewing(
            @PathVariable Long viewingId,
            Authentication authentication) {

        Long userId = extractUserIdFromAuthentication(authentication);
        ViewingResponseDto viewing = viewingService.completeViewing(viewingId, userId);

        return ResponseEntity.ok(ApiResponse.success("Viewing completed successfully", viewing));
    }

    @PostMapping("/{viewingId}/feedback")
    @Operation(summary = "Add viewing feedback", description = "Add feedback and rating for a completed viewing")
    public ResponseEntity<ApiResponse<ViewingResponseDto>> addFeedback(
            @PathVariable Long viewingId,
            @Valid @RequestBody FeedbackRequest request,
            Authentication authentication) {

        Long tenantUserId = extractUserIdFromAuthentication(authentication);
        ViewingResponseDto viewing = viewingService.addFeedback(
                viewingId, tenantUserId, request.getRating(), request.getComment());

        return ResponseEntity.ok(ApiResponse.success("Feedback added successfully", viewing));
    }

    @GetMapping("/property/{propertyId}/available-slots")
    @Operation(summary = "Get available time slots", description = "Get available viewing time slots for a property")
    public ResponseEntity<ApiResponse<List<LocalDateTime>>> getAvailableTimeSlots(
            @PathVariable Long propertyId,
            @Parameter(description = "From date (ISO format)") @RequestParam LocalDateTime fromDate,
            @Parameter(description = "To date (ISO format)") @RequestParam LocalDateTime toDate) {

        List<LocalDateTime> availableSlots = viewingService.getAvailableTimeSlots(propertyId, fromDate, toDate);

        return ResponseEntity.ok(ApiResponse.success("Available time slots retrieved successfully", availableSlots));
    }

    private Long extractUserIdFromAuthentication(Authentication authentication) {
        // Simplified - in real implementation, extract from JWT or security context
        return 1L;
    }

    // Request DTOs
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CreateViewingRequest {
        @jakarta.validation.constraints.NotNull(message = "Property ID is required")
        private Long propertyId;
        
        @jakarta.validation.constraints.NotNull(message = "Landlord ID is required")
        private Long landlordUserId;
        
        private Long agentUserId;
        
        @jakarta.validation.constraints.NotNull(message = "Scheduled time is required")
        private LocalDateTime scheduledAt;
        
        private Integer durationMinutes = 60;
        
        @jakarta.validation.constraints.Size(max = 1000, message = "Notes cannot exceed 1000 characters")
        private String tenantNotes;
        
        @jakarta.validation.constraints.Size(max = 20, message = "Phone number cannot exceed 20 characters")
        private String contactPhone;
        
        @jakarta.validation.constraints.Email(message = "Invalid email format")
        @jakarta.validation.constraints.Size(max = 255, message = "Email cannot exceed 255 characters")
        private String contactEmail;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UpdateViewingRequest {
        private LocalDateTime scheduledAt;
        private Integer durationMinutes;
        
        @jakarta.validation.constraints.Size(max = 1000, message = "Notes cannot exceed 1000 characters")
        private String tenantNotes;
        
        @jakarta.validation.constraints.Size(max = 1000, message = "Notes cannot exceed 1000 characters")
        private String landlordNotes;
        
        @jakarta.validation.constraints.Size(max = 1000, message = "Notes cannot exceed 1000 characters")
        private String agentNotes;
        
        @jakarta.validation.constraints.Size(max = 20, message = "Phone number cannot exceed 20 characters")
        private String contactPhone;
        
        @jakarta.validation.constraints.Email(message = "Invalid email format")
        @jakarta.validation.constraints.Size(max = 255, message = "Email cannot exceed 255 characters")
        private String contactEmail;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CancelViewingRequest {
        @jakarta.validation.constraints.Size(max = 500, message = "Cancellation reason cannot exceed 500 characters")
        private String reason;
    }

    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class FeedbackRequest {
        @jakarta.validation.constraints.NotNull(message = "Rating is required")
        @jakarta.validation.constraints.Min(value = 1, message = "Rating must be at least 1")
        @jakarta.validation.constraints.Max(value = 5, message = "Rating must be at most 5")
        private Integer rating;
        
        @jakarta.validation.constraints.Size(max = 1000, message = "Comment cannot exceed 1000 characters")
        private String comment;
    }
}