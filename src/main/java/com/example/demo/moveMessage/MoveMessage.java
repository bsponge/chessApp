package com.example.demo.moveMessage;

import chessLibOptimized.Color;
import chessLibOptimized.Game;
import chessLibOptimized.Move;
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
    private int undo;
    private boolean isCheckOnWhite;
    private boolean isCheckOnBlack;
    private boolean isMateOnWhite;
    private boolean isMateOnBlack;
    private boolean isCastle;
    private int promotionType;

    public MoveMessage(UUID gameUuid, UUID playerUuid, int undo, Move move, int promotionType) {
        this.gameUuid = gameUuid;
        this.playerUuid = playerUuid;
        this.undo = undo;
        this.move = move;
        this.promotionType = promotionType;
    }

    public MoveMessage(String gameUuid, String playerUuid, int undo, Move move) {
        this.gameUuid = UUID.fromString(gameUuid);
        this.playerUuid = UUID.fromString(playerUuid);
        this.undo = undo;
        this.move = move;
    }

    public void setChecksAndMates(Game gameSession) {
        this.isMateOnWhite = gameSession.isMate(Color.WHITE);
        this.isMateOnBlack = gameSession.isMate(Color.BLACK);
        this.isCheckOnBlack = isMateOnBlack || gameSession.isCheck(Color.BLACK);
        this.isCheckOnWhite = isMateOnWhite || gameSession.isCheck(Color.WHITE);
    }
}
