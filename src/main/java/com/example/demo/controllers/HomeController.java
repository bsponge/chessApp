package com.example.demo.controllers;

import com.example.demo.player.Player;
import com.example.demo.repository.AccountRepository;
import com.example.demo.service.AccountService;
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
    private final AccountService accountService;
    int i = 0;

    @Autowired
    public HomeController(PlayersService players, AccountService accountService) {
        this.players = players;
        this.accountService = accountService;
    }

    @GetMapping("/")
    public String homePage(@CookieValue(value = "token", defaultValue = "none") String jwtToken, HttpServletResponse response) {
        if (i == 0) {
            accountService.registerNewUser("123", "123", "123");
            i = 1;
        }
        log.info(jwtToken);
        if (jwtToken.equals("none")) {
            Player player = new Player();
            players.save(player);
            log.info(player.toString());
        }
        return "home";
    }

    @GetMapping("/play")
    public String playPage(@RequestParam("g") String uuid) {
        log.info(uuid);
        return "play/game";
    }

}