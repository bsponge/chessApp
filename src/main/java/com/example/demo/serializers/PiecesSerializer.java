package com.example.demo.serializers;

import chessLib.Piece;
import com.google.gson.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.List;

public class PiecesSerializer implements JsonSerializer<List<Piece>> {

    @Override
    public JsonElement serialize(List<Piece> pieces, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        for (Piece piece : pieces) {
            jsonArray.add(serializePiece(piece));
        }
        jsonObject.add("pieces", jsonArray);
        return jsonObject;
    }

    private JsonObject serializePiece(Piece piece) {
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
