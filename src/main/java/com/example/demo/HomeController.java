package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.lang.constant.Constable;
import java.util.Hashtable;
import java.util.UUID;
import java.util.stream.Collectors;
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
        - fix dostepu do api (zmiana adresow na sensowne)


FUTURE:
        - dodanie logowania i rejestrowania

 */


@Slf4j
@Controller
@EnableScheduling
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