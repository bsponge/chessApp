package com.example.demo.serializers;

import chessLib.Piece;
import com.google.gson.*;

import java.lang.reflect.Type;

public class PiecesSerializer implements JsonSerializer<Piece[][]> {

    @Override
    public JsonElement serialize(Piece[][] pieces, Type type, JsonSerializationContext jsonSerializationContext) {
        /*
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < pieces.length; ++i) {
            JsonArray array = new JsonArray();
            for (int j = 0; j < pieces[i].length; ++j) {
                array.add(serializePiece(pieces[i][j]));
            }
            jsonArray.add(array);
        }
        jsonObject.add("pieces", jsonArray);
        return jsonObject;
         */
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        for (int i = 0; i < pieces.length; ++i) {
            for (int j = 0; j < pieces[i].length; ++j) {
                jsonArray.add(serializePiece(pieces[i][j]));
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
