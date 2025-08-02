package com.hanihome.hanihome_au_api.presentation.web.user;

import com.hanihome.hanihome_au_api.application.user.dto.CreateUserCommand;
import com.hanihome.hanihome_au_api.application.user.dto.UserResponseDto;
import com.hanihome.hanihome_au_api.application.user.usecase.CreateUserUseCase;
import com.hanihome.hanihome_au_api.presentation.dto.ApiResponse;
import com.hanihome.hanihome_au_api.presentation.dto.CreateUserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "User Management", description = "User management operations")
public class UserController {
    
    private final CreateUserUseCase createUserUseCase;

    public UserController(CreateUserUseCase createUserUseCase) {
        this.createUserUseCase = createUserUseCase;
    }

    @PostMapping
    @Operation(summary = "Create a new user", description = "Creates a new user in the system")
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            CreateUserCommand command = new CreateUserCommand(
                request.getEmail(),
                request.getName(),
                request.getPhoneNumber(),
                request.getRole()
            );

            UserResponseDto userResponse = createUserUseCase.execute(command);
            ApiResponse<UserResponseDto> response = ApiResponse.success("User created successfully", userResponse);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            ApiResponse<UserResponseDto> errorResponse = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            ApiResponse<UserResponseDto> errorResponse = ApiResponse.error("Internal server error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}