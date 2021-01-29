package com.example.demo.moveMessage;

import chessLib.Color;
import chessLib.GameSession;
import chessLib.Move;
import chessLib.Type;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class MoveMessage {
    public final static MoveMessage WRONG_MOVE_MESSAGE = new MoveMessage();

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
    private Type promotionType;

    public MoveMessage(UUID gameUuid, UUID playerUuid, boolean isUndo, Move move, Type promotionType) {
        this.gameUuid = gameUuid;
        this.playerUuid = playerUuid;
        this.isUndo = isUndo;
        this.move = move;
        this.promotionType = promotionType;
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
        this.isCheckOnBlack = isMateOnBlack || gameSession.isCheck(Color.BLACK);
        this.isCheckOnWhite = isMateOnWhite || gameSession.isCheck(Color.WHITE);
    }
}
