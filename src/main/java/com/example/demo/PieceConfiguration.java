package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@Slf4j
public class PieceConfiguration {
    @Bean
    public Long lon() {
        return -1L;
    }

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
