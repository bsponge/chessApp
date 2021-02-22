package com.example.demo.controllers;

import com.example.demo.player.Player;
import com.example.demo.service.PlayersService;
import com.google.gson.Gson;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.UUID;

@Controller
public class GameInfoController {
    private PlayersService playersService;
    private Gson gsonPlayerSerializer;

    public GameInfoController(PlayersService playersService) {
        this.playersService = playersService;
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

    @GetMapping("/getGameSessionId")
    @ResponseBody
    public String getGameSessionId(@CookieValue(value = "playerId", defaultValue = "none") String playerId) {
        if (playerId.equals("none")) {
            return "no game session assigned";
        } else {
            try {
                UUID id = UUID.fromString(playerId);
                Player player = playersService.get(id);
                if (player.getGameUuids().isEmpty()) {
                    return "no game session assigned";
                } else {
                    //return player.getGameUuid().toString();
                    return Arrays.toString(player.getGameUuids().toArray());
                }
            } catch (Exception e) {
                return "no game session assigned";
            }
        }

    }
}
