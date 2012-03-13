package com.example.uiservice.gameserver;

import java.util.*;

import android.graphics.Color;
import bratseth.maja.androidtest.service.ExceptionHandler;
import bratseth.maja.androidtest.service.ResultHandler;

import com.example.uiservice.spi.GameCallbackListener;
import com.example.uiservice.spi.GameService;
import com.example.uiservice.spi.GameState;
import com.example.uiservice.spi.Piece;
import com.example.uiservice.spi.Position;

/**
 * @author Maja S Bratseth
 */
public class GameServiceImpl implements GameService {
    
    private final Set<GameCallbackListener> listeners = Collections.synchronizedSet(new HashSet<GameCallbackListener>());

    private final Map<Position, Piece> position2Piece = new HashMap<Position, Piece>();
    private final Map<Piece, Position> piece2Position = new HashMap<Piece, Position>();

    private Piece gamePiece = new Piece(Color.GREEN);

    @Override
    public void startGame(ResultHandler<GameState> resultHandler) {
        clear();
        moveTo(new Position('a', 1), new Piece(Color.RED));
        moveTo(new Position('h', 8), gamePiece);
        final GameState gameState = createBoard();
        resultHandler.result(gameState);
    }

    @Override
    public void move(Piece piece, Position position, ResultHandler<GameState> resultHandler) {
        if (position2Piece.containsKey(position)) {
            throw new IllegalArgumentException("Position " + position + " is already occupied");
        }
        moveTo(position, piece);
        moveGamePiece();
        final GameState gameState = createBoard();
        resultHandler.result(gameState);

        notifyListeners();
    }

    private void notifyListeners() {
        for (GameCallbackListener listener : listeners) {
            listener.somethingHappened();
        }
    }

    @Override
    public void endGame(ExceptionHandler... exceptionHandler) {
        clear();
    }

    @Override
    public void addGameCallbackListener(GameCallbackListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void removeGameCallbackListener(GameCallbackListener listener) {
        this.listeners.remove(listener);
    }

    private void clear() {
        piece2Position.clear();
        position2Piece.clear();
    }

    private void moveGamePiece() {
        Position newPosition = findNewPosition();
        while (position2Piece.containsKey(newPosition)) {
            newPosition = findNewPosition();
        }
        moveTo(newPosition, gamePiece);
    }

    private Position findNewPosition() {
        Random random = new Random();
        char newX = (char) (random.nextInt((int) ('h' - 'a')) + 'a');
        int newY = random.nextInt(8 - 1) + 1;
        return new Position(newX, newY);
    }

    private void moveTo(Position position, Piece piece) {
        Position oldPosition = piece2Position.get(piece);
        position2Piece.remove(oldPosition);
        position2Piece.put(position, piece);
        piece2Position.put(piece, position);
    }

    private GameState createBoard() {
        return new GameState(Color.RED, position2Piece);
    }

}
