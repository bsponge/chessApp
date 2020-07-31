package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.UUID;

@Slf4j
@Data
@AllArgsConstructor
@Component
@SessionScope
public class Player {


    private UUID id;
    private UUID gameSessionId;
    private String side;

    @Autowired
    public Player(UUID id) {
        this.id = id;
    }

    public Player() {
        this.id = UUID.randomUUID();
    }
}
