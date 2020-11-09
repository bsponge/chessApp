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
        gameSession.loadGameFromFEN("rnb1kb1r/pp1p1ppp/4Qn2/2p2q1p/8/4PNP1/PPP1BPPP/RNB1K2R w KQkq - 0 1");
        //gameSession.printChessboard();
        System.out.println(gameSession.isCheck(Piece.Color.BLACK));
    }
}
