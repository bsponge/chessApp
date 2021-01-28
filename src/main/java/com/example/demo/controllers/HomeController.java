package com.example.demo.controllers;

import chessLib.GameSession;
import chessLib.Player;
import com.example.demo.serializers.PlayerSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
public class HomeController {
    private Map<UUID, Player> players;
    private Map<UUID, GameSession> gameSessions;
    private static final Gson playerSerializer = new GsonBuilder()
            .registerTypeAdapter(Player.class, new PlayerSerializer())
            .create();

    @Autowired
    public HomeController(@Qualifier("players") Map<UUID, Player> players,@Qualifier("gameSessions") Map<UUID, GameSession> gameSessions) {
        this.players = players;
        this.gameSessions = gameSessions;
    }

    @GetMapping("/")
    public String homePage(@CookieValue(value = "playerId", defaultValue = "none") String playerId, HttpServletResponse response) {
        log.info(playerId);
        if (playerId.equals("none")) {
            Player player = new Player();
            Cookie cookie = new Cookie("playerId", player.getId().toString());
            players.put(player.getId(), player);
            log.info(player.toString());
            response.addCookie(cookie);
        }
        try {
            UUID id = UUID.fromString(playerId);
            if (!players.containsKey(id)) {
                Player player = new Player();
                player.setId(id);
                players.put(id, player);
                log.info("New player " + player.toString());
            }
        } catch (Exception e) {

        }
        return "home";
    }

    @GetMapping("/getId")
    @ResponseBody
    public String getId(@CookieValue(value = "playerId", defaultValue = "none") String playerId) {
        if (playerId.equals("none")) {
            return "none";
        } else {
            return playerId;
        }
    }

    @GetMapping("getInfo")
    @ResponseBody
    public String getInfo(@CookieValue(value = "playerId", defaultValue = "none") String playerId) {
        if (playerId.equals("none")) {
            return null;
        } else {
            try {
                UUID id = UUID.fromString(playerId);
                Player player = players.get(id);
                return playerSerializer.toJson(player);
            } catch (Exception e) {
                return null;
            }
        }
    }

    @GetMapping("/getGameSessionId")
    @ResponseBody
    public String getGameSessionId(@CookieValue(value = "playerId", defaultValue = "none") String playerId) {
        if (playerId.equals("none")) {
            return "no game session assigned";
        } else {
            try {
                UUID id = UUID.fromString(playerId);
                Player player = players.get(id);
                if (player.getGameSessionId() == null) {
                    return "no game session assigned";
                } else {
                    return player.getGameSessionId().toString();
                }
            } catch (Exception e) {
                return "no game session assigned";
            }
        }

    }

    @GetMapping("/greet")
    public String socketTest() {
        return "socketTest";
    }
}