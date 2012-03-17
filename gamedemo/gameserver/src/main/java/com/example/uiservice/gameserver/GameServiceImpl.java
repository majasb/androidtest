package com.example.uiservice.gameserver;

import java.util.*;

import android.graphics.Color;
import bratseth.maja.androidtest.service.EventPublisher;
import bratseth.maja.androidtest.service.ExceptionHandler;
import bratseth.maja.androidtest.service.ResultHandler;

import com.example.uiservice.spi.*;
import com.example.uiservice.spi.events.PlayerMoved;

public class GameServiceImpl implements GameService {
    
    private final EventPublisher eventPublisher;

    private final Map<Position, Piece> position2Piece = new HashMap<Position, Piece>();
    private final Map<Piece, Position> piece2Position = new HashMap<Piece, Position>();

    private final Piece gamePiece = new Piece(Color.GREEN);

    public GameServiceImpl(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

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
        final Position oldPosition = piece2Position.get(piece);
        moveTo(position, piece);
        moveGamePiece();
        final GameState gameState = createBoard();
        resultHandler.result(gameState);

        notifyListeners(new Move(oldPosition, position));
    }

    private void notifyListeners(Move move) {
        eventPublisher.publishEvent(new PlayerMoved(move));
    }

    @Override
    public void endGame(ExceptionHandler... exceptionHandler) {
        clear();
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
