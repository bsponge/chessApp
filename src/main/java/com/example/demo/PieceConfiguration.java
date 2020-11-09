package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@Slf4j
public class PieceConfiguration {
    @Bean
    public ConcurrentHashMap<UUID, GameSession> map() {
        log.info("CREATING MAP");
        return new ConcurrentHashMap<>();
    }

    @Bean
    public NavigableMap<Long, GameSession> gameSessions() {
        return new TreeMap<>();
    }
}
