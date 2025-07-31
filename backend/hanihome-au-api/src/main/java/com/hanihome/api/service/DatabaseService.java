package com.hanihome.api.service;

import com.hanihome.api.entity.DatabaseInfo;
import com.hanihome.api.repository.DatabaseInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseService {

    private final DatabaseInfoRepository databaseInfoRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> getDatabaseStatus() {
        try {
            String postgresVersion = databaseInfoRepository.getPostgreSQLVersion();
            String currentDatabase = databaseInfoRepository.getCurrentDatabase();
            
            DatabaseInfo dbInfo = databaseInfoRepository.findByName("hanihome_au")
                .orElse(null);

            log.info("Database connection successful. PostgreSQL version: {}, Database: {}", 
                    postgresVersion, currentDatabase);

            return Map.of(
                "status", "connected",
                "postgresVersion", postgresVersion,
                "currentDatabase", currentDatabase,
                "initializationInfo", dbInfo != null ? dbInfo : "Not initialized",
                "connectionTime", LocalDateTime.now()
            );
        } catch (Exception e) {
            log.error("Database connection failed", e);
            return Map.of(
                "status", "failed",
                "error", e.getMessage(),
                "connectionTime", LocalDateTime.now()
            );
        }
    }

    @Transactional
    public DatabaseInfo initializeDatabase() {
        DatabaseInfo dbInfo = databaseInfoRepository.findByName("hanihome_au")
            .orElse(DatabaseInfo.builder()
                .name("hanihome_au")
                .version("1.0.0")
                .initializedAt(LocalDateTime.now())
                .build());

        return databaseInfoRepository.save(dbInfo);
    }
}