package com.example.demo.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("game")
@Slf4j
public class PlayController {
    @GetMapping("/")
    public String playPage(@RequestParam("g") String gameUuid) {
        log.info(gameUuid);
        return "game";
    }
}