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

/*
    TODO:
        - refactor sendMoveMessage
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

    @GetMapping("/reload")
    @ResponseBody
    public String reload(@CookieValue(value = "playerId", defaultValue = "none") String playerId) {
        if (!playerId.equals("none")) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(Piece[][].class, new PiecesSerializer())
                    .create();

            try {
                return Optional
                        .of(playerId)
                        .map(UUID::fromString)
                        .map(players::get)
                        .map(player -> gameSessions.get(player.getGameSessionId()))
                        .map(gameSession -> gson.toJson(gameSession.getChessboard()))
                        .orElse("null");
            } catch (Exception e) {
                return "null";
            }
        } else {
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
                            if (gameQueue.isEmpty()) {
                                GameSession gameSession = new GameSession(player);
                                gameQueue.add(gameSession);
                                player.setColor(Color.WHITE);
                                player.setGameSessionId(gameSession.getId());
                                SidesMessage sidesMessage = new SidesMessage(gameSession.getWhitePlayer().getId().toString(), null);
                                return new ResponseEntity<>(sidesMessage.toString(), HttpStatus.OK);
                            } else {
                                GameSession gameSession = gameQueue.poll();
                                gameSession.setBlackPlayer(player);
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
        /*if (!id.equals("none")) {
            UUID playerId;
            try {
                playerId = UUID.fromString(id);
                if (players.containsKey(playerId)) {
                    Player player = players.get(playerId);
                    GameSession gameSession;
                    SidesMessage sidesMessage;
                    if (gameQueue.isEmpty()) {      // queue of games is empty
                        gameSession = new GameSession(player);
                        gameQueue.add(gameSession);
                        player.setColor(Color.WHITE);
                        player.setGameSessionId(gameSession.getId());
                        sidesMessage = new SidesMessage(gameSession.getWhitePlayer().getId().toString(), null);
                        return new ResponseEntity<>(sidesMessage.toString(), HttpStatus.OK);
                    } else {                        // queue of games is not empty
                        gameSession = gameQueue.poll();
                        gameSession.setBlackPlayer(player);
                        player.setColor(Color.BLACK);
                        player.setGameSessionId(gameSession.getId());
                        gameSessions.put(gameSession.getId(), gameSession);
                        sidesMessage = new SidesMessage(
                                gameSession
                                        .getWhitePlayer()
                                        .getId()
                                        .toString(),
                                gameSession
                                        .getBlackPlayer()
                                        .getId()
                                        .toString());
                        simpMessagingTemplate
                                .convertAndSend(
                                        "/topic/messages/" + gameSession.getId(),
                                        sidesMessage);
                        return ResponseEntity.ok(sidesMessage.toString());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);

         */
    }

    @MessageMapping("/chess/{toGameSession}")
    public void sendMoveMessage(@DestinationVariable String toGameSession, String move) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(MoveMessage.class, new MoveMessageSerializer())
                .create();
        try {
            MoveMessage moveMessage = gson.fromJson(move, MoveMessage.class);
            if (moveMessage.isUndo()) {
                Optional.of(moveMessage.getGameUuid())
                        .map(gameSessions::get)
                        .ifPresent(gameSession -> {
                            if (gameSession.getMovesHistory().size() > 0) {
                                Move lastMove = gameSession.getLastMove();
                                lastMove = new Move(lastMove.getToX(), lastMove.getToY(), lastMove.getFromX(), lastMove.getFromY(), lastMove.getColor(), lastMove.getType(), lastMove.getEnemyColor(), lastMove.getEnemyType());
                                gameSession.undoLastMove();
                                MoveMessage mm = new MoveMessage(UUID.fromString(toGameSession), moveMessage.getPlayerUuid(), true, lastMove);
                                mm.setChecksAndMates(gameSession);
                                simpMessagingTemplate.convertAndSend("/topic/messages/" + toGameSession, mm);
                            }
                        });
            } else {
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
                            boolean b = gameSession.move(mv.getFromX(), mv.getFromY(), mv.getToX(), mv.getToY(), null);
                            if (a && b) {
                                if ((mv.getFromX() == 4 && mv.getToX() == 6) || (mv.getFromX() == 4 && mv.getToX() == 2)) {
                                    moveMessage.setCastle(true);
                                }
                                moveMessage.setChecksAndMates(gameSession);
                                if (moveMessage.isMateOnBlack() || moveMessage.isMateOnWhite()) {
                                    gamesHistoryService.saveGameHistory(gameSession);
                                }
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
        /*
        log.info(move);
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(MoveMessage.class, new MoveMessageSerializer())
                .create();
        MoveMessage moveMessage = gson.fromJson(move, MoveMessage.class);
        if (moveMessage.getGameUuid() != null) {
            GameSession gameSession = gameSessions.get(moveMessage.getGameUuid());
            if (moveMessage.isUndo()) {
                if (gameSession.getMovesHistory().size() > 0) {
                    Move m = gameSession.getMovesHistory().get(gameSession.getMovesHistory().size() - 1);
                    m = new Move(m.getToX(), m.getToY(), m.getFromX(), m.getFromY(), m.getColor(), m.getType(), m.getEnemyColor(), m.getEnemyType());
                    gameSession.undoLastMove();
                    MoveMessage mm = new MoveMessage(UUID.fromString(toGameSession), moveMessage.getPlayerUuid(), true, m);
                    mm.setChecksAndMates(gameSession);
                    log.info("Undo move");
                    simpMessagingTemplate.convertAndSend("/topic/messages/" + toGameSession, mm);
                }
            } else {
                Move m = moveMessage.getMove();
                boolean a = gameSession.getColorTurn() == Color.WHITE ? gameSession.getWhitePlayer().getId().equals(moveMessage.getPlayerUuid()) : Objects.requireNonNull(gameSession).getBlackPlayer().getId().equals(moveMessage.getPlayerUuid());
                boolean b = gameSession.move(Objects.requireNonNull(m).getFromX(), m.getFromY(), m.getToX(), m.getToY(), null);
                if (a && b) {
                    if ((m.getFromX() == 4 && m.getToX() == 6) || (m.getFromX() == 4 && m.getToX() == 2)) {
                        moveMessage.setCastle(true);
                    }
                    log.info("After move");
                    log.info(gameSession.getColorTurn().toString());
                    moveMessage.setChecksAndMates(gameSession);
                    log.info(Boolean.toString(moveMessage.isMateOnBlack()));
                    log.info(Boolean.toString(moveMessage.isMateOnWhite()));
                    if (moveMessage.isMateOnBlack() || moveMessage.isMateOnWhite()) {
                        log.info("SAVING GAME HISTORY");
                        gamesHistoryService.saveGameHistory(gameSession);
                    }
                    simpMessagingTemplate.convertAndSend("/topic/messages/" + toGameSession, moveMessage);
                }
            }

        }

         */
    }
}
