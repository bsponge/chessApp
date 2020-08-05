package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.support.GenericWebApplicationContext;

import java.lang.constant.Constable;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/api")
@SessionAttributes("player")
public class ApiController {
    private ExtendedHashMap gameSessions;
    private GenericWebApplicationContext context;

    private static final ResponseEntity<HttpStatus> OK_STATUS = new ResponseEntity<>(HttpStatus.OK);
    private static final ResponseEntity<HttpStatus> ACCEPTED_STATUS = new ResponseEntity<>(HttpStatus.ACCEPTED);

    @ModelAttribute("player")
    public Player player() {
        return new Player();
    }

    @Autowired
    public ApiController(ExtendedHashMap gameSessions, GenericWebApplicationContext context) {
        this.gameSessions = gameSessions;
        this.context = context;
        log.info("APICONTROLLER CREATED");
    }


    // zwraca obiekt gracza
    @GetMapping("/player")
    @ResponseBody
    public Player getPlayer(@ModelAttribute("player") Player player) {
        return player;
    }

    @GetMapping("/findGame")
    @ResponseBody
    public synchronized ResponseEntity<HttpStatus> findGame(@ModelAttribute("player") Player player) {
        GameSession gameSession = gameSessions.getGame();
        log.info("IN FIND GAME: " + player.toString());
        if (gameSession != null) {
            if (gameSession.getPlayerWhite() == null) {
                gameSession.setPlayerWhite(player);
                gameSession.setPlayersTurn(player.getId());
                player.setSide("WHITE");
                player.setGameSessionId(gameSession.getSessionId());
            } else if (gameSession.getPlayerBlack() == null) {
                gameSession.setPlayerBlack(player);
                player.setSide("BLACK");
                player.setGameSessionId(gameSession.getSessionId());
            }
            return OK_STATUS;
        } else {
            return ACCEPTED_STATUS;
        }
    }

    @GetMapping("/isGameReady")
    @ResponseBody
    public ResponseEntity<HttpStatus> isGameReady(@ModelAttribute("player") Player player) {
        log.info("IN IS GAME READY: " + player.toString());
        if (player.getGameSessionId() == null || !gameSessions.containsKey(player.getGameSessionId())) {
            return ACCEPTED_STATUS;
        }
        GameSession gameSession = gameSessions.get(player.getGameSessionId());
        if (gameSession != null) {
            if (gameSession.getPlayerBlack() != null && gameSession.getPlayerWhite() != null) {
                return OK_STATUS;
            } else {
                return ACCEPTED_STATUS;
            }
        } else {
            return ACCEPTED_STATUS;
        }
    }

    @GetMapping("/game")
    @ResponseBody
    public GameSession game(@ModelAttribute("player") Player player) {
        if (player.getGameSessionId() == null || !gameSessions.containsKey(player.getGameSessionId())) {
            return new GameSession(true);
        }
        return gameSessions.get(player.getGameSessionId());
    }

