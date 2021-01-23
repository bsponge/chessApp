package com.example.demo.controllers;

import chessLib.*;
import com.example.demo.moveMessage.MoveMessage;
import com.example.demo.serializers.MoveMessageSerializer;
import com.example.demo.serializers.PiecesSerializer;
import com.example.demo.sides.SidesMessage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Queue;
import java.util.UUID;

@Slf4j
@RestController
@CrossOrigin
public class ApiController {
    private static final ResponseEntity<Void> RESPONSE_OK = new ResponseEntity<>(HttpStatus.OK);
    private static final ResponseEntity<Void> RESPONSE_BAD = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

    private SimpMessagingTemplate simpMessagingTemplate;
    private Map<UUID, GameSession> gameSessions;
    private Queue<GameSession> gameQueue;
    private Map<UUID, Player> players;

    @Autowired
    public ApiController(SimpMessagingTemplate simpMessagingTemplate,
                         @Qualifier("gameSessions") Map<UUID, GameSession> gameSessions,
                         @Qualifier("gameQueue") Queue<GameSession> gameQueue,
                         @Qualifier("players") Map<UUID, Player> players) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.gameSessions = gameSessions;
        this.gameQueue = gameQueue;
        this.players = players;
    }

    @GetMapping("/print")
    public void print() {
        log.info(gameQueue.toString());
        log.info(gameSessions.toString());
    }

    @GetMapping("/reload")
    @ResponseBody
    public String reload(@CookieValue(value = "playerId", defaultValue = "none") String playerId) {
        if (!playerId.equals("none")) {
            try {
                log.info("RECEIVED PLAYER ID: " + playerId);
                UUID id = UUID.fromString(playerId);
                Player player = players.get(id);
                log.info("1");
                if (player != null) {
                    if (gameSessions.containsKey(player.getGameSessionId())) {
                        Gson gson = new GsonBuilder()
                                .registerTypeAdapter(Piece[][].class, new PiecesSerializer())
                                .create();
                        try {
                            return gson.toJson(gameSessions.get(player.getGameSessionId()).getChessboard());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                }
                return "null";
            } catch (Exception e) {
                return "null";
            }
        } else {
            return "null";
        }
    }

    @PostMapping("/findGame")
    public ResponseEntity<String> findGame(@CookieValue(value = "playerId", defaultValue = "none") String id) {
        if (!id.equals("none")) {
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
    }

    @MessageMapping("/chess/{toGameSession}")
    public void sendMoveMessage(@DestinationVariable String toGameSession, String move, @CookieValue(value = "playerId", defaultValue = "none") String playerId) {
        log.info(move);
        log.info("PlayerId from Cookie");
        log.info(playerId);
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(MoveMessage.class, new MoveMessageSerializer())
                .create();
        MoveMessage moveMessage = gson.fromJson(move, MoveMessage.class);
        if (moveMessage.getGameUuid() != null) {
            GameSession gameSession = gameSessions.get(moveMessage.getGameUuid());
            Move m = moveMessage.getMove();
            if (gameSession != null && m != null && gameSession.move(m.getFromX(), m.getFromY(), m.getToX(), m.getToY(), null)) {
                log.info("LEGAL MOVE");
                if ((m.getFromX() == 4 && m.getToX() == 6) || (m.getFromX() == 4 && m.getToX() == 2)) {
                    moveMessage.setCastle(true);
                }
                moveMessage.setChecksAndMates(gameSession);
                simpMessagingTemplate.convertAndSend("/topic/messages/" + toGameSession, moveMessage);
            }

        }
    }

    @MessageMapping("/chess/{toGameSession}/undo")
    public void undoLastMove(@DestinationVariable String toGameSession, @CookieValue(value = "playerId", defaultValue = "none") String playerId) {

    }
}
