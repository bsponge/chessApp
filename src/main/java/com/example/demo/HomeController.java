package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;

@Slf4j
@Controller
@SessionAttributes("player")
public class HomeController {
    @ModelAttribute("player")
    public Player player() {
        return new Player();
    }

    // szachownica
    @GetMapping("/")
    public String homePage() {
        return "home";
    }
}