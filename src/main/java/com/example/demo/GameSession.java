package com.example.demo;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;
import java.util.Vector;


@Data
@Component
@Slf4j
public class GameSession {
    public List<Piece> pieces;
    private UUID playersTurn;
    private Player playerWhite;
    private Player playerBlack;
    private boolean playerWhiteReady = false;
    private boolean playerBlackReady = false;
    private UUID sessionId;
    private volatile boolean checkOnWhite = false;
    private volatile boolean checkOnBlack = false;
    private volatile boolean checkMate = false;
    private volatile int turn = 0;

    @Autowired
    public GameSession(Vector<Piece> pieces) {
        this.pieces = pieces;
    }

    @PostConstruct
    private void postConstruct() {
        for (int i = 0; i < 32; ++i) {
            this.pieces.add(new Piece());
            this.pieces.get(i).setAlive(true);
        }
        for (int i = 0; i < 8; ++i) {
            // SETTING PAWNS
            this.pieces.get(i).setLocation(1, i);
            this.pieces.get(i).setType(Piece.Type.PAWN);
            this.pieces.get(i).setColor(Piece.Color.BLACK);

            this.pieces.get(8 + i).setLocation(6, i);
            this.pieces.get(8 + i).setType(Piece.Type.PAWN);
            this.pieces.get(8 + i).setColor(Piece.Color.WHITE);

            this.pieces.get(16 + i).setLocation(7, i);
            this.pieces.get(16 + i).setColor(Piece.Color.WHITE);
            this.pieces.get(24 + i).setLocation(0, i);
            this.pieces.get(24 + i).setColor(Piece.Color.BLACK);
        }
        log.info(String.valueOf(this.pieces.stream().filter(e -> e.getType() == Piece.Type.PAWN && e.getColor() == Piece.Color.WHITE).count()));

        for (int i = 0; i < 2; ++i) {
            this.pieces.get(16 + i * 8).setType(Piece.Type.ROOK);
            this.pieces.get(17 + i * 8).setType(Piece.Type.KNIGHT);
            this.pieces.get(18 + i * 8).setType(Piece.Type.BISHOP);
            this.pieces.get(19 + i * 8).setType(Piece.Type.QUEEN);
            this.pieces.get(20 + i * 8).setType(Piece.Type.KING);
            this.pieces.get(21 + i * 8).setType(Piece.Type.BISHOP);
            this.pieces.get(22 + i * 8).setType(Piece.Type.KNIGHT);
            this.pieces.get(23 + i * 8).setType(Piece.Type.ROOK);
        }
}

    public synchronized Piece getPiece(int x, int y) {
        return pieces
                .stream()
                .filter(e -> e.getX() == x && e.getY() == y)
                .findFirst()
                .orElse(new Piece());
    }

