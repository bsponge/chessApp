package com.example.demo.controllers;

import chessLibOptimized.Color;
import chessLibOptimized.Move;
import com.example.demo.moveMessage.MoveMessage;
import com.example.demo.service.GameSessionsService;
import com.example.demo.service.GamesHistoryService;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@CrossOrigin
public class WebSocketController {
    private final GamesHistoryService gamesHistoryService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final GameSessionsService gameSessions;
    private final Gson gsonMoveMessageSerializer;

    @Autowired
    public WebSocketController(GamesHistoryService gamesHistoryService,
                               SimpMessagingTemplate simpMessagingTemplate,
                               GameSessionsService gameSessions,
                               @Qualifier("gsonMoveMessageSerializer") Gson gsonMoveMessageSerializer) {
        this.gamesHistoryService = gamesHistoryService;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.gameSessions = gameSessions;
        this.gsonMoveMessageSerializer = gsonMoveMessageSerializer;
    }

    @MessageMapping("/chess/{toGameSession}")
    public void sendMoveMessage(@DestinationVariable String toGameSession, String move) {
        try {
            MoveMessage moveMessage = gsonMoveMessageSerializer.fromJson(move, MoveMessage.class);
            if (moveMessage.getUndo() != 0) {                         // undo move message
                Optional.of(moveMessage.getGameUuid())
                        .map(gameSessions::get)
                        .ifPresent(gameSession -> {
                            if (gameSession.getMovesHistory().size() > 0) {
                                Move lastMove = gameSession.getLastMove();
                                gameSession.undoLastMove();
                                Move m = new Move(
                                        lastMove.getFromX(),
                                        lastMove.getFromY(),
                                        lastMove.getToX(),
                                        lastMove.getToY(),
                                        gameSession.getChessboard()[lastMove.getFromX()][lastMove.getFromY()],
                                        gameSession.getChessboard()[lastMove.getToX()][lastMove.getToY()]);
                                MoveMessage mm = new MoveMessage(UUID.fromString(toGameSession), null, 1, m, 0);
                                mm.setChecksAndMates(gameSession);
                                simpMessagingTemplate.convertAndSend("/topic/messages/" + toGameSession, mm);
                            }
                        });
            } else {                                            // move message
                Move mv = moveMessage.getMove();
                Optional.of(moveMessage.getGameUuid())
                        .map(gameSessions::get)
                        .ifPresent(gameSession -> {
                            if (gameSession.getTurn() == Color.WHITE) {
                                if (!gameSession.getWhitePlayerUuid().equals(moveMessage.getPlayerUuid())) {
                                    return;
                                }
                            } else {
                                if (!gameSession.getBlackPlayerUuid().equals(moveMessage.getPlayerUuid())) {
                                    return;
                                }
                            }
                            boolean a = gameSession.getTurn() == Color.WHITE
                                    ? gameSession.getWhitePlayerUuid().equals(moveMessage.getPlayerUuid()) : Optional.ofNullable(gameSession.getBlackPlayerUuid())
                                    .map(player -> player.equals(moveMessage.getPlayerUuid()))
                                    .orElse(false);
                            boolean b = gameSession.move(mv.getFromX(), mv.getFromY(), mv.getToX(), mv.getToY(), moveMessage.getPromotionType());
                            if (a && b) {
                                gameSession.setMovesTime(System.currentTimeMillis());
                                if ((mv.getFromX() == 4 && mv.getToX() == 6) || (mv.getFromX() == 4 && mv.getToX() == 2)) {
                                    moveMessage.setCastle(true);
                                }
                                moveMessage.setChecksAndMates(gameSession);
                                if (moveMessage.isMateOnBlack() || moveMessage.isMateOnWhite()) {
                                    gamesHistoryService.saveGameHistory(gameSession);
                                }
                                log.info(moveMessage.toString());
                                moveMessage.setPlayerUuid(null);
                                simpMessagingTemplate.convertAndSend("/topic/messages/" + toGameSession, moveMessage);
                            }
                        });
            }

        } catch (JsonParseException e) {
            e.printStackTrace();
            log.info("Wrong move message!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
