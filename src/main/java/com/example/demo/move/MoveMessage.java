package com.example.demo.move;

import com.myProject.Move;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class MoveMessage {
    private final int msgType = 1;
    private UUID gameUuid;
    private Move move;

    public MoveMessage(UUID gameUuid, Move move) {
        this.gameUuid = gameUuid;
        this.move = move;
    }

    public MoveMessage(String gameUuid, Move move) {
        this.gameUuid = UUID.fromString(gameUuid);
        this.move = move;
    }
}
