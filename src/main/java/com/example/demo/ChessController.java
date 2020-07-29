package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.lang.constant.Constable;
import java.util.Hashtable;
import java.util.UUID;
import java.util.stream.Collectors;
/*

                                x  ________________
                                |   |
                                |   |
                                V   |
                                    |______________
                                    y ->

                             */
/*

        TODO:

        - dodanie bicia w przelocie


 */


@Slf4j
@Controller
@EnableScheduling
public class ChessController {
    private final GameSession gameSession;
    private Hashtable<Long, GameSession> gameSessions;
    private BeanFactory beanFactory;

    @Autowired
    public ChessController(GameSession gameSession, Hashtable<Long, GameSession> gameSessions, BeanFactory beanFactory) {
        log.info("CHESS CONTROLLER CONSTRUCTOR");
        this.gameSessions = gameSessions;
        this.beanFactory = beanFactory;
        this.gameSession = gameSession;
    }

    // szachownica
    @GetMapping("/home")
    public String homePage(HttpSession session) {
        //log.info(gameSession.getPieces().toString());
        log.info("CREATED BEAN: " + beanFactory.getBean("player").toString());
        if (getPlayerFromSession(session) == null) {
            session.setAttribute("player", beanFactory.getBean("player"));
        }
        return "home";
    }

    // zwraca obiekt z id gracza ktory ma teraz ture
    @GetMapping("/turn")
    @ResponseBody
    public UUID playersTurn() {
        return gameSession.getPlayersTurn();
    }

    // zwraca obiekt gracza
    @GetMapping("/")
    @ResponseBody
    public Player home(HttpSession session) {
        //log.info(gameSession.getPlayerWhite().toString());
        //log.info(gameSession.getPlayerBlack().toString());
        return getPlayerFromSession(session);
    }

    // szuka gierki
    @GetMapping("/findGame")
    @ResponseBody
    public ResponseEntity<HttpStatus> findGame(HttpSession session) {
        /*if (gameSession.getPlayerWhite().getSide() == null) {
            gameSession.setPlayerWhite(getPlayerFromSession(session));
            gameSession.getPlayerWhite().setSide("WHITE");
            gameSession.getPlayerWhite().setGameSessionId(gameSession.getSessionId());
            gameSession.setPlayersTurn(getPlayerFromSession(session).getId());
            return new ResponseEntity<>(HttpStatus.OK);
        } else if (gameSession.getPlayerBlack().getSide() == null) {
            gameSession.setPlayerBlack(getPlayerFromSession(session));
            gameSession.getPlayerBlack().setSide("BLACK");
            gameSession.getPlayerBlack().setGameSessionId(gameSession.getSessionId());
            return new ResponseEntity<>(HttpStatus.OK);
        }*/

        if (getPlayerFromSession(session).getSide() == null && gameSession.getPlayerWhite().getSide() == null) {
            gameSession.setPlayerWhite(getPlayerFromSession(session));
            gameSession.getPlayerWhite().setSide("WHITE");
            gameSession.getPlayerWhite().setGameSessionId(gameSession.getSessionId());
            gameSession.setPlayersTurn(getPlayerFromSession(session).getId());
        } else if (getPlayerFromSession(session).getSide() == null && gameSession.getPlayerBlack().getSide() == null){
            gameSession.setPlayerWhite(getPlayerFromSession(session));
            gameSession.getPlayerWhite().setSide("BLACK");
            gameSession.getPlayerWhite().setGameSessionId(gameSession.getSessionId());
            gameSession.setPlayersTurn(getPlayerFromSession(session).getId());
        }
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    // sprawdza czy gra jest gotowa
    @GetMapping("/isGameReady")
    @ResponseBody
    public ResponseEntity<HttpStatus> isGameRead() {
        if (gameSession.getPlayerWhite().getSide() == null) {
            //log.info(gameSession.getPlayerWhite().toString());
        }
        if (gameSession.getPlayerBlack().getSide() == null) {
            //log.info(gameSession.getPlayerBlack().toString());
        }
        if (gameSession.getPlayerWhite().getSide() != null && gameSession.getPlayerBlack().getSide() != null) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
        }
    }

    // zwraca GameSession
    @GetMapping("/game")
    @ResponseBody
    public GameSession game(HttpSession session) {
        //log.info(getPlayerFromSession(session).toString());
        //log.info("TURN: " + gameSession.getPlayersTurn());
        //log.info(gameSession.isCheckMate() + "");
        return gameSession;
    }


    // ruszanie figur
    @GetMapping("move/{fromX}/{fromY}/{toX}/{toY}")
    @ResponseBody
    public synchronized HttpEntity<? extends Constable> movePiece(@PathVariable("fromX") int x,
                                                @PathVariable("fromY") int y,
                                                @PathVariable("toX") int row,
                                                @PathVariable("toY") int column,
                                                HttpSession session) {
        if (!gameSession.isCheckMate()) {
            Player player = getPlayerFromSession(session);
            Piece p = gameSession.getPiece(x, y);
            boolean canMove;
            if ((x == row && y == column)
                    && p.getColor() != null
                    || p.getColor() != Piece.Color.valueOf(player.getSide())) {
                //log.info("NIE TAKIE ID");
                return new ResponseEntity<>(HttpStatus.ACCEPTED);
            }

            canMove = !moveAndUndo(p, row, column);
            log.info(String.valueOf(canMove));

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
                return new ResponseEntity<>(HttpStatus.OK);
            } else if (canMove) {
                p.setLocation(row, column);
                //log.info(gameSession.toString());
                //log.info(gameSession.getPlayerWhite().toString());
                //log.info(gameSession.getPlayerBlack().toString());
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
        if (copy.getType() == Piece.Type.PAWN) {
            log.info(copy.toString());
        }
        gameSession.setPieceDead(x, y);
        boolean canMove = gameSession.move(piece, x, y);

        if (canMove && !isCheck(piece.getColor())) {
            piece.setLocation(preX, preY);
            if (copy.getColor() != null) {
                gameSession.addPiece(copy);
            }
            return true;
        } else {
            piece.setLocation(preX, preY);
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
        if (copy.getType() == Piece.Type.PAWN) {
            log.info(copy.toString());
        }
        gameSession.setPieceDead(x, y);
        boolean canMove = gameSession.move(piece, x, y);

        if (canMove) {
            boolean isCheck = isCheck(piece.getColor());
            piece.setLocation(preX, preY);
            if (copy.getColor() != null) {
                gameSession.addPiece(copy);
            }
            return isCheck;
        } else {
            piece.setLocation(preX, preY);
            if (copy.getColor() != null) {
                gameSession.addPiece(copy);
            }
            return false;
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

    // zwraca obiekt gracza danej sesji http
    private Player getPlayerFromSession(HttpSession session) {
        if (session.getAttribute("player") == null) {
            session.setAttribute("player", new Player());
        }
        return (Player) session.getAttribute("player");
    }

}