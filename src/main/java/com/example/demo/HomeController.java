package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;
/*

                                x  ________________
                                |   |
                                |   |
                                V   |
                                    |______________
                                    y ->

                             */
/*

        TODO:

EARLY:
        - dodanie bicia w przelocie


MID:
        - mozliwosc wielu gier na raz
        - dodanie odliczania czasu
        - dodanie cofania ruchow (pewnie trzeba zapisywac wszystkie ruchy)


FUTURE:
        - dodanie logowania i rejestrowania

 */


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