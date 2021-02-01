package com.example.demo.controllers;

import chessLib.Color;
import chessLib.GameSession;
import chessLib.Move;
import chessLib.Player;
import com.example.demo.moveMessage.MoveMessage;
import com.example.demo.serializers.MoveMessageSerializer;
import com.example.demo.service.GamesHistoryService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@CrossOrigin
public class WebSocketController {
    private final GamesHistoryService gamesHistoryService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final Map<UUID, GameSession> gameSessions;
    private final Gson gsonMoveMessageSerializer;

    @Autowired
    public WebSocketController(GamesHistoryService gamesHistoryService,
                               SimpMessagingTemplate simpMessagingTemplate,
                               @Qualifier("gameSessions") Map<UUID, GameSession> gameSessions,
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
            if (moveMessage.isUndo()) {                         // undo move message
                Optional.of(moveMessage.getGameUuid())
                        .map(gameSessions::get)
                        .ifPresent(gameSession -> {
                            if (gameSession.getMovesHistory().size() > 0) {
                                Move lastMove = gameSession.getLastMove();
                                lastMove = new Move(lastMove.getToX(),
                                        lastMove.getToY(),
                                        lastMove.getFromX(),
                                        lastMove.getFromY(),
                                        lastMove.getColor(),
                                        lastMove.getType(),
                                        lastMove.getEnemyColor(),
                                        lastMove.getEnemyType());
                                gameSession.undoLastMove();
                                MoveMessage mm = new MoveMessage(UUID.fromString(toGameSession), moveMessage.getPlayerUuid(), true, lastMove, null);
                                mm.setChecksAndMates(gameSession);
                                simpMessagingTemplate.convertAndSend("/topic/messages/" + toGameSession, mm);
                            }
                        });
            } else {                                            // move message
                Move mv = moveMessage.getMove();
                Optional.of(moveMessage.getGameUuid())
                        .map(gameSessions::get)
                        .ifPresent(gameSession -> {
                            boolean a = gameSession.getColorTurn() == Color.WHITE
                                    ? gameSession.getWhitePlayer().getId().equals(moveMessage.getPlayerUuid())
                                    : Optional.ofNullable(gameSession.getBlackPlayer())
                                    .map(Player::getId)
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
