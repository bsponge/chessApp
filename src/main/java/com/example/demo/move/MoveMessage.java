package com.example.demo.move;

import com.google.gson.JsonSerializer;
import com.myProject.Move;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class MoveMessage {
    private UUID gameUuid;
    private Move move;

    public MoveMessage(UUID gameUuid, Move move) {
        this.gameUuid = gameUuid;
        this.move = move;
    }
}
