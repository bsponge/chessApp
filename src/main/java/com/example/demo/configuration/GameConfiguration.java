package com.example.demo.configuration;

import chessLib.GameSession;
import chessLib.Player;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Hashtable;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedTransferQueue;

@Configuration
public class GameConfiguration {
    @Bean("gameQueue")
    public Queue<GameSession> gameQueue() {
        return new LinkedTransferQueue<>();
    }

    @Bean("gameSessions")
    public Map<UUID, GameSession> gameSessions() {
        return new ConcurrentHashMap<>();
    }

    @Bean("players")
    public Map<UUID, Player> players() {
        return new ConcurrentHashMap<>();
    }
}
