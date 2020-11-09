package com.example.demo;

import lombok.Data;

@Data
public class Move {
    private int fromX;
    private int fromY;
    private int toX;
    private int toY;

    public Move() {}


}
