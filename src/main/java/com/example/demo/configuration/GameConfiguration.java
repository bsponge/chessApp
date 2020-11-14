package com.example.demo.configuration;

import com.myProject.GameSession;
import com.myProject.Player;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Hashtable;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedTransferQueue;

@Configuration
public class GameConfiguration {
    @Bean("gameQueue")
    public Queue<GameSession> gameQueue() {
        return new LinkedTransferQueue<>();
    }

    @Bean("gameSessions")
    public Hashtable<UUID, GameSession> gameSession() {
        return new Hashtable<>();
    }

    @Bean("players")
    public Hashtable<UUID, Player> players() {
        return new Hashtable<>();
    }
}
