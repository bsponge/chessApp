package com.example.demo.serializers;

import com.google.gson.*;

import java.lang.reflect.Type;

public class ChessboardSerializer implements JsonSerializer<Integer[][]> {
    @Override
    public JsonElement serialize(Integer[][] chessboard, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                jsonArray.add(chessboard[i][j]);
            }
        }
        jsonObject.add("chessboard", jsonArray);
        return jsonObject;
    }

}
