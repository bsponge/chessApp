package com.example.demo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.Hashtable;
import java.util.UUID;
import java.util.Vector;

@Configuration
public class PieceConfiguration {
    @Bean
    public Long lon() {
        return -1L;
    }

    @Bean
    public Vector<Piece> pieces() {
        Vector<Piece> list = new Vector<>();
        return list;
    }

    @Bean
    @Scope("prototype")
    public UUID uuid() {
        return UUID.randomUUID();
    }

    @Bean
    public Hashtable<Long, GameSession> gameSessions() {
        return new Hashtable<>();
    }
}
