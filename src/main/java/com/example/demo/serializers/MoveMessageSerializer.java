package com.example.demo.serializers;

import chessLibOptimized.Move;
import com.example.demo.moveMessage.MoveMessage;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

public class MoveMessageSerializer implements JsonDeserializer<MoveMessage> {

    @Override
    public MoveMessage deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return Optional.of(jsonElement.getAsJsonObject())
                .map(jsonObject ->  new MoveMessage(
                        UUID.fromString(
                                Optional.ofNullable(jsonObject.get("id"))
                                        .map(JsonElement::getAsString)
                                        .orElseThrow()),
                        UUID.fromString(
                                Optional.ofNullable(jsonObject.get("playerId"))
                                        .map(JsonElement::getAsString)
                                        .orElseThrow()),
                        Optional.ofNullable(jsonObject.get("undo"))
                                .map(JsonElement::getAsInt)
                                .orElseThrow(),
                        new Move(
                                Optional.ofNullable(jsonObject.getAsJsonObject("move")).map(move -> move.get("fromX")).map(JsonElement::getAsInt).orElseThrow(),
                                Optional.ofNullable(jsonObject.getAsJsonObject("move")).map(move -> move.get("fromY")).map(JsonElement::getAsInt).orElseThrow(),
                                Optional.ofNullable(jsonObject.getAsJsonObject("move")).map(move -> move.get("toX")).map(JsonElement::getAsInt).orElseThrow(),
                                Optional.ofNullable(jsonObject.getAsJsonObject("move")).map(move -> move.get("toY")).map(JsonElement::getAsInt).orElseThrow()
                        ),
                        Optional.ofNullable(jsonObject.get("promotionType"))
                                .map(JsonElement::getAsInt)
                                .orElse(0)
                        )
                ).orElse(MoveMessage.WRONG_MOVE_MESSAGE);
        /*
        return new MoveMessage(UUID.fromString(jsonObject.get("id").getAsString()),
                UUID.fromString(jsonObject.get("playerId").getAsString()),
                jsonObject.get("isUndo").getAsBoolean(),
                new Move(move.get("fromX").getAsInt(),
                        move.get("fromY").getAsInt(),
                        move.get("toX").getAsInt(),
                        move.get("toY").getAsInt()));

         */
    }
}
