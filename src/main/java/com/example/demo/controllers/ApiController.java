package com.example.demo.controllers;

import chessLib.*;
import com.example.demo.moveMessage.MoveMessage;
import com.example.demo.serializers.MoveMessageSerializer;
import com.example.demo.serializers.PiecesSerializer;
import com.example.demo.service.GamesHistoryService;
import com.example.demo.sides.SidesMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/*
    TODO:
        - undoMove accept
        - game time
        - extract websocket endpoints to external class
        - extract gson serializers from methods
 */

/**
 * ApiController is rest controller for client's game moves communication with server
 * @author js
 */

@Slf4j
@RestController
@CrossOrigin
public class ApiController {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final Map<UUID, GameSession> gameSessions;
    private final Queue<GameSession> gameQueue;
    private final Map<UUID, Player> players;
    private final GamesHistoryService gamesHistoryService;

    @Autowired
    public ApiController(SimpMessagingTemplate simpMessagingTemplate,
                         GamesHistoryService gamesHistoryService,
                         @Qualifier("gameSessions") Map<UUID, GameSession> gameSessions,
                         @Qualifier("gameQueue") Queue<GameSession> gameQueue,
                         @Qualifier("players") Map<UUID, Player> players) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.gamesHistoryService = gamesHistoryService;
        this.gameSessions = gameSessions;
        this.gameQueue = gameQueue;
        this.players = players;
    }

    /**
     * Returns json String representing Piece[] of active GameSession
     * @param playerId
     * @return json String representing Piece[] of active GameSession
     */
    @GetMapping("/reload")
    @ResponseBody
    public String reload(@CookieValue(value = "playerId", defaultValue = "none") String playerId) {
        if (!playerId.equals("none")) {     // player has playerId attribute in cookie
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Piece[][].class, new PiecesSerializer())
                    .create();

            try {                           // if player has active game return json String representing array of pieces
                return Optional
                        .of(playerId)
                        .map(UUID::fromString)
                        .map(players::get)
                        .map(Player::getGameSessionId)
                        .map(gameSessions::get)
                        .map(gameSession -> Arrays.stream(gameSession.getChessboard())
                                .flatMap(Arrays::stream)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList()))
                        .map(gson::toJson)
                        .orElse("null");
            } catch (Exception e) {
                e.printStackTrace();
                log.info("Exception");
                return "null";
            }
        } else {
            log.info("bad cookie");
            return "null";
        }
    }

    @PostMapping("/findGame")
    public ResponseEntity<String> findGame(@CookieValue(value = "playerId", defaultValue = "none") String playerId) {
        if (!playerId.equals("none")) {
            try {
                return Optional.of(playerId)
                        .map(UUID::fromString)
                        .map(players::get)
                        .map(player -> {
                            if (gameQueue.isEmpty()) {                  // white player
                                GameSession gameSession = new GameSession(player);
                                gameSession.setWhitesTime(120_000L);    // 2 minutes
                                gameQueue.add(gameSession);
                                player.setColor(Color.WHITE);
                                player.setGameSessionId(gameSession.getId());
                                SidesMessage sidesMessage = new SidesMessage(gameSession.getWhitePlayer().getId().toString(), null);
                                return new ResponseEntity<>(sidesMessage.toString(), HttpStatus.OK);
                            } else {                                    // black player
                                GameSession gameSession = gameQueue.poll();
                                gameSession.setBlackPlayer(player);
                                gameSession.setBlacksTime(120_000L);    // 2 minutes
                                player.setColor(Color.BLACK);
                                player.setGameSessionId(gameSession.getId());
                                gameSessions.put(gameSession.getId(), gameSession);
                                SidesMessage sidesMessage = new SidesMessage(
                                        gameSession.getWhitePlayer().getId().toString(),
                                        gameSession.getBlackPlayer().getId().toString()
                                );
                                return new ResponseEntity<>(sidesMessage.toString(), HttpStatus.OK);
                            }
                        }).orElse(new ResponseEntity<>("null", HttpStatus.ACCEPTED));
            } catch (Exception e) {
                e.printStackTrace();
                return new ResponseEntity<>(null, HttpStatus.ACCEPTED);
            }
        } else {
            return new ResponseEntity<>(null, HttpStatus.ACCEPTED);
        }
    }

    @MessageMapping("/chess/{toGameSession}")
    public void sendMoveMessage(@DestinationVariable String toGameSession, String move) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(MoveMessage.class, new MoveMessageSerializer())
                .create();
        try {
            MoveMessage moveMessage = gson.fromJson(move, MoveMessage.class);
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
                                log.info("Whites time: " + (gameSession.getWhitesTime() / 1000) / 60 + " min " + (gameSession.getWhitesTime() / 1000) % 60);
                                log.info("Blacks tiem: " + (gameSession.getBlacksTime() / 1000) / 60 + " min " + (gameSession.getBlacksTime() / 1000) % 60);
                                simpMessagingTemplate.convertAndSend("/topic/messages/" + toGameSession, moveMessage);
                            }
                        });
                log.info("move received");
            }

        } catch (JsonParseException e) {
            e.printStackTrace();
            log.info("Wrong move message!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
