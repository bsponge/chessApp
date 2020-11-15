package com.example.demo.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.myProject.Piece;
import com.myProject.Player;

import java.lang.reflect.Type;

public class PlayerSerializer implements JsonSerializer<Player> {

    @Override
    public JsonElement serialize(Player player, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", player.getId().toString());
        jsonObject.addProperty("gameSessionId", player.getGameSessionId() == null ? "null" : player.getGameSessionId().toString());
        if (player.getSide() == null) {
            jsonObject.addProperty("side", "null");
        } else {
            jsonObject.addProperty("side", player.getSide().equals(Piece.Color.WHITE) ? "white" : "black");
        }
        return jsonObject;
    }
}
