package com.example.demo.controllers;

import chessLib.Color;
import chessLib.GameSession;
import chessLib.Player;
import com.example.demo.service.GameSessionsService;
import com.example.demo.service.PlayersService;
import com.example.demo.sides.SidesMessage;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/*
    TODO:
        - undoMove accept
        - end game if time is over
 */

/**
 * ApiController is rest controller for client's game moves communication with server
 * @author js
 */

@Slf4j
@RestController
@CrossOrigin
public class ApiController {
    private final GameSessionsService gameSessions;
    private final Queue<GameSession> gameQueue;
    private final PlayersService players;
    private final Gson gsonPiecesSerializer;


    @Autowired
    public ApiController(GameSessionsService gameSessions,
                         PlayersService players,
                         @Qualifier("gameQueue") Queue<GameSession> gameQueue,
                         @Qualifier("gsonPiecesSerializer") Gson gsonPiecesSerializer) {
        this.gameSessions = gameSessions;
        this.gameQueue = gameQueue;
        this.players = players;
        this.gsonPiecesSerializer = gsonPiecesSerializer;
    }

    /**
     * Returns json String representing Piece[] of active GameSession
     * @param playerId player's uuid
     * @return json String representing Piece[] of active GameSession
     */
    @GetMapping("/reload")
    @ResponseBody
    public String reload(@CookieValue(value = "playerId", defaultValue = "none") String playerId) {
        if (!playerId.equals("none")) {     // player has playerId attribute in cookie
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
                        .map(gsonPiecesSerializer::toJson)
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
                                gameSessions.save(gameSession);
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
}
