package com.example.demo.service;

import chessLibOptimized.Game;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class GameSessionsService {
    private final Map<UUID, Game> gameSessions;

    @Autowired
    public GameSessionsService(@Qualifier("gameSessions") Map<UUID, Game> gameSessions) {
        this.gameSessions = gameSessions;
    }

    public Game get(UUID id) {
        return gameSessions.get(id);
    }

    public void save(Game gameSession) {
        if (!gameSessions.containsKey(gameSession.getUuid())) {
            gameSessions.put(gameSession.getUuid(), gameSession);
        }
    }
}
