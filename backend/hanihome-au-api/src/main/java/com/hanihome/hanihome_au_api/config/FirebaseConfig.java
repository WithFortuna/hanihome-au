package com.hanihome.hanihome_au_api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.service-account-key-path:firebase-service-account.json}")
    private String firebaseConfigPath;

    @PostConstruct
    public void initializeFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                ClassPathResource resource = new ClassPathResource(firebaseConfigPath);
                
                if (!resource.exists()) {
                    log.warn("Firebase service account key not found at: {}. FCM functionality will be disabled.", firebaseConfigPath);
                    return;
                }

                InputStream serviceAccount = resource.getInputStream();
                
                GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
                
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase app initialized successfully");
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase: {}", e.getMessage(), e);
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging() {
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("Firebase app not initialized. Returning null FirebaseMessaging bean.");
            return null;
        }
        return FirebaseMessaging.getInstance();
    }
}