package com.example.demo.controllers;

import com.example.demo.player.Player;
import com.example.demo.service.PlayersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Slf4j
@Controller
public class HomeController {
    private final PlayersService players;

    @Autowired
    public HomeController(PlayersService players) {
        this.players = players;
    }

    @GetMapping("/")
    public String homePage(@CookieValue(value = "playerId", defaultValue = "none") String playerId, HttpServletResponse response) {
        log.info(playerId);
        if (playerId.equals("none")) {
            Player player = new Player();
            Cookie cookie = new Cookie("playerId", player.getUuid().toString());
            players.save(player);
            log.info(player.toString());
            response.addCookie(cookie);
        } else {
            try {
                UUID id = UUID.fromString(playerId);
                if (!players.containsPlayerWithId(id)) {
                    Player player = new Player();
                    player.setUuid(id);
                    players.save(player);
                    log.info("New player " + player.toString());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "home";
    }

    @GetMapping("/play")
    public String playPage(@RequestParam("g") String uuid) {
        log.info(uuid);
        return "play/game";
    }

}