    // ruszanie figur
    @GetMapping("move/{fromX}/{fromY}/{toX}/{toY}")
    @ResponseBody
    public synchronized HttpEntity<? extends Constable> movePiece(@PathVariable("fromX") int x,
                                                                  @PathVariable("fromY") int y,
                                                                  @PathVariable("toX") int row,
                                                                  @PathVariable("toY") int column,
                                                                  @ModelAttribute("player") Player player) {
        GameSession gameSession = gameSessions.get(player.getGameSessionId());
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
            log.info("W MOVE");


            canMove = moveAndUndo(player, p, row, column);


            //log.info("" + canMove);
            //log.info(gameSession.getPiece(row, column).toString());

            if (canMove && gameSession.getPiece(row, column).isAlive()) {
                log.info("TU?");
                gameSession.setPieceDead(row, column);
                p.setLocation(row, column);
                gameSession.setPlayersTurn(
                        gameSession.getPlayerWhite().getId().equals(gameSession.getPlayersTurn()) ? gameSession.getPlayerBlack().getId() : gameSession.getPlayerWhite().getId()
                );
                boolean isCheckOnWhite = isCheck(player, Piece.Color.WHITE);
                boolean isCheckOnBlack = isCheck(player, Piece.Color.BLACK);
                if (isCheckOnWhite && isMate(player, Piece.Color.WHITE)) {
                    gameSession.setCheckMate(true);

                } else if (isCheckOnBlack && isMate(player, Piece.Color.BLACK)) {
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
                log.info("TU W MOVE");
                p.setLocation(row, column);
                gameSession.setPlayersTurn(
                        gameSession.getPlayerWhite().getId().equals(gameSession.getPlayersTurn()) ? gameSession.getPlayerBlack().getId() : gameSession.getPlayerWhite().getId()
                );
                boolean isCheckOnWhite = isCheck(player, Piece.Color.WHITE);
                boolean isCheckOnBlack = isCheck(player, Piece.Color.BLACK);
                if (isCheckOnWhite && isMate(player, Piece.Color.WHITE)) {
                    gameSession.setCheckMate(true);

                } else if (isCheckOnBlack && isMate(player, Piece.Color.BLACK)) {
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

    private boolean canMoveAndIsNotCheck(@ModelAttribute("player") Player player, Piece piece, int x, int y) {
        GameSession gameSession = gameSessions.get(player.getGameSessionId());
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

        if (canMove && !isCheck(player, piece.getColor())) {
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
    private synchronized boolean isMate(@ModelAttribute("player") Player player, Piece.Color color) {
        GameSession gameSession = gameSessions.get(player.getGameSessionId());
        for (Piece piece : gameSession.getPieces().stream().filter(e -> e.getColor() == color).collect(Collectors.toList())) {
            switch (piece.getType()) {
                case PAWN:
                    switch (piece.getColor()) {
                        case WHITE:
                            if (canMoveAndIsNotCheck(player, piece, piece.getX() - 1, piece.getY())
                                    || canMoveAndIsNotCheck(player, piece, piece.getX() - 2, piece.getY())
                                    || canMoveAndIsNotCheck(player, piece, piece.getX() - 1, piece.getY() - 1)
                                    || canMoveAndIsNotCheck(player, piece, piece.getX() - 1, piece.getY() + 1)) {
                                return false;
                            }
                        case BLACK:
                            if (canMoveAndIsNotCheck(player, piece, piece.getX() + 1, piece.getY())
                                    || canMoveAndIsNotCheck(player, piece, piece.getX() + 2, piece.getY())
                                    || canMoveAndIsNotCheck(player, piece, piece.getX() + 1, piece.getY() - 1)
                                    || canMoveAndIsNotCheck(player, piece, piece.getX() + 1, piece.getY() + 1)) {
                                return false;
                            }
                    }
                    break;
                case ROOK:
                    for (int i = 0; i < 8; ++i) {
                        if (canMoveAndIsNotCheck(player, piece, piece.getX() + i, piece.getY())
                                || canMoveAndIsNotCheck(player, piece, piece.getX() - i, piece.getY())
                                || canMoveAndIsNotCheck(player, piece, piece.getX(), piece.getY() + i)
                                || canMoveAndIsNotCheck(player, piece, piece.getX(), piece.getY() - i)) {
                            return false;
                        }
                    }
                    break;
                case BISHOP:
                    for (int i = 0; i < 8; ++i) {
                        if (canMoveAndIsNotCheck(player, piece, piece.getX() + i, piece.getY() + i)
                                || canMoveAndIsNotCheck(player, piece, piece.getX() + i, piece.getY() - i)
                                || canMoveAndIsNotCheck(player, piece, piece.getX() - i, piece.getY() + i)
                                || canMoveAndIsNotCheck(player, piece, piece.getX() - i, piece.getY() - i)) {
                            return false;
                        }
                    }
                    break;
                case KNIGHT:
                    if (canMoveAndIsNotCheck(player, piece, piece.getX() + 2, piece.getY() + 1)
                            || canMoveAndIsNotCheck(player, piece, piece.getX() + 2, piece.getY() - 1)
                            || canMoveAndIsNotCheck(player, piece, piece.getX() - 2, piece.getY() - 1)
                            || canMoveAndIsNotCheck(player, piece, piece.getX() - 2, piece.getY() +1)
                            || canMoveAndIsNotCheck(player, piece, piece.getX() - 1, piece.getY() + 2)
                            || canMoveAndIsNotCheck(player, piece, piece.getX() - 1, piece.getY() - 2)
                            || canMoveAndIsNotCheck(player, piece, piece.getX() + 1, piece.getY() + 2)
                            || canMoveAndIsNotCheck(player, piece, piece.getX() + 1, piece.getY() - 2)) {
                        return false;
                    }
                    break;
                case QUEEN:
                    for (int i = 0; i < 8; ++i) {
                        if (canMoveAndIsNotCheck(player, piece, piece.getX() + i, piece.getY() + i)
                                || canMoveAndIsNotCheck(player, piece, piece.getX() + i, piece.getY() - i)
                                || canMoveAndIsNotCheck(player, piece, piece.getX() - i, piece.getY() + i)
                                || canMoveAndIsNotCheck(player, piece, piece.getX() - i, piece.getY() - i)
                                || canMoveAndIsNotCheck(player, piece, piece.getX() + i, piece.getY() + i)
                                || canMoveAndIsNotCheck(player, piece, piece.getX() + i, piece.getY() - i)
                                || canMoveAndIsNotCheck(player, piece, piece.getX() - i, piece.getY() + i)
                                || canMoveAndIsNotCheck(player, piece, piece.getX() - i, piece.getY() - i)) {
                            return false;
                        }
                    }
                    break;
                case KING:
                    if (canMoveAndIsNotCheck(player, piece, piece.getX() + 1, piece.getY() + 1)
                            || canMoveAndIsNotCheck(player, piece, piece.getX() + 1, piece.getY())
                            || canMoveAndIsNotCheck(player, piece, piece.getX() + 1, piece.getY() - 1)
                            || canMoveAndIsNotCheck(player, piece, piece.getX() - 1, piece.getY() - 1)
                            || canMoveAndIsNotCheck(player, piece, piece.getX() - 1, piece.getY())
                            || canMoveAndIsNotCheck(player, piece, piece.getX() -1, piece.getY() + 1)
                            || canMoveAndIsNotCheck(player, piece, piece.getX(), piece.getY() - 1)
                            || canMoveAndIsNotCheck(player, piece, piece.getX(), piece.getY() + 1)) {
                        return false;
                    }
            }
        }
        return true;
    }


    private synchronized boolean moveAndUndo(@ModelAttribute("player") Player player, Piece piece, int x, int y) {
        GameSession gameSession = gameSessions.get(player.getGameSessionId());
        if (piece.getX() == -1 || piece.getY() == -1 || x < 0 || x > 7 || y < 0 || y > 7) {
            return false;
        }
        Piece copy = new Piece(gameSession.getPiece(x, y));
        int preX = piece.getX();
        int preY = piece.getY();
        boolean canMove = gameSession.move(piece, x, y);
        log.info(piece.toString());
        log.info(canMove+"");

        if (canMove) {
            gameSession.setPieceDead(x, y);
            piece.setLocation(x, y);
            boolean isCheck = isCheck(player, piece.getColor());
            //gameSession.getPiece(x, y).setLocation(preX, preY);
            piece.setLocation(preX, preY);
            if (copy.getColor() != null) {
                gameSession.addPiece(copy);
            }
            return !isCheck;
        } else {
            return false;
        }
    }



    private synchronized boolean isCheck(@ModelAttribute("player") Player player, Piece.Color color) {
        GameSession gameSession = gameSessions.get(player.getGameSessionId());
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
