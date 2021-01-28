package com.example.demo.serializers;

import chessLib.Color;
import chessLib.Move;
import com.example.demo.moveMessage.MoveMessage;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.UUID;

public class MoveMessageSerializer implements JsonDeserializer<MoveMessage> {

    @Override
    public MoveMessage deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonObject move = jsonObject.getAsJsonObject("move");
            return new MoveMessage(UUID.fromString(jsonObject.get("id").getAsString()),
                    UUID.fromString(jsonObject.get("playerId").getAsString()),
                    jsonObject.get("isUndo").getAsBoolean(),
                    new Move(move.get("fromX").getAsInt(),
                            move.get("fromY").getAsInt(),
                            move.get("toX").getAsInt(),
                            move.get("toY").getAsInt()));
    }
}
