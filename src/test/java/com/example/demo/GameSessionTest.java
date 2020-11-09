package com.example.demo;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class GameSessionTest {
    @Test
    public void gameSessionTest() {
        GameSession gameSession = new GameSession();
        for (Piece[] arr : gameSession.getChessboard()) {
            for (Piece p : arr) {
                System.out.println(p);
            }
        }
    }

    @Test
    public void loadGameFromFEN() {
        GameSession gameSession = new GameSession();
        gameSession.loadGameFromFEN("rnbqkbnr/ppppp1pp/8/4Pp2/8/8/PPPP1PPP/RNBQKBNR w KQkq - 0 1");
        gameSession.printChessboard();
        gameSession.setLastMove(new Move(5, 6, 5, 4));
        gameSession.makeMove(new Move(4, 4, 5, 5));
        gameSession.printChessboard();
    }
}
