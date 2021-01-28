package com.example.demo.gameHistory;

import chessLib.GameSession;
import chessLib.Move;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
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

    public GameHistory(GameSession gameSession) {
        this.gameId = gameSession.getId();
        this.movesHistory = gameSession.getMovesHistory();
        this.whitePlayerId = gameSession.getWhitePlayerId();
        this.blackPlayerId = gameSession.getWhitePlayerId();
    }

    public GameHistory() {

    }
}
