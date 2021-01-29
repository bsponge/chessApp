package com.example.demo.serializers;

import chessLib.Color;
import chessLib.Player;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class PlayerSerializer implements JsonSerializer<Player> {

    @Override
    public JsonElement serialize(Player player, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("msgType", 2);
        jsonObject.addProperty("id", player.getId().toString());
        jsonObject.addProperty("gameSessionId", player.getGameSessionId() == null ? "null" : player.getGameSessionId().toString());
        if (player.getColor() == null) {
            jsonObject.addProperty("side", "null");
        } else {
            jsonObject.addProperty("side", player.getColor().equals(Color.WHITE) ? "white" : "black");
        }
        return jsonObject;
    }
}
