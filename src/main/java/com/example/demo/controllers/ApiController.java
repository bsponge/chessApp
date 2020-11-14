package com.example.demo.controllers;

import com.example.demo.move.MoveMessage;
import com.example.demo.move.MoveMessageSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.myProject.GameSession;
import com.myProject.Piece;
import com.myProject.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Hashtable;
import java.util.Queue;
import java.util.UUID;

@Slf4j
@RestController
@CrossOrigin
public class ApiController {
    private static final ResponseEntity<Void> RESPONSE_OK = new ResponseEntity<>(HttpStatus.OK);
    private static final ResponseEntity<Void> RESPONSE_BAD = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

    private SimpMessagingTemplate simpMessagingTemplate;
    private Hashtable<UUID, GameSession> gameSessions;
    private Queue<GameSession> gameQueue;
    private Hashtable<UUID, Player> players;

    @Autowired
    public ApiController(SimpMessagingTemplate simpMessagingTemplate,@Qualifier("gameSessions") Hashtable gameSessions, Queue<GameSession> gameQueue, @Qualifier("players") Hashtable<UUID, Player> players) {
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

    @PostMapping("/findGame")
    public ResponseEntity<Void> findGame(@RequestBody String id) {
        if (id != null) {
            id = id.substring(0, id.length()-1);
            UUID playerId;
            try {
                playerId = UUID.fromString(id);
                if (players.containsKey(playerId)) {
                    Player player = players.get(playerId);
                    GameSession gameSession;
                    if (gameQueue.isEmpty()) {
                        gameSession = new GameSession(player);
                        gameQueue.add(gameSession);
                        player.setSide(Piece.Color.WHITE);
                        player.setGameSessionId(gameSession.getId());
                    } else {
                        gameSession = gameQueue.poll();
                        gameSession.setPlayerBlack(player);
                        player.setSide(Piece.Color.BLACK);
                        player.setGameSessionId(gameSession.getId());
                        gameSessions.put(gameSession.getId(), gameSession);
                    }
                    return RESPONSE_OK;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return RESPONSE_BAD;
    }

    @MessageMapping("/chess/{toGameSession}")
    public void sendMoveMessage(@DestinationVariable String toGameSession, String move) {
        log.info(move);
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(MoveMessage.class, new MoveMessageSerializer())
                .create();
        MoveMessage moveMessage = gson.fromJson(move, MoveMessage.class);
        log.info(moveMessage.toString());
        if (moveMessage.getGameUuid() != null) {
            if (moveMessage != null) {
                GameSession gameSession = gameSessions.get(moveMessage.getGameUuid());
                if (gameSession != null && !gameSession.move(moveMessage.getMove()).equals(GameSession.WRONG_MOVE)) {
                    simpMessagingTemplate.convertAndSend("/topic/messages/" + toGameSession, moveMessage);
                }
            }
        }
    }

}
