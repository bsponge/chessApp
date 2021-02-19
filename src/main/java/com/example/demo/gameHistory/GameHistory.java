package com.example.demo.gameHistory;

import chessLibOptimized.Game;
import chessLibOptimized.Move;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.List;
import java.util.UUID;

@Entity
public class GameHistory {
    @Id
    private UUID gameId;
    @ElementCollection
    private List<Move> movesHistory;
    private UUID whitePlayerId;
    private UUID blackPlayerId;

    public GameHistory(Game gameSession) {
        this.gameId = gameSession.getUuid();
        this.movesHistory = gameSession.getMovesHistory();
        this.whitePlayerId = gameSession.getWhitePlayerUuid();
        this.blackPlayerId = gameSession.getWhitePlayerUuid();
    }

    public GameHistory() {

    }
}
