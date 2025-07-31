package com.hanihome.hanihome_au_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    private String code;
    private String message;
    private Map<String, String> fieldErrors;
    private String details;
    private LocalDateTime timestamp;
    private String path;
    private String method;
}