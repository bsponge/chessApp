package com.example.demo.controllers;

import chessLib.Player;
import com.example.demo.service.PlayersService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.UUID;

@Controller
public class GameInfoController {
    private PlayersService playersService;
    private Gson gsonPlayerSerializer;

    public GameInfoController(PlayersService playersService,
                              @Qualifier("gsonPlayerSerializer") Gson gsonPlayerSerializer) {
        this.playersService = playersService;
        this.gsonPlayerSerializer = gsonPlayerSerializer;
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
                Player player = playersService.get(id);
                return gsonPlayerSerializer.toJson(player);
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
                Player player = playersService.get(id);
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
}
