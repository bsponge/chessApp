package com.example.demo.service;

import chessLib.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
public class PlayersService {
    private final Map<UUID, Player> players;

    @Autowired
    public PlayersService(@Qualifier("players") Map<UUID, Player> players) {
        this.players = players;
    }

    public Player get(UUID id) {
        return players.get(id);
    }

    public void save(Player player) {
        if (!players.containsKey(player.getId())) {
            players.put(player.getId(), player);
        }
    }

    public boolean contains(Player player) {
        if (player != null) {
            return players.containsKey(player.getId());
        }
        return false;
    }

    public boolean containsPlayerWithId(UUID id) {
        if (id != null) {
            return players.containsKey(id);
        }
        return false;
    }
}
