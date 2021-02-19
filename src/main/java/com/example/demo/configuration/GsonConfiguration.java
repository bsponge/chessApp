package com.example.demo.configuration;

import com.example.demo.moveMessage.MoveMessage;
import com.example.demo.serializers.ChessboardSerializer;
import com.example.demo.serializers.MoveMessageSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GsonConfiguration {
    @Bean("gsonMoveMessageSerializer")
    public Gson gsonMoveMessageSerializer() {
        return new GsonBuilder()
                .registerTypeAdapter(MoveMessage.class, new MoveMessageSerializer())
                .create();
    }

    @Bean("gsonChessboardSerializer")
    public Gson gsonChessboardSerializer() {
        return new GsonBuilder()
                .registerTypeAdapter(int[][].class, new ChessboardSerializer())
                .create();
    }
}
