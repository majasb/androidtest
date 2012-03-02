package com.example.uiservice.ui;

import java.util.*;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.widget.Toast;
import com.example.uiservice.service.ResultHandler;
import com.example.uiservice.spi.*;

public class ChessServiceMock implements ChessService {
    
    private final Context context;
    private final Map<Position, Piece> position2Piece = new HashMap<Position, Piece>();
    private final Map<Piece, Position> piece2Position = new HashMap<Piece, Position>();

    private Piece gamePiece = new Piece(Color.GREEN);

    public ChessServiceMock(Context context) {
        this.context = context;
    }

    // exception handling would not be in the service implementation, but in an infrastructure layer

    public void startGame(ResultHandler<Board> resultHandler) {
        try {
            final Board board = invokeStartGame();
            resultHandler.result(board);
        } catch (Exception e) {
            try {
                resultHandler.exception(e);
            } catch (Exception e2) {
                defaultHandleException(e2);
            }
        }
    }

    private Board invokeStartGame() {
        clear();
        moveTo(new Position('a', 1), new Piece(Color.RED));
        moveTo(new Position('h', 8), gamePiece);
        return createBoard();
    }

    public void move(Piece piece, Position position, ResultHandler<Board> resultHandler) {
        try {
            final Board board = invokeMove(piece, position);
            resultHandler.result(board);
        } catch (Exception e) {
            try {
                resultHandler.exception(e);
            } catch (Exception e2) {
                defaultHandleException(e2);
            }
        }
    }

    public void endGame() {
        clear();
    }

    private void clear() {
        piece2Position.clear();
        position2Piece.clear();
    }

    private Board invokeMove(Piece piece, Position position) {
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

    private Board createBoard() {
        return new Board(Color.RED, position2Piece);
    }

    private void defaultHandleException(Exception e) {
        Log.e(getClass().getSimpleName(), "Error", e);
        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
    }

}
