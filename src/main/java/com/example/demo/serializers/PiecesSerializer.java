package com.example.demo.serializers;

import chessLib.Piece;
import com.google.gson.*;

import java.lang.reflect.Type;

public class PiecesSerializer implements JsonSerializer<Piece[][]> {

    @Override
    public JsonElement serialize(Piece[][] pieces, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        for (Piece[] piece : pieces) {
            for (Piece value : piece) {
                jsonArray.add(serializePiece(value));
            }
        }
        jsonObject.add("pieces", jsonArray);
        return jsonObject;
    }

    private JsonElement serializePiece(Piece piece) {
        if (piece == null) {
            return null;
        } else {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("color", piece.getColor().toString());
            jsonObject.addProperty("type", piece.getType().toString());
            jsonObject.addProperty("x", piece.getX());
            jsonObject.addProperty("y", piece.getY());
            return jsonObject;
        }
    }
}
