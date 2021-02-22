package com.example.demo.controllers;

import chessLibOptimized.Game;
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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
    private final Queue<Game> gameQueue;
    private final PlayersService players;
    private final Gson gsonChessboardSerializer;


    @Autowired
    public ApiController(GameSessionsService gameSessions,
                         PlayersService players,
                         @Qualifier("gameQueue") Queue<Game> gameQueue,
                         @Qualifier("gsonChessboardSerializer") Gson gsonChessboardSerializer) {
        this.gameSessions = gameSessions;
        this.gameQueue = gameQueue;
        this.players = players;
        this.gsonChessboardSerializer = gsonChessboardSerializer;
    }

    /**
     * Returns json String representing Piece[] of active GameSession
     * @param playerId player's uuid
     * @return json String representing Piece[] of active GameSession
     */
    @GetMapping("/reload/{uuid}")
    @ResponseBody
    public String reload(@CookieValue(value = "playerId", defaultValue = "none") String playerId, @PathVariable String uuid) {
        if (!playerId.equals("none")) {     // player has playerId attribute in cookie
            try {                           // if player has active game return json String representing array of pieces
                return Optional.of(uuid)
                        .map(UUID::fromString)
                        .map(gameSessions::get)
                        .map(Game::getChessboard)
                        .map(gsonChessboardSerializer::toJson)
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
    public ResponseEntity<String> findGame(@CookieValue(value = "playerId", defaultValue = "none") String playerId,
                                           HttpServletResponse response) {
        if (!playerId.equals("none")) {
            try {
                return Optional.of(playerId)
                        .map(UUID::fromString)
                        .map(players::get)
                        .map(player -> {
                            if (gameQueue.isEmpty()) {                  // white player
                                Game gameSession = new Game(player.getUuid());
                                gameSession.setWhitesTime(120_000L);    // 2 minutes
                                gameQueue.add(gameSession);
                                players.get(player.getUuid()).addGameUuid(gameSession.getUuid());
                                SidesMessage sidesMessage = new SidesMessage(gameSession.getWhitePlayerUuid().toString(), null);
                                Cookie cookie = new Cookie("gameUuid", gameSession.getUuid().toString());
                                response.addCookie(cookie);
                                return new ResponseEntity<>(sidesMessage.toString(), HttpStatus.OK);
                            } else {                                    // black player
                                Game gameSession = gameQueue.poll();
                                gameSession.setBlackPlayerUuid(player.getUuid());
                                gameSession.setBlacksTime(120_000L);    // 2 minutes
                                gameSessions.save(gameSession);
                                players.get(player.getUuid()).addGameUuid(gameSession.getUuid());
                                SidesMessage sidesMessage = new SidesMessage(
                                        gameSession.getWhitePlayerUuid().toString(),
                                        gameSession.getBlackPlayerUuid().toString()
                                );
                                Cookie cookie = new Cookie("gameUuid", gameSession.getUuid().toString());
                                response.addCookie(cookie);
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
