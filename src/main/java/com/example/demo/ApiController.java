package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.lang.constant.Constable;
import java.util.Hashtable;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/api")
@SessionAttributes("player")
public class ApiController {
    private GameSession gameSession;
    private Hashtable<Long, GameSession> gameSessions;

    @ModelAttribute("player")
    public Player player() {
        return new Player();
    }

    @Autowired
    public ApiController(GameSession gameSession, Hashtable<Long, GameSession> gameSessions) {
        this.gameSession = gameSession;
        this.gameSessions = gameSessions;
    }


    // zwraca obiekt gracza
    @GetMapping("/player")
    @ResponseBody
    public Player home(@ModelAttribute("player") Player player) {
        return player;
    }

    // szuka gierki
    @GetMapping("/findGame")
    @ResponseBody
    public synchronized ResponseEntity<HttpStatus> findGame(@ModelAttribute("player") Player player) {
        if (gameSession.getPlayerWhite() == null) {
            gameSession.setPlayerWhiteReady(true);
            player.setGameSessionId(gameSession.getSessionId());
            player.setSide("WHITE");
            gameSession.setPlayerWhite(player);
            gameSession.setPlayersTurn(player.getId());
            return new ResponseEntity<>(HttpStatus.OK);
        } else if (gameSession.getPlayerWhite() != null && gameSession.getPlayerBlack() == null) {
            gameSession.setPlayerBlackReady(true);
            gameSession.setPlayerBlack(player);
            player.setGameSessionId(gameSession.getSessionId());
            player.setSide("BLACK");
            return new ResponseEntity<>(HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    // sprawdza czy gra jest gotowa
    @GetMapping("/isGameReady")
    @ResponseBody
    public ResponseEntity<HttpStatus> isGameReady() { ;
        if (gameSession.isPlayerWhiteReady() && gameSession.isPlayerBlackReady()) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }
    }

    // zwraca GameSession
    @GetMapping("/game")
    @ResponseBody
    public GameSession game() {
        return gameSession;
    }


    // ruszanie figur
    @GetMapping("move/{fromX}/{fromY}/{toX}/{toY}")
    @ResponseBody
    public synchronized HttpEntity<? extends Constable> movePiece(@PathVariable("fromX") int x,
                                                                  @PathVariable("fromY") int y,
                                                                  @PathVariable("toX") int row,
                                                                  @PathVariable("toY") int column,
                                                                  @ModelAttribute("player") Player player) {
        if (!gameSession.isCheckMate()) {
            Piece p = gameSession.getPiece(x, y);
            boolean canMove;
            if ((x == row && y == column)
                    && p.getColor() != null
                    || p.getColor() != Piece.Color.valueOf(player.getSide())
                    || !p.isAlive()
                    || gameSession.getPlayerBlack() == null
                    || gameSession.getPlayerWhite() == null
                    || !gameSession.getPlayersTurn().equals(player.getId())) {
                return new ResponseEntity<>(HttpStatus.ACCEPTED);
            }

            canMove = !moveAndUndo(p, row, column);

            if (canMove && gameSession.getPiece(row, column).isAlive()) {
                gameSession.setPieceDead(row, column);
                p.setLocation(row, column);
                gameSession.setPlayersTurn(
                        gameSession.getPlayerWhite().getId().equals(gameSession.getPlayersTurn()) ? gameSession.getPlayerBlack().getId() : gameSession.getPlayerWhite().getId()
                );
                boolean isCheckOnWhite = isCheck(Piece.Color.WHITE);
                boolean isCheckOnBlack = isCheck(Piece.Color.BLACK);
                if (isCheckOnWhite && isMate(Piece.Color.WHITE)) {
                    gameSession.setCheckMate(true);

                } else if (isCheckOnBlack && isMate(Piece.Color.BLACK)) {
                    gameSession.setCheckMate(true);
                }
                gameSession.setCheckOnBlack(isCheckOnBlack);
                gameSession.setCheckOnWhite(isCheckOnWhite);
                if (Piece.Color.valueOf(player.getSide()) == Piece.Color.WHITE) {
                    gameSession.setPlayersTurn(gameSession.getPlayerBlack().getId());
                } else {
                    gameSession.setPlayersTurn(gameSession.getPlayerWhite().getId());
                }
                return new ResponseEntity<>(HttpStatus.OK);
            } else if (canMove) {
                p.setLocation(row, column);
                gameSession.setPlayersTurn(
                        gameSession.getPlayerWhite().getId().equals(gameSession.getPlayersTurn()) ? gameSession.getPlayerBlack().getId() : gameSession.getPlayerWhite().getId()
                );
                boolean isCheckOnWhite = isCheck(Piece.Color.WHITE);
                boolean isCheckOnBlack = isCheck(Piece.Color.BLACK);
                if (isCheckOnWhite && isMate(Piece.Color.WHITE)) {
                    gameSession.setCheckMate(true);

                } else if (isCheckOnBlack && isMate(Piece.Color.BLACK)) {
                    gameSession.setCheckMate(true);
                }
                gameSession.setCheckOnBlack(isCheckOnBlack);
                gameSession.setCheckOnWhite(isCheckOnWhite);
                if (Piece.Color.valueOf(player.getSide()) == Piece.Color.WHITE) {
                    gameSession.setPlayersTurn(gameSession.getPlayerBlack().getId());
                } else {
                    gameSession.setPlayersTurn(gameSession.getPlayerWhite().getId());
                }
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.ACCEPTED);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }
    }

    private boolean canMoveAndIsNotCheck(Piece piece, int x, int y) {
        if (piece.getX() == -1 || piece.getY() == -1 || x < 0 || x > 7 || y < 0 || y > 7) {
            return false;
        }
        Piece copy = gameSession.getPiece(x, y);
        if (copy.getType() == Piece.Type.KING) {
            return false;
        }
        int preX = piece.getX();
        int preY = piece.getY();

        boolean canMove = gameSession.move(piece, x, y);
        gameSession.setPieceDead(x, y);
        if (canMove) {
            piece.setLocation(x, y);
        }

        if (canMove && !isCheck(piece.getColor())) {
            gameSession.getPiece(x, y).setLocation(preX, preY);
            if (copy.getColor() != null) {
                gameSession.addPiece(copy);
            }
            return true;
        } else {
            gameSession.getPiece(x, y).setLocation(preX, preY);
            if (copy.getColor() != null) {
                gameSession.addPiece(copy);
            }
            return false;
        }
    }

    // zwraca true takze jesli krol nie moze sie ruszyc wiec sprawdzic przez ism
    private synchronized boolean isMate(Piece.Color color) {
        for (Piece piece : gameSession.getPieces().stream().filter(e -> e.getColor() == color).collect(Collectors.toList())) {
            switch (piece.getType()) {
                case PAWN:
                    switch (piece.getColor()) {
                        case WHITE:
                            if (canMoveAndIsNotCheck(piece, piece.getX() - 1, piece.getY())
                                    || canMoveAndIsNotCheck(piece, piece.getX() - 2, piece.getY())
                                    || canMoveAndIsNotCheck(piece, piece.getX() - 1, piece.getY() - 1)
                                    || canMoveAndIsNotCheck(piece, piece.getX() - 1, piece.getY() + 1)) {
                                return false;
                            }
                        case BLACK:
                            if (canMoveAndIsNotCheck(piece, piece.getX() + 1, piece.getY())
                                    || canMoveAndIsNotCheck(piece, piece.getX() + 2, piece.getY())
                                    || canMoveAndIsNotCheck(piece, piece.getX() + 1, piece.getY() - 1)
                                    || canMoveAndIsNotCheck(piece, piece.getX() + 1, piece.getY() + 1)) {
                                return false;
                            }
                    }
                    break;
                case ROOK:
                    for (int i = 0; i < 8; ++i) {
                        if (canMoveAndIsNotCheck(piece, piece.getX() + i, piece.getY())
                                || canMoveAndIsNotCheck(piece, piece.getX() - i, piece.getY())
                                || canMoveAndIsNotCheck(piece, piece.getX(), piece.getY() + i)
                                || canMoveAndIsNotCheck(piece, piece.getX(), piece.getY() - i)) {
                            return false;
                        }
                    }
                    break;
                case BISHOP:
                    for (int i = 0; i < 8; ++i) {
                        if (canMoveAndIsNotCheck(piece, piece.getX() + i, piece.getY() + i)
                                || canMoveAndIsNotCheck(piece, piece.getX() + i, piece.getY() - i)
                                || canMoveAndIsNotCheck(piece, piece.getX() - i, piece.getY() + i)
                                || canMoveAndIsNotCheck(piece, piece.getX() - i, piece.getY() - i)) {
                            return false;
                        }
                    }
                    break;
                case KNIGHT:
                    if (canMoveAndIsNotCheck(piece, piece.getX() + 2, piece.getY() + 1)
                            || canMoveAndIsNotCheck(piece, piece.getX() + 2, piece.getY() - 1)
                            || canMoveAndIsNotCheck(piece, piece.getX() - 2, piece.getY() - 1)
                            || canMoveAndIsNotCheck(piece, piece.getX() - 2, piece.getY() +1)
                            || canMoveAndIsNotCheck(piece, piece.getX() - 1, piece.getY() + 2)
                            || canMoveAndIsNotCheck(piece, piece.getX() - 1, piece.getY() - 2)
                            || canMoveAndIsNotCheck(piece, piece.getX() + 1, piece.getY() + 2)
                            || canMoveAndIsNotCheck(piece, piece.getX() + 1, piece.getY() - 2)) {
                        return false;
                    }
                    break;
                case QUEEN:
                    for (int i = 0; i < 8; ++i) {
                        if (canMoveAndIsNotCheck(piece, piece.getX() + i, piece.getY() + i)
                                || canMoveAndIsNotCheck(piece, piece.getX() + i, piece.getY() - i)
                                || canMoveAndIsNotCheck(piece, piece.getX() - i, piece.getY() + i)
                                || canMoveAndIsNotCheck(piece, piece.getX() - i, piece.getY() - i)
                                || canMoveAndIsNotCheck(piece, piece.getX() + i, piece.getY() + i)
                                || canMoveAndIsNotCheck(piece, piece.getX() + i, piece.getY() - i)
                                || canMoveAndIsNotCheck(piece, piece.getX() - i, piece.getY() + i)
                                || canMoveAndIsNotCheck(piece, piece.getX() - i, piece.getY() - i)) {
                            return false;
                        }
                    }
                    break;
                case KING:
                    if (canMoveAndIsNotCheck(piece, piece.getX() + 1, piece.getY() + 1)
                            || canMoveAndIsNotCheck(piece, piece.getX() + 1, piece.getY())
                            || canMoveAndIsNotCheck(piece, piece.getX() + 1, piece.getY() - 1)
                            || canMoveAndIsNotCheck(piece, piece.getX() - 1, piece.getY() - 1)
                            || canMoveAndIsNotCheck(piece, piece.getX() - 1, piece.getY())
                            || canMoveAndIsNotCheck(piece, piece.getX() -1, piece.getY() + 1)
                            || canMoveAndIsNotCheck(piece, piece.getX(), piece.getY() - 1)
                            || canMoveAndIsNotCheck(piece, piece.getX(), piece.getY() + 1)) {
                        return false;
                    }
            }
        }
        return true;
    }


    private synchronized boolean moveAndUndo(Piece piece, int x, int y) {
        if (piece.getX() == -1 || piece.getY() == -1 || x < 0 || x > 7 || y < 0 || y > 7) {
            return false;
        }
        Piece copy = gameSession.getPiece(x, y);
        int preX = piece.getX();
        int preY = piece.getY();
        boolean canMove = gameSession.move(piece, x, y);

        if (canMove) {
            gameSession.setPieceDead(x, y);
            piece.setLocation(x, y);
            boolean isCheck = isCheck(piece.getColor());
            gameSession.getPiece(x, y).setLocation(preX, preY);
            if (copy.getColor() != null) {
                gameSession.addPiece(copy);
            }
            return isCheck;
        } else {
            return true;
        }
    }



    private synchronized boolean isCheck(Piece.Color color) {
        int i = 1;
        boolean leftUp = true;
        boolean rightUp = true;
        boolean leftDown = true;
        boolean rightDown = true;
        boolean up = true;
        boolean down = true;
        boolean left = true;
        boolean right = true;
        Piece king = gameSession
                .getPieces()
                .stream()
                .filter(e -> e.getType() == Piece.Type.KING && e.getColor() == color)
                .findFirst()
                .get();

        while (leftUp && king.getX() + i < 8 && king.getY() - i > -1) {
            if (gameSession.getPiece(king.getX() + i, king.getY() - i).getColor() == color) {
                leftUp = false;
            } else if (gameSession.getPiece(king.getX() + i, king.getY() - i).isAlive()
                    && gameSession.getPiece(king.getX() + i, king.getY() - i).isHittingOnDiagonal(color)) {
                return true;
            }
            i++;
        }
        i = 1;
        while (leftDown && king.getX() - i > -1 && king.getY() - i > -1) {
            if (gameSession.getPiece(king.getX() - i, king.getY() - i).getColor() == color) {
                leftDown = false;
            } else if (gameSession.getPiece(king.getX() - i, king.getY() - i).isAlive()
                    && gameSession.getPiece(king.getX() - i, king.getY() - i).isHittingOnDiagonal(color)) {
                return true;
            }
            i++;
        }
        i = 1;
        while (rightUp && king.getX() + i < 8 && king.getY() + i < 8) {
            if (gameSession.getPiece(king.getX() + i, king.getY() + i).getColor() == color) {
                rightUp = false;
            } else if (gameSession.getPiece(king.getX() + i, king.getY() + i).isAlive()
                    && gameSession.getPiece(king.getX() + i, king.getY() + i).isHittingOnDiagonal(color)) {
                return true;
            }
            i++;
        }
        i = 1;
        while (rightDown && king.getX() - i > -1 && king.getY() + i < 8) {
            if (gameSession.getPiece(king.getX() - i, king.getY() + i).getColor() == color) {
                rightDown = false;
            } else if (gameSession.getPiece(king.getX() - i, king.getY() + i).isAlive()
                    && gameSession.getPiece(king.getX() - i, king.getY() + i).isHittingOnDiagonal(color)) {
                return true;
            }
            i++;
        }
        i = 1;
        while (up && king.getX() + i < 8) {
            if (gameSession.getPiece(king.getX() + i, king.getY()).getColor() == color) {
                up = false;
            } else if (gameSession.getPiece(king.getX() + i, king.getY()).isAlive()
                    && gameSession.getPiece(king.getX() + i, king.getY()).isHittingOnLine(color)) {
                return true;
            }
            i++;
        }
        i = 1;
        while (down && king.getX() - i > -1) {
            if (gameSession.getPiece(king.getX() - i, king.getY()).getColor() == color) {
                down = false;
            } else if (gameSession.getPiece(king.getX() - i, king.getY()).isAlive()
                    && gameSession.getPiece(king.getX() - i, king.getY()).isHittingOnLine(color)) {
                return true;
            }
            i++;
        }
        i = 1;
        while (left && king.getY() - i > -1) {
            if (gameSession.getPiece(king.getX(), king.getY() - i).getColor() == color) {
                left = false;
            } else if (gameSession.getPiece(king.getX(), king.getY() - i).isAlive()
                    && gameSession.getPiece(king.getX(), king.getY() - i).isHittingOnLine(color)) {
                return true;
            }
            i++;
        }
        i = 1;
        while (right && king.getY() + i < 8) {
            if (gameSession.getPiece(king.getX(), king.getY() + i).getColor() == color) {
                right = false;
            } else if (gameSession.getPiece(king.getX(), king.getY() + i).isAlive()
                    && gameSession.getPiece(king.getX(), king.getY() + i).isHittingOnLine(color)) {
                return true;
            }
            i++;
        }

        if (king.getX() - 2 > -1 && king.getY() - 1 > -1) {
            if (gameSession.getPiece(king.getX() - 2, king.getY() - 1).getType() == Piece.Type.KNIGHT
                    && gameSession.getPiece(king.getX() - 2, king.getY() - 1).getColor() != king.getColor()) {
                return true;
            }
        }
        if (king.getX() - 2 > -1 && king.getY() + 1 < 8) {
            if (gameSession.getPiece(king.getX() - 2, king.getY() + 1).getType() == Piece.Type.KNIGHT
                    && gameSession.getPiece(king.getX() - 2, king.getY() + 1).getColor() != king.getColor()) {
                return true;
            }
        }
        if (king.getX() + 2 < 8 && king.getY() - 1 > -1) {
            if (gameSession.getPiece(king.getX() + 2, king.getY() - 1).getType() == Piece.Type.KNIGHT
                    && gameSession.getPiece(king.getX() + 2, king.getY() - 1).getColor() != king.getColor()) {
                return true;
            }
        }
        if (king.getX() + 2 < 8 && king.getY() + 1 < 8) {
            if (gameSession.getPiece(king.getX() + 2, king.getY() + 1).getType() == Piece.Type.KNIGHT
                    && gameSession.getPiece(king.getX() + 2, king.getY() + 1).getColor() != king.getColor()) {
                return true;
            }
        }
        if (king.getX() - 1 > -1 && king.getY() - 2 > -1) {
            if (gameSession.getPiece(king.getX() - 1, king.getY() - 2).getType() == Piece.Type.KNIGHT
                    && gameSession.getPiece(king.getX() - 1, king.getY() - 2).getColor() != king.getColor()) {
                return true;
            }
        }
        if (king.getX() - 1 > -1 && king.getY() + 2 < 8) {
            if (gameSession.getPiece(king.getX() - 1, king.getY() + 2).getType() == Piece.Type.KNIGHT
                    && gameSession.getPiece(king.getX() - 1, king.getY() + 2).getColor() != king.getColor()) {
                return true;
            }
        }
        if (king.getX() + 1 < 8 && king.getY() + 2 < 8) {
            if (gameSession.getPiece(king.getX() + 1, king.getY() + 2).getType() == Piece.Type.KNIGHT
                    && gameSession.getPiece(king.getX() + 1, king.getY() + 2).getColor() != king.getColor()) {
                return true;
            }
        }
        if (king.getX() + 1 < 8 && king.getY() - 2 > -1) {
            if (gameSession.getPiece(king.getX() + 1, king.getY() - 2).getType() == Piece.Type.KNIGHT
                    && gameSession.getPiece(king.getX() + 1, king.getY() - 2).getColor() != king.getColor()) {
                return true;
            }
        }

        switch (color) {
            case WHITE:
                if (king.getX() - 1 > -1) {
                    if (king.getY() - 1 > -1 && gameSession.getPiece(king.getX() - 1, king.getY() - 1).isPawnOfColor(Piece.Color.BLACK)) {
                        return true;
                    } else if (king.getY() + 1 < 8 && gameSession.getPiece(king.getX() - 1, king.getY() + 1).isPawnOfColor(Piece.Color.BLACK)) {
                        return true;
                    }
                }
                break;
            case BLACK:
                if (king.getX() + 1 < 8) {
                    if (king.getY() - 1 > -1 && gameSession.getPiece(king.getX() + 1, king.getY() - 1).isPawnOfColor(Piece.Color.WHITE)) {
                        return true;
                    } else if (king.getY() + 1 < 8 && gameSession.getPiece(king.getX() + 1, king.getY() + 1).isPawnOfColor(Piece.Color.WHITE)) {
                        return true;
                    }
                }
                break;
        }
        return false;
    }
}
