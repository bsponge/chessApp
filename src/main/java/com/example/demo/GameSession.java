package com.example.demo;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.example.demo.Piece.Type.BISHOP;


/*
                 _______________________________________
             7  | __ | __ | __ | __ | __ | __ | __ | __ |
             6  | __ | __ | __ | __ | __ | __ | __ | __ |
             5  | __ | __ | __ | __ | __ | __ | __ | __ |
             4  | __ | __ | __ | __ | __ | __ | __ | __ |
             3  | __ | __ | __ | __ | __ | __ | __ | __ |
             2  | __ | __ | __ | __ | __ | __ | __ | __ |
    ^        1  | __ | __ | __ | __ | __ | __ | __ | __ |
    |        0  | __ | __ | __ | __ | __ | __ | __ | __ |
    y      x ->   0    1    2    3    4    5    6    7


        chessboard[x][y];

 */


/*
            TODO:
            - loadGameFromFEN dodaje tylko figury, brak obslugi calego wyrazenia FEN (bicie w przelocie i liczba ruchów)
            - napisac testy
            - przeniesienie usuwania piona z bicia w przelocie z isMoveLegal do makeMove
            - sprawdzenie roszady
 */

@Data
@Slf4j
public class GameSession {
    private Player playerWhite;
    private Player playerBlack;
    private Piece[][] chessboard;
    private Set<Piece> whitePieces;
    private Set<Piece> blackPieces;
    private boolean isCheckOnWhite = false;
    private boolean isCheckOnBlack = false;
    private boolean isWhiteLeftCastleAvailable = true;
    private boolean isWhiteRightCastleAvailable = true;
    private boolean isBlackLeftCastleAvailable = true;
    private boolean isBlackRightCastleAvailable = true;
    private Piece.Color turn = Piece.Color.WHITE;
    private Piece whiteKing;
    private Piece blackKing;
    private Move lastMove;

    public GameSession() {
        chessboard = new Piece[8][];
        whitePieces = new HashSet<>();
        blackPieces = new HashSet<>();
        for (int i = 0; i < 8; ++i) {
            chessboard[i] = new Piece[8];
        }

        chessboard[0][0] = new Piece(Piece.Type.ROOK, Piece.Color.WHITE, 0, 0);
        whitePieces.add(chessboard[0][0]);
        chessboard[1][0] = new Piece(Piece.Type.KNIGHT, Piece.Color.WHITE, 1, 0);
        whitePieces.add(chessboard[1][0]);
        chessboard[2][0] = new Piece(BISHOP, Piece.Color.WHITE, 2, 0);
        whitePieces.add(chessboard[2][0]);
        chessboard[3][0] = new Piece(Piece.Type.QUEEN, Piece.Color.WHITE, 3, 0);
        whitePieces.add(chessboard[3][0]);
        chessboard[4][0] = new Piece(Piece.Type.KING, Piece.Color.WHITE, 4, 0);
        whitePieces.add(chessboard[4][0]);
        whiteKing = chessboard[4][0];
        chessboard[5][0] = new Piece(BISHOP, Piece.Color.WHITE, 5, 0);
        whitePieces.add(chessboard[5][0]);
        chessboard[6][0] = new Piece(Piece.Type.KNIGHT, Piece.Color.WHITE, 6, 0);
        whitePieces.add(chessboard[6][0]);
        chessboard[7][0] = new Piece(Piece.Type.ROOK, Piece.Color.WHITE, 7, 0);
        whitePieces.add(chessboard[7][0]);

        chessboard[0][7] = new Piece(Piece.Type.ROOK, Piece.Color.BLACK, 0, 7);
        blackPieces.add(chessboard[0][7]);
        chessboard[1][7] = new Piece(Piece.Type.KNIGHT, Piece.Color.BLACK, 1, 7);
        blackPieces.add(chessboard[1][7]);
        chessboard[2][7] = new Piece(BISHOP, Piece.Color.BLACK, 2, 7);
        blackPieces.add(chessboard[2][7]);
        chessboard[3][7] = new Piece(Piece.Type.QUEEN, Piece.Color.BLACK, 3, 7);
        blackPieces.add(chessboard[3][7]);
        chessboard[4][7] = new Piece(Piece.Type.KING, Piece.Color.BLACK, 4, 7);
        blackPieces.add(chessboard[4][7]);
        blackKing = chessboard[4][7];
        chessboard[5][7] = new Piece(BISHOP, Piece.Color.BLACK, 5, 7);
        blackPieces.add(chessboard[5][7]);
        chessboard[6][7] = new Piece(Piece.Type.KNIGHT, Piece.Color.BLACK, 6, 7);
        blackPieces.add(chessboard[6][7]);
        chessboard[7][7] = new Piece(Piece.Type.ROOK, Piece.Color.BLACK, 7, 7);
        blackPieces.add(chessboard[7][7]);


        for (int i = 0; i < 8; ++i) {
            chessboard[i][1] = new Piece(Piece.Type.PAWN, Piece.Color.WHITE, i, 1);
            whitePieces.add(chessboard[i][1]);
            chessboard[i][6] = new Piece(Piece.Type.PAWN, Piece.Color.BLACK, i, 6);
            blackPieces.add(chessboard[i][6]);
        }
    }

    public void printChessboard() {
        for (int i = 7; i > -1; --i) {
            for (int j = 0; j < 8; ++j) {
                if (chessboard[j][i] != null) {
                    System.out.print("|" + chessboard[j][i].getColor().toString() + " " + chessboard[j][i].getType().toString() + "|");
                } else {
                    System.out.print("|         |");
                }
            }
            System.out.println("");
        }
        System.out.println("");
        /*System.out.println("WHITE");
        for (Piece p : whitePieces) {
            System.out.println(p);
        }
        System.out.println("BLACK");
        for (Piece p : blackPieces) {
            System.out.println(p);
        }*/
    }

