package com.example.demo.moveMessage;

import chessLib.Color;
import chessLib.GameSession;
import chessLib.Move;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class MoveMessage {
    private final int msgType = 1;
    private UUID gameUuid;
    private Move move;
    private boolean isCheckOnWhite;
    private boolean isCheckOnBlack;
    private boolean isMateOnWhite;
    private boolean isMateOnBlack;

    public MoveMessage(UUID gameUuid, Move move) {
        this.gameUuid = gameUuid;
        this.move = move;
    }

    public MoveMessage(String gameUuid, Move move) {
        this.gameUuid = UUID.fromString(gameUuid);
        this.move = move;
    }

    public void setChecksAndMates(GameSession gameSession) {
        this.isMateOnWhite = gameSession.isMate(Color.WHITE);
        this.isMateOnBlack = gameSession.isMate(Color.BLACK);
        this.isCheckOnWhite = gameSession.isCheck(Color.WHITE);
        this.isCheckOnBlack = gameSession.isCheck(Color.BLACK);
    }
}
