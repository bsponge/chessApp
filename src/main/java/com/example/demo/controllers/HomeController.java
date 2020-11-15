package com.example.demo.controllers;

import com.example.demo.serializers.PlayerSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.myProject.GameSession;
import com.myProject.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Hashtable;
import java.util.UUID;

@Slf4j
@Controller
@SessionAttributes("Player")
public class HomeController {
    private Hashtable<UUID, Player> players;
    private Hashtable<UUID, GameSession> gameSessions;
    private static final Gson playerSerializer = new GsonBuilder()
            .registerTypeAdapter(Player.class, new PlayerSerializer())
            .create();

    @ModelAttribute("Player")
    public Player player() {
        return new Player();
    }


    @Autowired
    public HomeController(@Qualifier("players") Hashtable<UUID, Player> players,@Qualifier("gameSessions") Hashtable<UUID, GameSession> gameSessions) {
        this.players = players;
        this.gameSessions = gameSessions;
    }

    @GetMapping("/")
    public String homePage(@ModelAttribute("Player") Player player) {
        if (!players.containsKey(player.getId())) {
            players.put(player.getId(), player);
        }
        log.info(player.toString());
        return "home";
    }

    @GetMapping("/getId")
    @ResponseBody
    public String getId(@ModelAttribute("Player") Player player) {
        return player.getId().toString();
    }

    @GetMapping("getInfo")
    @ResponseBody
    public String getInfo(@ModelAttribute("Player") Player player) {
        return playerSerializer.toJson(player);
    }

    @GetMapping("/getGameSessionId")
    @ResponseBody
    public String getGameSessionId(@ModelAttribute("Player") Player player) {
        if (player.getGameSessionId() == null) {
            return null;
        } else {
            return player.getGameSessionId().toString();
        }
    }

    @GetMapping("/greet")
    public String socketTest() {
        return "socketTest";
    }
}