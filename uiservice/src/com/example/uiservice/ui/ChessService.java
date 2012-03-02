package com.example.uiservice.ui;

import com.example.uiservice.service.ResultHandler;
import com.example.uiservice.spi.Board;
import com.example.uiservice.spi.Piece;
import com.example.uiservice.spi.Position;

/**
 * @author Maja S Bratseth
 */
public interface ChessService {

    void startGame(ResultHandler<Board> resultHandler);

    void move(Piece piece, Position position, ResultHandler<Board> resultHandler);

    void endGame();

}
