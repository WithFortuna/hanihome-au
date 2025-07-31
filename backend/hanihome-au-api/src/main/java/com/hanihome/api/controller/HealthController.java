package com.hanihome.api.controller;

import com.hanihome.api.service.DatabaseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
public class HealthController {

    private final DatabaseService databaseService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> databaseStatus = databaseService.getDatabaseStatus();
        
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now(),
            "service", "HaniHome AU API",
            "version", "0.0.1-SNAPSHOT",
            "database", databaseStatus
        ));
    }

    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> databaseHealth() {
        return ResponseEntity.ok(databaseService.getDatabaseStatus());
    }
}