package com.example.uiservice.spi.mock;

import java.util.*;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;
import bratseth.maja.androidtest.service.CallbackHandler;
import bratseth.maja.androidtest.service.ExceptionHandler;
import bratseth.maja.androidtest.service.ResultHandler;

import com.example.uiservice.spi.*;

public class GameServiceMock implements GameService {
    
    private final Context context;
    private final CallbackHandler callbackHandler;

    private final Map<Position, Piece> position2Piece = new HashMap<Position, Piece>();
    private final Map<Piece, Position> piece2Position = new HashMap<Piece, Position>();

    private final Piece gamePiece = new Piece(Color.GREEN);

    public GameServiceMock(Context context, CallbackHandler callbackHandler) {
        this.context = context;
        this.callbackHandler = callbackHandler;
    }

    // exception handling would not be in the service implementation, but in an infrastructure layer

    @Override
    public void startGame(ResultHandler<GameState> resultHandler) {
        try {
            clear();
            moveTo(new Position('a', 1), new Piece(Color.RED));
            moveTo(new Position('h', 8), gamePiece);
            final GameState gameState = createBoard();
            resultHandler.result(gameState);
        } catch (Exception e) {
            handleException(e, resultHandler);
        }
    }

    @Override
    public void move(Piece piece, Position position, ResultHandler<GameState> resultHandler) {
        try {
            final GameState gameState = invokeMove(piece, position);
            resultHandler.result(gameState);

            nofityListeners();
        } catch (Exception e) {
            handleException(e, resultHandler);
        }
    }

    private void nofityListeners() {
        callbackHandler.sendCallback(new GameMoveHappened());
    }

    @Override
    public void endGame(ExceptionHandler... exceptionHandlers) {
        try {
            clear();
        } catch (Exception e) {
            handleException(e, exceptionHandlers);
        }
    }

    private void handleException(Exception e, ExceptionHandler... exceptionHandlers) {
        try {
            for (ExceptionHandler exceptionHandler : exceptionHandlers) {
                exceptionHandler.exception(e);
            }
        } catch (Exception e2) {
            defaultHandleException(e2);
        }
    }

    private void clear() {
        piece2Position.clear();
        position2Piece.clear();
    }

    private GameState invokeMove(Piece piece, Position position) {
        if (position2Piece.containsKey(position)) {
            throw new IllegalArgumentException("Position " + position + " is already occupied");
        }
        moveTo(position, piece);
        moveGamePiece();
        return createBoard();
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

    private void defaultHandleException(Exception e) {
        Log.e(getClass().getSimpleName(), "Error", e);
        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }

}
