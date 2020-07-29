package com.example.demo;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class Piece {
    public Type type;
    public Color color;
    public int x;
    public int y;
    public boolean alive = false;

    synchronized public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Piece() {
        this.x = -1;
        this.y = -1;
    }

    public void setPiece(Piece piece) {
        this.type = piece.getType();
        this.color = piece.getColor();
        this.x = piece.getX();
        this.y = piece.getY();
        this.alive = piece.isAlive();
    }

    public boolean isHittingOnDiagonal(Color yourSide) {
        return (this.type == Type.QUEEN || this.type == Type.BISHOP) && !(this.color == yourSide);
    }

    public boolean isHittingOnLine(Color yourSide) {
        return (this.type == Type.QUEEN || this.type == Type.ROOK) && !this.color.equals(yourSide);
    }

    public boolean isPawnOfColor(Color color) {
        return this.type == Type.PAWN && this.color == color;
    }



    public void setDead() {
        if (this.x == 6 && this.y == 0) {
            Thread.dumpStack();
        }
        this.type = null;
        this.color = null;
        this.alive = false;
        this.x = -1;
        this.y = -1;
    }

    public boolean canMove(Piece piece) {
        return this.color == piece.getColor();
    }

    public enum Type {
        ROOK, BISHOP, KNIGHT, QUEEN, KING, PAWN

    }

    public enum Color {
        WHITE, BLACK
    }

}
