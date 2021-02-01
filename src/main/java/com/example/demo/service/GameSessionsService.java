package com.example.demo.service;

import chessLib.GameSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class GameSessionsService {
    private final Map<UUID, GameSession> gameSessions;

    @Autowired
    public GameSessionsService(@Qualifier("gameSessions") Map<UUID, GameSession> gameSessions) {
        this.gameSessions = gameSessions;
    }

    public GameSession get(UUID id) {
        return gameSessions.get(id);
    }

    public void save(GameSession gameSession) {
        if (!gameSessions.containsKey(gameSession.getId())) {
            gameSessions.put(gameSession.getId(), gameSession);
        }
    }
}
