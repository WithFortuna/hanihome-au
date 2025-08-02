package com.hanihome.hanihome_au_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableScheduling
public class HanihomeAuApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(HanihomeAuApiApplication.class, args);
    }
}