    public void loadGameFromFEN(String fen) {
        chessboard = new Piece[8][];
        for (int i = 0; i < 8; ++i) {
            chessboard[i] = new Piece[8];
        }
        whitePieces = new HashSet<>();
        blackPieces = new HashSet<>();
        String[] arr = fen.split(" ");
        String[] lines = arr[0].split("/");
        char[][] pieces = new char[8][];
        for (int i = 0; i < 8; ++i) {
            pieces[i] = lines[i].toCharArray();
        }
        for (int i = 0; i < 8; ++i) {
            int k = 0;
            for (int j = 0; j < 8; ++j, ++k) {
                if (Character.isDigit(pieces[i][k])) {
                    j += Character.getNumericValue(pieces[i][k]) - 1;
                } else {
                    switch (pieces[i][k]) {
                        case 'r':
                            chessboard[j][7 - i] = new Piece(Piece.Type.ROOK, Piece.Color.BLACK, j, 7 - i);
                            blackPieces.add(chessboard[j][7 - i]);
                            break;
                        case 'R':
                            chessboard[j][7 - i] = new Piece(Piece.Type.ROOK, Piece.Color.WHITE, j, 7 - i);
                            whitePieces.add(chessboard[j][7 - i]);
                            break;
                        case 'p':
                            chessboard[j][7 - i] = new Piece(Piece.Type.PAWN, Piece.Color.BLACK, j, 7 - i);
                            blackPieces.add(chessboard[j][7 - i]);
                            break;
                        case 'P':
                            chessboard[j][7 - i] = new Piece(Piece.Type.PAWN, Piece.Color.WHITE, j, 7 - i);
                            whitePieces.add(chessboard[j][7 - i]);
                            break;
                        case 'N':
                            chessboard[j][7 - i] = new Piece(Piece.Type.KNIGHT, Piece.Color.WHITE, j, 7 - i);
                            whitePieces.add(chessboard[j][7 - i]);
                            break;
                        case 'n':
                            chessboard[j][7 - i] = new Piece(Piece.Type.KNIGHT, Piece.Color.BLACK, j, 7 - i);
                            blackPieces.add(chessboard[j][7 - i]);
                            break;
                        case 'Q':
                            chessboard[j][7 - i] = new Piece(Piece.Type.QUEEN, Piece.Color.WHITE, j, 7 - i);
                            whitePieces.add(chessboard[j][7 - i]);
                            break;
                        case 'q':
                            chessboard[j][7 - i] = new Piece(Piece.Type.QUEEN, Piece.Color.BLACK, j, 7 - i);
                            blackPieces.add(chessboard[j][7 - i]);
                            break;
                        case 'K':
                            chessboard[j][7 - i] = new Piece(Piece.Type.KING, Piece.Color.WHITE, j, 7 - i);
                            whitePieces.add(chessboard[j][7 - i]);
                            break;
                        case 'k':
                            chessboard[j][7 - i] = new Piece(Piece.Type.KING, Piece.Color.BLACK, j, 7 - i);
                            blackPieces.add(chessboard[j][7 - i]);
                            break;
                        case 'b':
                            chessboard[j][7 - i] = new Piece(BISHOP, Piece.Color.BLACK, j, 7 - i);
                            blackPieces.add(chessboard[j][7 - i]);
                            break;
                        case 'B':
                            chessboard[j][7 - i] = new Piece(BISHOP, Piece.Color.WHITE, j, 7 - i);
                            whitePieces.add(chessboard[j][7 - i]);
                            break;
                    }
                }
            }
        }

        if (arr[1].charAt(0) == 'w') {
            turn = Piece.Color.WHITE;
        } else {
            turn = Piece.Color.BLACK;
        }

        char[] castles = arr[2].toCharArray();
        for (char c : castles) {
            switch (c) {
                case 'K':
                    isWhiteRightCastleAvailable = true;
                case 'k':
                    isBlackRightCastleAvailable = true;
                case 'Q':
                    isWhiteLeftCastleAvailable = true;
                case 'q':
                    isBlackLeftCastleAvailable = true;
                case '-':
                    isWhiteLeftCastleAvailable = false;
                    isWhiteRightCastleAvailable = false;
                    isBlackLeftCastleAvailable = false;
                    isBlackRightCastleAvailable = false;
            }
        }
        //
        //
        // do dokonczenia
        //
        //
    }

    private Piece move(Move move) {
        chessboard[move.getFromX()][move.getFromY()].setLocation(move.getToX(), move.getToY());
        Piece copy = chessboard[move.getToX()][move.getToY()];
        chessboard[move.getToX()][move.getToY()] = chessboard[move.getFromX()][move.getFromY()];
        chessboard[move.getFromX()][move.getFromY()] = null;
        if (copy != null) {
            if (copy.getColor() == Piece.Color.WHITE) {
                whitePieces.remove(copy);
            } else {
                blackPieces.remove(copy);
            }
        }
        chessboard[move.getFromX()][move.getFromY()] = null;
        return copy;
    }

    private void undoMove(Move move, Piece piece) {
        if (piece != null && piece.getColor() == Piece.Color.WHITE) {
            whitePieces.add(piece);
        } else {
            blackPieces.add(piece);
        }
        chessboard[move.getFromX()][move.getFromY()] = chessboard[move.getToX()][move.getToY()];
        chessboard[move.getFromX()][move.getFromY()].setLocation(move.getFromX(), move.getFromY());
        chessboard[move.getToX()][move.getToY()] = piece;
    }

    public void makeMove(Move move) {
        if (isMoveLegal(move)) {
            move(move);
            if (chessboard[move.getToX()][move.getToY()].getType() == Piece.Type.KING) {
                if (move.getFromX() == 4) {
                    if (chessboard[move.getToX()][move.getToY()].getColor() == Piece.Color.WHITE) {
                        if (move.getToX() == 2) {
                            move(new Move(0, 0, 3, 0));
                        } else {
                            move(new Move(7, 0, 5, 0));
                        }
                    } else {
                        if (move.getToX() == 2) {
                            move(new Move(0, 7, 3, 7));
                        } else {
                            move(new Move(7, 7, 5, 7));
                        }
                    }
                }
                if (chessboard[move.getToX()][move.getToY()].getColor() == Piece.Color.WHITE) {
                    isWhiteLeftCastleAvailable = false;
                    isWhiteRightCastleAvailable = false;
                } else {
                    isBlackLeftCastleAvailable = false;
                    isBlackRightCastleAvailable = false;
                }
            }
            lastMove = move;
            if (chessboard[move.getToX()][move.getToY()].getColor() == Piece.Color.WHITE) {
                turn = Piece.Color.BLACK;
            } else {
                turn = Piece.Color.WHITE;
            }
        } else if (chessboard[move.getToX()][move.getToY()].getType() == Piece.Type.ROOK) {
            if (chessboard[move.getToX()][move.getToY()].getColor() == Piece.Color.WHITE) {
                if (move.getFromX() == 0) {
                    isWhiteLeftCastleAvailable = false;
                } else if (move.getFromX() == 7) {
                    isWhiteRightCastleAvailable = false;
                }
            } else {
                if (move.getFromX() == 0) {
                    isBlackLeftCastleAvailable = false;
                } else if (move.getFromX() == 7) {
                    isBlackRightCastleAvailable = false;
                }
            }
        }
    }

