package com.example.demo.serializers;

import com.example.demo.moveMessage.MoveMessage;
import com.google.gson.*;
import com.myProject.Move;

import java.lang.reflect.Type;
import java.util.UUID;

public class MoveMessageSerializer implements JsonDeserializer<MoveMessage> {

    @Override
    public MoveMessage deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonObject move = jsonObject.getAsJsonObject("move");
            return new MoveMessage(UUID.fromString(jsonObject.get("id").getAsString()),
                    new Move(move.get("fromX").getAsInt(),
                            move.get("fromY").getAsInt(),
                            move.get("toX").getAsInt(),
                            move.get("toY").getAsInt()));
    }
}
