package com.example.demo.sides;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SidesMessage {
    private final int msgType = 2;
    private String whiteSide;
    private String blackSide;

    public String toString() {
        if (blackSide == null) {
            return String.format("{\"msgType\":%d,\"whiteSide\":\"%s\",\"blackSide\":null}", 2, whiteSide);
        } else {
            return String.format("{\"msgType\":%d,\"whiteSide\":\"%s\",\"blackSide\":\"%s\"}", 2, whiteSide, blackSide);
        }
    }
}