    private boolean isMoveLegal(Move move) {
        Piece piece = chessboard[move.getFromX()][move.getFromY()];
        if (piece == null) {
            return false;
        } else {
            Piece tmp;
            boolean isCheck;
            int i = 0;
            switch (piece.getType()) {
                case PAWN:
                    if (piece.getColor() == Piece.Color.WHITE) {
                        if (move.getFromX() == move.getToX()) {
                            if (move.getFromY() + 1 == move.getToY()) {
                                if (chessboard[move.getToX()][move.getToY()] == null) {
                                    tmp = move(move);
                                    isCheck = isCheck(Piece.Color.WHITE);
                                    undoMove(move, tmp);
                                    return !isCheck;
                                }
                            } else if (move.getFromY() + 2 == move.getToY()) {
                                if (chessboard[move.getFromX()][move.getFromY() + 1] == null && chessboard[move.getFromX()][move.getFromY() + 2] == null) {
                                    tmp = move(move);
                                    isCheck = isCheck(Piece.Color.WHITE);
                                    undoMove(move, tmp);
                                    return !isCheck;
                                }
                            }
                        } else if (move.getFromY() + 1 == move.getToY()
                                && Math.abs(move.getFromX() - move.getToX()) == 1
                                && chessboard[move.getToX()][move.getToY()] == null
                                && chessboard[move.getToX()][move.getFromY()].getType() == Piece.Type.PAWN
                                && chessboard[move.getToX()][move.getFromY()].isOpposite(chessboard[move.getFromX()][move.getFromY()])) {
                            tmp = move(move);
                            isCheck = isCheck(Piece.Color.WHITE);
                            blackPieces.remove(chessboard[move.getToX()][move.getFromY()]);
                            chessboard[move.getToX()][move.getFromY()] = null;
                            undoMove(move, tmp);
                            return !isCheck;
                        } else if (move.getToY() - move.getFromY() == 1 && Math.abs(move.getFromX() - move.getToX()) == 1) {
                            if (chessboard[move.getToX()][move.getToY()] != null && chessboard[move.getToX()][move.getToY()].getColor() == Piece.Color.BLACK) {
                                tmp = move(move);
                                isCheck = isCheck(Piece.Color.WHITE);
                                undoMove(move, tmp);
                                return !isCheck;
                            }
                        }
                    } else {
                        if (move.getFromX() == move.getToX()) {
                            if (move.getFromY() - 1 == move.getToY()) {
                                if (chessboard[move.getToX()][move.getToY()] == null) {
                                    tmp = move(move);
                                    isCheck = isCheck(Piece.Color.BLACK);
                                    undoMove(move, tmp);
                                    return !isCheck;
                                }
                            } else if (move.getFromY() - 2 == move.getToY()) {
                                if (chessboard[move.getFromX()][move.getFromY() - 1] == null && chessboard[move.getFromX()][move.getFromY() - 2] == null) {
                                    tmp = move(move);
                                    isCheck = isCheck(Piece.Color.BLACK);
                                    undoMove(move, tmp);
                                    return !isCheck;
                                }
                            }
                        } else if (move.getFromY() - 1 == move.getToY()
                                && Math.abs(move.getFromX() - move.getToX()) == 1
                                && chessboard[move.getToX()][move.getToY()] == null
                                && chessboard[move.getToX()][move.getFromY()].getType() == Piece.Type.PAWN
                                && chessboard[move.getToX()][move.getFromY()].isOpposite(chessboard[move.getFromX()][move.getFromY()])) {
                            tmp = move(move);
                            isCheck = isCheck(Piece.Color.BLACK);
                            blackPieces.remove(chessboard[move.getToX()][move.getFromY()]);
                            chessboard[move.getToX()][move.getFromY()] = null;
                            undoMove(move, tmp);
                            return !isCheck;
                        } else if (move.getFromY() - move.getToY() == 1 && Math.abs(move.getFromX() - move.getToX()) == 1) {
                            if (chessboard[move.getToX()][move.getToY()] != null && chessboard[move.getToX()][move.getToY()].getColor() == Piece.Color.WHITE) {
                                tmp = move(move);
                                isCheck = isCheck(Piece.Color.BLACK);
                                undoMove(move, tmp);
                                return !isCheck;
                            }
                        }
                    }
                    break;
                case KNIGHT:
                    if (move.getFromX() + 2 == move.getToX() && move.getFromY() + 1 == move.getToY()) {
                        if (chessboard[move.getToX()][move.getToY()] == null || piece.isOpposite(chessboard[move.getToX()][move.getToY()])) {
                            tmp = move(move);
                            isCheck = isCheck(chessboard[move.getToX()][move.getToY()].getColor());
                            undoMove(move, tmp);
                            return !isCheck;
                        }
                    } else if (move.getFromX() + 2 == move.getToX() && move.getFromY() - 1 == move.getToY()) {
                        if (chessboard[move.getToX()][move.getToY()] == null || piece.isOpposite(chessboard[move.getToX()][move.getToY()])) {
                            tmp = move(move);
                            isCheck = isCheck(chessboard[move.getToX()][move.getToY()].getColor());
                            undoMove(move, tmp);
                            return !isCheck;
                        }
                    } else if (move.getFromX() - 2 == move.getToX() && move.getFromY() + 1 == move.getToY()) {
                        if (chessboard[move.getToX()][move.getToY()] == null || piece.isOpposite(chessboard[move.getToX()][move.getToY()])) {
                            tmp = move(move);
                            isCheck = isCheck(chessboard[move.getToX()][move.getToY()].getColor());
                            undoMove(move, tmp);
                            return !isCheck;
                        }
                    } else if (move.getFromX() - 2 == move.getToX() && move.getFromY() - 1 == move.getToY()) {
                        if (chessboard[move.getToX()][move.getToY()] == null || piece.isOpposite(chessboard[move.getToX()][move.getToY()])) {
                            tmp = move(move);
                            isCheck = isCheck(chessboard[move.getToX()][move.getToY()].getColor());
                            undoMove(move, tmp);
                            return !isCheck;
                        }
                    } else if (move.getFromX() + 1 == move.getToX() && move.getFromY() + 2 == move.getToY()) {
                        if (chessboard[move.getToX()][move.getToY()] == null || piece.isOpposite(chessboard[move.getToX()][move.getToY()])) {
                            tmp = move(move);
                            isCheck = isCheck(chessboard[move.getToX()][move.getToY()].getColor());
                            undoMove(move, tmp);
                            return !isCheck;
                        }
                    } else if (move.getFromX() + 1 == move.getToX() && move.getFromY() - 2 == move.getToY()) {
                        if (chessboard[move.getToX()][move.getToY()] == null || piece.isOpposite(chessboard[move.getToX()][move.getToY()])) {
                            tmp = move(move);
                            isCheck = isCheck(chessboard[move.getToX()][move.getToY()].getColor());
                            undoMove(move, tmp);
                            return !isCheck;
                        }
                    } else if (move.getFromX() - 1 == move.getToX() && move.getFromY() + 2 == move.getToY()) {
                        if (chessboard[move.getToX()][move.getToY()] == null || piece.isOpposite(chessboard[move.getToX()][move.getToY()])) {
                            tmp = move(move);
                            isCheck = isCheck(chessboard[move.getToX()][move.getToY()].getColor());
                            undoMove(move, tmp);
                            return !isCheck;
                        }
                    } else if (move.getFromX() - 1 == move.getToX() && move.getFromY() - 2 == move.getToY()) {
                        if (chessboard[move.getToX()][move.getToY()] == null || piece.isOpposite(chessboard[move.getToX()][move.getToY()])) {
                            tmp = move(move);
                            isCheck = isCheck(chessboard[move.getToX()][move.getToY()].getColor());
                            undoMove(move, tmp);
                            return !isCheck;
                        }
                    }
                    break;
                case BISHOP:
                    i = 1;
                    // left-up
                    if (move.getFromX() > move.getToX() && move.getToY() > move.getFromY()) {
                        while (chessboard[move.getFromX() - i][move.getFromY() + i] == null) {
                            ++i;
                        }
                        if ((move.getFromX() - i == move.getToX() && move.getFromY() + i == move.getToY())) {
                            if (chessboard[move.getFromX()][move.getFromY()].isOpposite(chessboard[move.getToX()][move.getToY()])) {
                                tmp = move(move);
                                isCheck = isCheck(chessboard[move.getToX()][move.getToY()].getColor());
                                undoMove(move, tmp);
                                return !isCheck;
                            } else {
                                return false;
                            }
                        }
                    }
                    //left-down
                    if (move.getFromX() > move.getToX() && move.getToY() < move.getFromY()) {
                        i = 1;
                        while (chessboard[move.getFromX() - i][move.getFromY() - i] == null) {
                            ++i;
                        }
                        if ((move.getFromX() - i == move.getToX() && move.getFromY() + i == move.getToY())) {
                            if (chessboard[move.getFromX()][move.getFromY()].isOpposite(chessboard[move.getToX()][move.getToY()])) {
                                tmp = move(move);
                                isCheck = isCheck(chessboard[move.getToX()][move.getToY()].getColor());
                                undoMove(move, tmp);
                                return !isCheck;
                            } else {
                                return false;
                            }
                        }
                    }
                    //right-up
                    if (move.getFromX() < move.getToX() && move.getToY() > move.getFromY()) {
                        i = 1;
                        while (chessboard[move.getFromX() + i][move.getFromY() + i] == null) {
                            ++i;
                        }
                        if ((move.getFromX() - i == move.getToX() && move.getFromY() + i == move.getToY())) {
                            if (chessboard[move.getFromX()][move.getFromY()].isOpposite(chessboard[move.getToX()][move.getToY()])) {
                                tmp = move(move);
                                isCheck = isCheck(chessboard[move.getToX()][move.getToY()].getColor());
                                undoMove(move, tmp);
                                return !isCheck;
                            } else {
                                return false;
                            }
                        }
                    }
                    //right-down
                    if (move.getFromX() < move.getToX() && move.getToY() < move.getFromY()) {
                        i = 1;
                        while (chessboard[move.getFromX() + i][move.getFromY() - i] == null) {
                            ++i;
                        }
                        if ((move.getFromX() - i == move.getToX() && move.getFromY() + i == move.getToY())) {
                            if (chessboard[move.getFromX()][move.getFromY()].isOpposite(chessboard[move.getToX()][move.getToY()])) {
                                tmp = move(move);
                                isCheck = isCheck(chessboard[move.getToX()][move.getToY()].getColor());
                                undoMove(move, tmp);
                                return !isCheck;
                            } else {
                                return false;
                            }
                        }
                    }
                    break;
                case ROOK:
                    //left
                    if (move.getToX() < move.getFromX() && move.getFromY() == move.getToY()) {
                        i = 1;
                        while (chessboard[move.getFromX() - i][move.getFromY()] == null) {
                            ++i;
                        }
                        if (move.getToX() + i == move.getFromX()) {
                            if (chessboard[move.getToX()][move.getToY()].isOpposite(chessboard[move.getFromX()][move.getFromY()])) {
                                tmp = move(move);
                                isCheck = isCheck(chessboard[move.getToX()][move.getToY()].getColor());
                                undoMove(move, tmp);
                                return !isCheck;
                            } else {
                                return false;
                            }
                        }
                    }
                    //right
                    if (move.getToX() > move.getFromX() && move.getFromY() == move.getToY()) {
                        i = 1;
                        while (chessboard[move.getFromX() + i][move.getFromY()] == null) {
                            ++i;
                        }
                        if (move.getToX() - i == move.getFromX()) {
                            if (chessboard[move.getToX()][move.getToY()].isOpposite(chessboard[move.getFromX()][move.getFromY()])) {
                                tmp = move(move);
                                isCheck = isCheck(chessboard[move.getToX()][move.getToY()].getColor());
                                undoMove(move, tmp);
                                return !isCheck;
                            } else {
                                return false;
                            }
                        }
                    }
                    //up
                    if (move.getToY() > move.getFromY() && move.getFromX() == move.getToX()) {
                        i = 1;
                        while (chessboard[move.getFromX()][move.getFromY() + i] == null) {
                            ++i;
                        }
                        if (move.getFromY() + i == move.getToY()) {
                            if (chessboard[move.getToX()][move.getToY()].isOpposite(chessboard[move.getFromX()][move.getFromY()])) {
                                tmp = move(move);
                                isCheck = isCheck(chessboard[move.getToX()][move.getToY()].getColor());
                                undoMove(move, tmp);
                                return !isCheck;
                            } else {
                                return false;
                            }
                        }
                    }
                    //down
                    if (move.getToY() < move.getFromY() && move.getFromX() == move.getToX()) {
                        i = 1;
                        while (chessboard[move.getFromX()][move.getFromY() - i] == null) {
                            ++i;
                        }
                        if (move.getToY() + i == move.getFromY()) {
                            if (chessboard[move.getToX()][move.getToY()].isOpposite(chessboard[move.getFromX()][move.getFromY()])) {
                                tmp = move(move);
                                isCheck = isCheck(chessboard[move.getToX()][move.getToY()].getColor());
                                undoMove(move, tmp);
                                return !isCheck;
                            } else {
                                return false;
                            }
                        }
                    }
                    break;
                case QUEEN:
                    //left
                    if (move.getToX() < move.getFromX() && move.getFromY() == move.getToY()) {
                        i = 1;
                        while (chessboard[move.getFromX() - i][move.getFromY()] == null) {
                            ++i;
                        }
                        if (move.getToX() + i == move.getFromX()) {
                            if (chessboard[move.getToX()][move.getToY()].isOpposite(chessboard[move.getFromX()][move.getFromY()])) {
                                tmp = move(move);
                                isCheck = isCheck(chessboard[move.getToX()][move.getToY()].getColor());
                                undoMove(move, tmp);
                                return !isCheck;
                            } else {
                                return false;
                            }
                        }
                    }
                    //right
                    if (move.getToX() > move.getFromX() && move.getFromY() == move.getToY()) {
                        i = 1;
                        while (chessboard[move.getFromX() + i][move.getFromY()] == null) {
                            ++i;
                        }
                        if (move.getToX() - i == move.getFromX()) {
                            if (chessboard[move.getToX()][move.getToY()].isOpposite(chessboard[move.getFromX()][move.getFromY()])) {
                                tmp = move(move);
                                isCheck = isCheck(chessboard[move.getToX()][move.getToY()].getColor());
                                undoMove(move, tmp);
                                return !isCheck;
                            } else {
                                return false;
                            }
                        }
                    }
                    //up
                    if (move.getToY() > move.getFromY() && move.getFromX() == move.getToX()) {
                        i = 1;
                        while (chessboard[move.getFromX()][move.getFromY() + i] == null) {
                            ++i;
                        }
                        if (move.getFromY() + i == move.getToY()) {
                            if (chessboard[move.getToX()][move.getToY()].isOpposite(chessboard[move.getFromX()][move.getFromY()])) {
                                tmp = move(move);
                                isCheck = isCheck(chessboard[move.getToX()][move.getToY()].getColor());
                                undoMove(move, tmp);
                                return !isCheck;
                            } else {
                                return false;
                            }
                        }
                    }
                    //down
                    if (move.getToY() < move.getFromY() && move.getFromX() == move.getToX()) {
                        i = 1;
                        while (chessboard[move.getFromX()][move.getFromY() - i] == null) {
                            ++i;
                        }
                        if (move.getToY() + i == move.getFromY()) {
                            if (chessboard[move.getToX()][move.getToY()].isOpposite(chessboard[move.getFromX()][move.getFromY()])) {
                                tmp = move(move);
                                isCheck = isCheck(chessboard[move.getToX()][move.getToY()].getColor());
                                undoMove(move, tmp);
                                return !isCheck;
                            } else {
                                return false;
                            }
                        }
                    }
                    i = 1;
                    // left-up
                    if (move.getFromX() > move.getToX() && move.getToY() > move.getFromY()) {
                        while (chessboard[move.getFromX() - i][move.getFromY() + i] == null) {
                            ++i;
                        }
                        if ((move.getFromX() - i == move.getToX() && move.getFromY() + i == move.getToY())) {
                            if (chessboard[move.getFromX()][move.getFromY()].isOpposite(chessboard[move.getToX()][move.getToY()])) {
                                tmp = move(move);
                                isCheck = isCheck(chessboard[move.getToX()][move.getToY()].getColor());
                                undoMove(move, tmp);
                                return !isCheck;
                            } else {
                                return false;
                            }
                        }
                    }
                    //left-down
                    if (move.getFromX() > move.getToX() && move.getToY() < move.getFromY()) {
                        i = 1;
                        while (chessboard[move.getFromX() - i][move.getFromY() - i] == null) {
                            ++i;
                        }
                        if ((move.getFromX() - i == move.getToX() && move.getFromY() + i == move.getToY())) {
                            if (chessboard[move.getFromX()][move.getFromY()].isOpposite(chessboard[move.getToX()][move.getToY()])) {
                                tmp = move(move);
                                isCheck = isCheck(chessboard[move.getToX()][move.getToY()].getColor());
                                undoMove(move, tmp);
                                return !isCheck;
                            } else {
                                return false;
                            }
                        }
                    }
                    //right-up
                    if (move.getFromX() < move.getToX() && move.getToY() > move.getFromY()) {
                        i = 1;
                        while (chessboard[move.getFromX() + i][move.getFromY() + i] == null) {
                            ++i;
                        }
                        if ((move.getFromX() - i == move.getToX() && move.getFromY() + i == move.getToY())) {
                            if (chessboard[move.getFromX()][move.getFromY()].isOpposite(chessboard[move.getToX()][move.getToY()])) {
                                tmp = move(move);
                                isCheck = isCheck(chessboard[move.getToX()][move.getToY()].getColor());
                                undoMove(move, tmp);
                                return !isCheck;
                            } else {
                                return false;
                            }
                        }
                    }
                    //right-down
                    if (move.getFromX() < move.getToX() && move.getToY() < move.getFromY()) {
                        i = 1;
                        while (chessboard[move.getFromX() + i][move.getFromY() - i] == null) {
                            ++i;
                        }
                        if ((move.getFromX() - i == move.getToX() && move.getFromY() + i == move.getToY())) {
                            if (chessboard[move.getFromX()][move.getFromY()].isOpposite(chessboard[move.getToX()][move.getToY()])) {
                                tmp = move(move);
                                isCheck = isCheck(chessboard[move.getToX()][move.getToY()].getColor());
                                undoMove(move, tmp);
                                return !isCheck;
                            } else {
                                return false;
                            }
                        }
                    }
                    break;
                case KING:
                    if (Math.abs(move.getFromX() - move.getToX()) == 1 || Math.abs(move.getFromX() - move.getToX()) == 0) {
                        if (Math.abs(move.getFromY() - move.getToY()) == 1 || Math.abs(move.getFromY() - move.getToY()) == 0) {
                            if (chessboard[move.getFromX()][move.getFromY()].isOpposite(chessboard[move.getToX()][move.getToY()])) {
                                tmp = move(move);
                                isCheck = isCheck(chessboard[move.getToX()][move.getToY()].getColor());
                                undoMove(move, tmp);
                                return !isCheck;
                            } else {
                                return false;
                            }
                        }
                    } else if (piece == whiteKing && !isCheck(Piece.Color.WHITE) && isWhiteRightCastleAvailable && move.getToX() == 6 && move.getToY() == 0 && chessboard[5][0] == null && chessboard[6][0] == null) {
                        if (chessboard[move.getFromX()][move.getFromY()].isOpposite(chessboard[move.getToX()][move.getToY()])) {
                            tmp = move(move);
                            isCheck = isCheck(chessboard[move.getToX()][move.getToY()].getColor());
                            undoMove(move, tmp);
                            return !isCheck;
                        } else {
                            return false;
                        }
                    } else if (piece == blackKing && !isCheck(Piece.Color.BLACK) && isBlackRightCastleAvailable && move.getToX() == 6 && move.getToY() == 0 && chessboard[5][7] == null && chessboard[6][7] == null) {
                        if (chessboard[move.getFromX()][move.getFromY()].isOpposite(chessboard[move.getToX()][move.getToY()])) {
                            tmp = move(move);
                            isCheck = isCheck(chessboard[move.getToX()][move.getToY()].getColor());
                            undoMove(move, tmp);
                            return !isCheck;
                        } else {
                            return false;
                        }
                    } else if (piece == whiteKing && !isCheck(Piece.Color.WHITE) && isWhiteLeftCastleAvailable && move.getToX() == 2 && move.getToY() == 0 && chessboard[1][0] == null && chessboard[2][0] == null && chessboard[3][0] == null) {
                        if (chessboard[move.getFromX()][move.getFromY()].isOpposite(chessboard[move.getToX()][move.getToY()])) {
                            tmp = move(move);
                            isCheck = isCheck(chessboard[move.getToX()][move.getToY()].getColor());
                            undoMove(move, tmp);
                            return !isCheck;
                        } else {
                            return false;
                        }
                    } else if (piece == blackKing && !isCheck(Piece.Color.BLACK) && isBlackLeftCastleAvailable && move.getToX() == 2 && move.getToY() == 0 && chessboard[1][7] == null && chessboard[2][7] == null && chessboard[3][7] == null) {
                        if (chessboard[move.getFromX()][move.getFromY()].isOpposite(chessboard[move.getToX()][move.getToY()])) {
                            tmp = move(move);
                            isCheck = isCheck(chessboard[move.getToX()][move.getToY()].getColor());
                            undoMove(move, tmp);
                            return !isCheck;
                        } else {
                            return false;
                        }
                    }
                    break;
            }
        }
        return false;
    }

