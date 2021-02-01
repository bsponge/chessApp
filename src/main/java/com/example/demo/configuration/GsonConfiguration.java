package com.example.demo.configuration;

import chessLib.Piece;
import com.example.demo.moveMessage.MoveMessage;
import com.example.demo.serializers.MoveMessageSerializer;
import com.example.demo.serializers.PiecesSerializer;
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

    @Bean("gsonPiecesSerializer")
    public Gson gsonPiecesSerializer() {
        return new GsonBuilder()
                .registerTypeAdapter(Piece[][].class, new PiecesSerializer())
                .create();
    }
}