    public void addPiece(Piece piece) {
        boolean b = pieces.add(piece);
        log.info("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        log.info("DODAWANIE :" + b);
    }

    public synchronized boolean move(Piece piece, int x, int y) {
        log.info("W MOVE");
        if (piece.getType() == Piece.Type.PAWN) {
            log.info(pieces.toString());
        }
        boolean canMove = true;
        int copyX;
        int copyY;
        int horizontal;
        int vertical;
        boolean descending;
        //log.info("W MOVE: " + piece.getType());
        if (x > -1 && x < 8 && y > -1 && y < 8) {
            switch (piece.getType()) {
                case PAWN:
                    switch (piece.getColor()) {
                        case WHITE:
                            if (piece.getX() - x == 1
                                    && piece.getY() == y
                                    && !this.getPiece(x, y).isAlive()) {
                            } else if (piece.getX() == 6
                                    && piece.getX() - x == 2
                                    && piece.getY() == y
                                    && !this.getPiece(x + 1, y).isAlive()
                                    && !this.getPiece(x, y).isAlive()) {
                                if (piece.getX() == 6 && piece.getY() == 7) {
                                    log.info("TEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
                                }
                                log.info("RUSZANIE PIONEM");
                                log.info(this.getPiece(4, 7).toString());
                                log.info(this.getPiece(x + 1, y).toString());
                                log.info(this.getPiece(x, y).toString());

                            } else if (piece.getX() - x == 1
                                    && Math.abs(piece.getY() - y) == 1
                                    && this.getPiece(x, y).getColor() == Piece.Color.BLACK) {
                            } else {
                                canMove = false;
                            }
                            break;
                        case BLACK:
                            if (x - piece.getX() == 1
                                    && piece.getY() == y
                                    && !this.getPiece(piece.getX()+x, y).isAlive()) {
                            } else if (piece.getX() == 1
                                    && x - piece.getX() == 2
                                    && piece.getY() == y
                                    && !this.getPiece(x, y).isAlive()
                                    && !this.getPiece(x-1, y).isAlive()) {
                            } else if (x - piece.getX() == 1
                                    && Math.abs(piece.getY() - y) == 1
                                    && this.getPiece(x, y).getColor() == Piece.Color.WHITE) {
                            } else {
                                canMove = false;
                            }
                            break;
                    }
                    break;
                case ROOK:
                    if (piece.getX() == x) {
                        descending = piece.getY() - y > 0;
                        copyY = piece.getY();
                        log.info("ROOK MOVING1");
                        while (canMove && copyY != y) {
                            if (descending) {
                                if (piece.getColor() == this.getPiece(x, --copyY).getColor()) {
                                    log.info("CANNOT MOVE ROOK1");
                                    canMove = false;
                                }
                            } else if (piece.getColor() == this.getPiece(x, ++copyY).getColor()){
                                log.info("CANNOT MOVE ROOK2");
                                canMove = false;
                            }
                        }
                    } else if (piece.getY() == y) {
                        descending = piece.getX() - x > 0;
                        copyX = piece.getX();
                        log.info("ROOK MOVING2");
                        while (canMove && copyX != x) {
                            if (descending) {
                                if (piece.getColor() == this.getPiece(--copyX, y).getColor()) {
                                    canMove = false;
                                }
                            } else if (piece.getColor() == this.getPiece(++copyX, y).getColor()){
                                canMove = false;
                            }
                        }
                    } else {
                        canMove = false;
                    }
                    break;
                case BISHOP:
                    horizontal = piece.getY() - y < 0 ? 2 : 4;
                    vertical = piece.getX() - x > 0 ? 3 : 1;
                    copyX = piece.getX();
                    copyY = piece.getY();
                    //log.info("BEFORE TEST");
                    //log.info(horizontal + " " + vertical);
                    if (Math.abs(piece.getY() - y) == Math.abs(piece.getX() - x)) {
                        //log.info("PASSED");
                        while (canMove && copyX > -1 && copyX < 8 && copyY > -1 && copyY < 8) {
                            //log.info("W WHILE");
                            if (vertical == 1 && horizontal == 2) {
                                if (this.getPiece(++copyX, ++copyY).getColor() == piece.getColor()) {
                                    //log.info(this.getPiece(copyX, copyY).toString());
                                    //log.info("1 i 2");
                                    canMove = false;
                                }
                            } else if (vertical == 3 && horizontal == 4) {
                                if (this.getPiece(--copyX, ++copyY).getColor() == piece.getColor()) {
                                    //log.info("CO");
                                    canMove = false;
                                }
                            } else if (vertical == 3) {
                                //log.info("NIE");
                                if (this.getPiece(--copyX, --copyY).getColor() == piece.getColor()) {
                                    canMove = false;
                                }
                            } else {
                                if (this.getPiece(++copyX, --copyY).getColor() == piece.getColor()) {
                                    //log.info("TUTUT");
                                    canMove = false;
                                }
                            }
                        }
                    } else {
                        canMove = false;
                    }
                    break;
                case KNIGHT:
                    if ((Math.abs(piece.getX() - x) == 2 && Math.abs(piece.getY() - y) == 1)
                            || (Math.abs(piece.getX() - x) == 1 && Math.abs(piece.getY() - y) == 2)) {
                        if (this.getPiece(x, y).getColor() == piece.getColor()) {
                            canMove = false;
                        }
                    } else {
                        canMove = false;
                    }
                    break;
                case QUEEN:
                    horizontal = piece.getY() - y < 0 ? 2 : 4;
                    vertical = piece.getX() - x > 0 ? 3 : 1;
                    copyX = piece.getX();
                    copyY = piece.getY();
                    //log.info("BEFORE TEST");
                    //log.info(horizontal + " " + vertical);
                    if (Math.abs(piece.getY() - y) == Math.abs(piece.getX() - x)) {
                        //log.info("PASSED");
                        while (canMove && copyX > -1 && copyX < 8 && copyY > -1 && copyY < 8) {
                            //log.info("W WHILE");
                            if (vertical == 1 && horizontal == 2) {
                                if (this.getPiece(++copyX, ++copyY).getColor() == piece.getColor()) {
                                    //log.info(this.getPiece(copyX, copyY).toString());
                                    //log.info("1 i 2");
                                    canMove = false;
                                }
                            } else if (vertical == 3 && horizontal == 4) {
                                if (this.getPiece(--copyX, ++copyY).getColor() == piece.getColor()) {
                                    //log.info("CO");
                                    canMove = false;
                                }
                            } else if (vertical == 3) {
                                //log.info("NIE");
                                if (this.getPiece(--copyX, --copyY).getColor() == piece.getColor()) {
                                    canMove = false;
                                }
                            } else {
                                if (this.getPiece(++copyX, --copyY).getColor() == piece.getColor()) {
                                    //log.info("TUTUT");
                                    canMove = false;
                                }
                            }
                        }
                    } else if (piece.getX() == x) {
                        descending = piece.getY() - y > 0;
                        copyY = piece.getY();
                        while (canMove && copyY != y) {
                            if (descending) {
                                if (piece.getColor() == this.getPiece(x, --copyY).getColor()) {
                                    canMove = false;
                                }
                            } else if (piece.getColor() == this.getPiece(x, ++copyY).getColor()){
                                canMove = false;
                            }
                        }
                    } else if (piece.getY() == y) {
                        descending = piece.getX() - x > 0;
                        copyX = piece.getX();
                        while (canMove && copyX != x) {
                            if (descending) {
                                if (piece.getColor() == this.getPiece(--copyX, y).getColor()) {
                                    canMove = false;
                                }
                            } else if (piece.getColor() == this.getPiece(++copyX, y).getColor()){
                                canMove = false;
                            }
                        }
                    } else {
                        canMove = false;
                    }
                    break;
                case KING:
                    if (Math.abs(piece.getX() - x) < 2 && Math.abs(piece.getY() - y) < 2) {
                        log.info("MOVING KING: " + (this.getPiece(x, y).getColor() == piece.getColor()) + " " + this.getPiece(x, y).getColor());
                        if (this.getPiece(x, y).getColor() == piece.getColor()) {
                            canMove = false;
                        }
                    } else {
                        canMove = false;
                    }
            }
        } else {
            canMove = false;
        }


        return canMove;
    }

    public synchronized void setPieceDead(int x, int y) {
            //log.info("DELETING QUEEN!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            //log.info(pieces.stream().filter(e -> e.getX() == x && e.getY() == y).findAny().get() + " ");
        this.pieces.removeIf(e -> e.getX() == x && e.getY() == y);
        //log.info("REMOVING PIECE :" + p);
   }
}