    // is check on /color/
    public boolean isCheck(Piece.Color color) {
        if (color == Piece.Color.WHITE) {
            for (Piece piece : blackPieces) {
                switch (piece.getType()) {
                    case PAWN:
                        if (piece.getX() > 0 && piece.getX() < 7) {
                            if (whiteKing.getY() == piece.getY() - 1 && Math.abs(whiteKing.getX() - piece.getX()) == 1) {
                                return true;
                            } else if (piece.getX() == 0) {
                                if (whiteKing.getY() == piece.getY() - 1 && whiteKing.getX() == 1) {
                                    return true;
                                }
                            } else if (piece.getX() == 7) {
                                if (whiteKing.getY() == piece.getY() - 1 && whiteKing.getX() == 6) {
                                    return true;
                                }
                            }
                        }
                        break;
                    case ROOK:
                        //left
                        int i = 1;
                        if (whiteKing.getY() == piece.getY() && whiteKing.getX() < piece.getX()) {
                            while (piece.getX() - i >= 0 && chessboard[piece.getX() - i][piece.getY()] == null) {
                                ++i;
                            }
                            if (whiteKing.getX() + i == piece.getX()) {
                                return true;
                            }
                        }
                        //right
                        if (whiteKing.getY() == piece.getY() && whiteKing.getX() > piece.getX()) {
                            i = 1;
                            while (piece.getX() + i < 8 && chessboard[piece.getX() + i][piece.getY()] == null) {
                                ++i;
                            }
                            if (whiteKing.getX() - i == piece.getX()) {
                                return true;
                            }
                        }
                        //up
                        if (whiteKing.getX() == piece.getX() && whiteKing.getY() > piece.getY()) {
                            i = 1;
                            while (piece.getY() + i < 8 && chessboard[piece.getX()][piece.getY() + i] == null) {
                                ++i;
                            }
                            if (piece.getY() + i == whiteKing.getY()) {
                                return true;
                            }
                        }
                        //down
                        if (whiteKing.getX() == piece.getX() && whiteKing.getY() < piece.getY()) {
                            i = 1;
                            while (piece.getY() - i >= 0 && chessboard[piece.getX()][piece.getY() - i] == null) {
                                ++i;
                            }
                            if (whiteKing.getY() + i == piece.getY()) {
                                return true;
                            }
                        }
                        break;
                    case KNIGHT:
                        if (piece.getX() - 2 == whiteKing.getX() && piece.getY() - 1 == whiteKing.getY()) {
                            return true;
                        }
                        if (piece.getX() - 2 == whiteKing.getX() && piece.getY() + 1 == whiteKing.getY()) {
                            return true;
                        }
                        if (piece.getX() + 2 == whiteKing.getX() && piece.getY() + 1 == whiteKing.getY()) {
                            return true;
                        }
                        if (piece.getX() + 2 == whiteKing.getX() && piece.getY() - 1 == whiteKing.getY()) {
                            return true;
                        }
                        if (piece.getY() - 2 == whiteKing.getY() && piece.getX() - 1 == whiteKing.getX()) {
                            return true;
                        }
                        if (piece.getY() - 2 == whiteKing.getY() && piece.getX() + 1 == whiteKing.getX()) {
                            return true;
                        }
                        if (piece.getY() + 2 == whiteKing.getY() && piece.getX() + 1 == whiteKing.getX()) {
                            return true;
                        }
                        if (piece.getY() + 2 == whiteKing.getY() && piece.getX() - 1 == whiteKing.getX()) {
                            return true;
                        }
                        break;
                    case BISHOP:
                        //left-up
                        i = 1;
                        if (whiteKing.getX() < piece.getX() && whiteKing.getY() > piece.getY()) {
                            while (piece.getX() - i >= 0 && piece.getY() + i < 8 && chessboard[piece.getX() - i][piece.getY() + i] == null) {
                                ++i;
                            }
                            if (whiteKing.getX() + i == piece.getX() && whiteKing.getY() - i == piece.getY()) {
                                return true;
                            }
                        }
                        //left-down
                        if (whiteKing.getX() < piece.getX() && whiteKing.getY() < piece.getY()) {
                            i = 1;
                            while (piece.getX() - i >= 0 && piece.getY() - i >= 0 && chessboard[piece.getX() - i][piece.getY() - i] == null) {
                                ++i;
                            }
                            if (whiteKing.getX() + i == piece.getX() && whiteKing.getY() + i == piece.getY()) {
                                return true;
                            }
                        }
                        //right-up
                        if (whiteKing.getX() > piece.getX() && whiteKing.getY() > piece.getY()) {
                            i = 1;
                            while (piece.getX() + i < 8 && piece.getY() + i < 8 && chessboard[piece.getX() + i][piece.getY() + i] == null) {
                                ++i;
                            }
                            if (whiteKing.getX() - i == piece.getX() && whiteKing.getY() - i == piece.getY()) {
                                return true;
                            }
                        }
                        //right-down
                        if (whiteKing.getX() > piece.getX() && whiteKing.getY() < piece.getY()) {
                            i = 1;
                            while (piece.getX() + i < 8 && piece.getY() - i >= 0 && chessboard[piece.getX() + i][piece.getY() - i] == null) {
                                ++i;
                            }
                            if (whiteKing.getX() - i == piece.getX() && whiteKing.getY() + i == piece.getY()) {
                                return true;
                            }
                        }
                        break;
                    case QUEEN:
                        //left-up
                        i = 1;
                        if (whiteKing.getX() < piece.getX() && whiteKing.getY() > piece.getY()) {
                            while (piece.getX() - i >= 0 && piece.getY() + i < 8 && chessboard[piece.getX() - i][piece.getY() + i] == null) {
                                ++i;
                            }
                            if (whiteKing.getX() + i == piece.getX() && whiteKing.getY() - i == piece.getY()) {
                                return true;
                            }
                        }
                        //left-down
                        if (whiteKing.getX() < piece.getX() && whiteKing.getY() < piece.getY()) {
                            i = 1;
                            while (piece.getX() - i >= 0 && piece.getY() - i >= 0 && chessboard[piece.getX() - i][piece.getY() - i] == null) {
                                ++i;
                            }
                            if (whiteKing.getX() + i == piece.getX() && whiteKing.getY() + i == piece.getY()) {
                                return true;
                            }
                        }
                        //right-up
                        if (whiteKing.getX() > piece.getX() && whiteKing.getY() > piece.getY()) {
                            i = 1;
                            while (piece.getX() + i < 8 && piece.getY() + i < 8 && chessboard[piece.getX() + i][piece.getY() + i] == null) {
                                ++i;
                            }
                            if (whiteKing.getX() - i == piece.getX() && whiteKing.getY() - i == piece.getY()) {
                                return true;
                            }
                        }
                        //right-down
                        if (whiteKing.getX() > piece.getX() && whiteKing.getY() < piece.getY()) {
                            i = 1;
                            while (piece.getX() + i < 8 && piece.getY() - i >= 0 && chessboard[piece.getX() + i][piece.getY() - i] == null) {
                                ++i;
                            }
                            if (whiteKing.getX() == piece.getX() + i && whiteKing.getY() + i == piece.getY()) {
                                return true;
                            }
                        }
                        //left
                        i = 1;
                        if (whiteKing.getY() == piece.getY() && whiteKing.getX() < piece.getX()) {
                            while (piece.getX() - i >= 0 && chessboard[piece.getX() - i][piece.getY()] == null) {
                                ++i;
                            }
                            if (whiteKing.getX() + i == piece.getX()) {
                                return true;
                            }
                        }
                        //right
                        if (whiteKing.getY() == piece.getY() && whiteKing.getX() > piece.getX()) {
                            i = 1;
                            while (piece.getX() + i < 8 && chessboard[piece.getX() + i][piece.getY()] == null) {
                                ++i;
                            }
                            if (whiteKing.getX() - i == piece.getX()) {
                                return true;
                            }
                        }
                        //up
                        if (whiteKing.getX() == piece.getX() && whiteKing.getY() > piece.getY()) {
                            i = 1;
                            while (piece.getY() + i < 8 && chessboard[piece.getX()][piece.getY() + i] == null) {
                                ++i;
                            }
                            if (piece.getY() + i == whiteKing.getY()) {
                                return true;
                            }
                        }
                        //down
                        if (whiteKing.getX() == piece.getX() && whiteKing.getY() < piece.getY()) {
                            i = 1;
                            while (piece.getY() - i >= 0 && chessboard[piece.getX()][piece.getY() - i] == null) {
                                ++i;
                            }
                            if (whiteKing.getY() + i == piece.getY()) {
                                return true;
                            }
                        }
                        break;
                }
            }
        } else {
            for (Piece piece : whitePieces) {
                switch (piece.getType()) {
                    case PAWN:
                        if (piece.getX() > 0 && piece.getX() < 7) {
                            if (blackKing.getY() == piece.getY() + 1 && Math.abs(blackKing.getX() - piece.getX()) == 1) {
                                return true;
                            } else if (piece.getX() == 0) {
                                if (blackKing.getY() == piece.getY() + 1 && blackKing.getX() == 1) {
                                    return true;
                                }
                            } else if (piece.getX() == 7) {
                                if (blackKing.getY() == piece.getY() + 1 && blackKing.getX() == 6) {
                                    return true;
                                }
                            }
                        }
                        break;
                    case ROOK:
                        //left
                        int i = 1;
                        if (blackKing.getY() == piece.getY() && blackKing.getX() < piece.getX()) {
                            while (chessboard[piece.getX() - i][piece.getY()] == null) {
                                ++i;
                            }
                            if (blackKing.getX() + i == piece.getX()) {
                                return true;
                            }
                        }
                        //right
                        if (blackKing.getY() == piece.getY() && blackKing.getX() > piece.getX()) {
                            i = 1;
                            while (chessboard[piece.getX() + i][piece.getY()] == null) {
                                ++i;
                            }
                            if (blackKing.getX() - i == piece.getX()) {
                                return true;
                            }
                        }
                        //up
                        if (blackKing.getX() == piece.getX() && blackKing.getY() > piece.getY()) {
                            i = 1;
                            while (chessboard[piece.getX()][piece.getY() + i] == null) {
                                ++i;
                            }
                            if (piece.getY() + i == blackKing.getY()) {
                                return true;
                            }
                        }
                        //down
                        if (blackKing.getX() == piece.getX() && blackKing.getY() < piece.getY()) {
                            i = 1;
                            while (chessboard[piece.getX()][piece.getY() - i] == null) {
                                ++i;
                            }
                            if (blackKing.getY() + i == piece.getY()) {
                                return true;
                            }
                        }
                        break;
                    case KNIGHT:
                        if (piece.getX() - 2 == blackKing.getX() && piece.getY() - 1 == blackKing.getY()) {
                            return true;
                        }
                        if (piece.getX() - 2 == blackKing.getX() && piece.getY() + 1 == blackKing.getY()) {
                            return true;
                        }
                        if (piece.getX() + 2 == blackKing.getX() && piece.getY() + 1 == blackKing.getY()) {
                            return true;
                        }
                        if (piece.getX() + 2 == blackKing.getX() && piece.getY() - 1 == blackKing.getY()) {
                            return true;
                        }
                        if (piece.getY() - 2 == blackKing.getY() && piece.getX() - 1 == blackKing.getX()) {
                            return true;
                        }
                        if (piece.getY() - 2 == blackKing.getY() && piece.getX() + 1 == blackKing.getX()) {
                            return true;
                        }
                        if (piece.getY() + 2 == blackKing.getY() && piece.getX() + 1 == blackKing.getX()) {
                            return true;
                        }
                        if (piece.getY() + 2 == blackKing.getY() && piece.getX() - 1 == blackKing.getX()) {
                            return true;
                        }
                        break;
                    case BISHOP:
                        //left-up
                        i = 1;
                        if (blackKing.getX() < piece.getX() && blackKing.getY() > piece.getY()) {
                            while (chessboard[piece.getX() - i][piece.getY() + i] == null) {
                                ++i;
                            }
                            if (blackKing.getX() + i == piece.getX() && blackKing.getY() - i - 1 == piece.getY()) {
                                return true;
                            }
                        }
                        //left-down
                        if (blackKing.getX() < piece.getX() && blackKing.getY() < piece.getY()) {
                            i = 1;
                            while (chessboard[piece.getX() - i][piece.getY() - i] == null) {
                                ++i;
                            }
                            if (blackKing.getX() + i == piece.getX() && blackKing.getY() + i + 1 == piece.getY()) {
                                return true;
                            }
                        }
                        //right-up
                        if (blackKing.getX() > piece.getX() && blackKing.getY() > piece.getY()) {
                            i = 1;
                            while (chessboard[piece.getX() + i][piece.getY() + i] == null) {
                                ++i;
                            }
                            if (blackKing.getX() - i == piece.getX() && blackKing.getY() - i - 1 == piece.getY()) {
                                return true;
                            }
                        }
                        //right-down
                        if (blackKing.getX() > piece.getX() && blackKing.getY() < piece.getY()) {
                            i = 1;
                            while (chessboard[piece.getX() + i][piece.getY() - i] == null) {
                                ++i;
                            }
                            if (blackKing.getX() - i == piece.getX() && blackKing.getY() + i + 1 == piece.getY()) {
                                return true;
                            }
                        }
                        break;
                    case QUEEN:
                        i = 1;
                        if (blackKing.getX() < piece.getX() && blackKing.getY() > piece.getY()) {
                            while (chessboard[piece.getX() - i][piece.getY() + i] == null) {
                                ++i;
                            }
                            if (blackKing.getX() + i == piece.getX() && blackKing.getY() - i - 1 == piece.getY()) {
                                return true;
                            }
                        }
                        //left-down
                        if (blackKing.getX() < piece.getX() && blackKing.getY() < piece.getY()) {
                            i = 1;
                            while (chessboard[piece.getX() - i][piece.getY() - i] == null) {
                                ++i;
                            }
                            if (blackKing.getX() + i == piece.getX() && blackKing.getY() + i + 1 == piece.getY()) {
                                return true;
                            }
                        }
                        //right-up
                        if (blackKing.getX() > piece.getX() && blackKing.getY() > piece.getY()) {
                            i = 1;
                            while (chessboard[piece.getX() + i][piece.getY() + i] == null) {
                                ++i;
                            }
                            if (blackKing.getX() - i == piece.getX() && blackKing.getY() - i - 1 == piece.getY()) {
                                return true;
                            }
                        }
                        //right-down
                        if (blackKing.getX() > piece.getX() && blackKing.getY() < piece.getY()) {
                            i = 1;
                            while (chessboard[piece.getX() + i][piece.getY() - i] == null) {
                                ++i;
                            }
                            if (blackKing.getX() - i == piece.getX() && blackKing.getY() + i + 1 == piece.getY()) {
                                return true;
                            }
                        }
                        i = 1;
                        if (blackKing.getY() == piece.getY() && blackKing.getX() < piece.getX()) {
                            while (chessboard[piece.getX() - i][piece.getY()] == null) {
                                ++i;
                            }
                            if (blackKing.getX() + i == piece.getX()) {
                                return true;
                            }
                        }
                        //right
                        if (blackKing.getY() == piece.getY() && blackKing.getX() > piece.getX()) {
                            i = 1;
                            while (chessboard[piece.getX() + i][piece.getY()] == null) {
                                ++i;
                            }
                            if (blackKing.getX() - i == piece.getX()) {
                                return true;
                            }
                        }
                        //up
                        if (blackKing.getX() == piece.getX() && blackKing.getY() > piece.getY()) {
                            i = 1;
                            while (chessboard[piece.getX()][piece.getY() + i] == null) {
                                ++i;
                            }
                            if (piece.getY() + i == blackKing.getY()) {
                                return true;
                            }
                        }
                        //down
                        if (blackKing.getX() == piece.getX() && blackKing.getY() < piece.getY()) {
                            i = 1;
                            while (chessboard[piece.getX()][piece.getY() - i] == null) {
                                ++i;
                            }
                            if (blackKing.getY() + i == piece.getY()) {
                                return true;
                            }
                        }
                        break;
                }
            }
        }
        return false;
    }

    public void setLastMove(Move move) {
        lastMove = move;
    }
}