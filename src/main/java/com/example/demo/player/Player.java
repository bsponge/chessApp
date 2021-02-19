package com.example.demo.player;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
public class Player {
    private UUID uuid;
    private Set<UUID> gameUuids;

    public Player() {
        this.uuid = UUID.randomUUID();
        this.gameUuids = new HashSet<>();
    }

    public void addGameUuid(UUID uuid) {
        gameUuids.add(uuid);
    }
}
