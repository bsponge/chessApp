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
    private UUID playerUuid;
    private Move move;
    private boolean isUndo;
    private boolean isCheckOnWhite;
    private boolean isCheckOnBlack;
    private boolean isMateOnWhite;
    private boolean isMateOnBlack;
    private boolean isCastle;

    public MoveMessage(UUID gameUuid, UUID playerUuid, boolean isUndo, Move move) {
        this.gameUuid = gameUuid;
        this.playerUuid = playerUuid;
        this.isUndo = isUndo;
        this.move = move;
    }

    public MoveMessage(String gameUuid, String playerUuid, boolean isUndo, Move move) {
        this.gameUuid = UUID.fromString(gameUuid);
        this.playerUuid = UUID.fromString(playerUuid);
        this.isUndo = isUndo;
        this.move = move;
    }

    public void setChecksAndMates(GameSession gameSession) {
        this.isMateOnWhite = gameSession.isMate(Color.WHITE);
        this.isMateOnBlack = gameSession.isMate(Color.BLACK);
        this.isCheckOnBlack = isMateOnBlack ? true : gameSession.isCheck(Color.BLACK);
        this.isCheckOnWhite = isMateOnWhite ? true : gameSession.isCheck(Color.WHITE);
    }
}
