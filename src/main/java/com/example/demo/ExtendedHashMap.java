package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
@Component
public class ExtendedHashMap{
    private ConcurrentMap<UUID, GameSession> map;
    private UUID lastAdded;

    @Autowired
    public ExtendedHashMap(ConcurrentHashMap<UUID, GameSession> map) {
        log.info("EXTENDEDHASHMAP CREATED");
        this.map = map;
    }

    public synchronized GameSession put(UUID uuid, GameSession gameSession) {
        map.put(uuid, gameSession);
        lastAdded = uuid;
        return gameSession;
    }

    public synchronized GameSession get(UUID uuid) {
        return map.get(uuid);
    }

    public boolean containsKey(UUID uuid) {
        return map.containsKey(uuid);
    }

    public ConcurrentMap<UUID, GameSession> getMap() {
        return this.map;
    }

    public synchronized GameSession getGame() {
        if (map.isEmpty()) {
            log.info("CREATING NEW GAMESESSION, CUZ MAP IS EMPTY");
            GameSession gameSession = new GameSession();
            map.put(gameSession.getSessionId(), gameSession);
            lastAdded = gameSession.getSessionId();
        } else if (map.get(lastAdded).getPlayerWhite() != null && map.get(lastAdded).getPlayerBlack() != null) {
            log.info("CREATING NEW GAMESESSION, CUZ NO FREE SPACE");
            GameSession gameSession = new GameSession();
            map.put(gameSession.getSessionId(), gameSession);
            lastAdded = gameSession.getSessionId();
        }
        log.info("RETURNING LAST GAME SESSION");
        return map.get(lastAdded);
    }
}
