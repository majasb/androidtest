package com.example.uiservice.ui;

import com.example.uiservice.service.ResultHandler;
import com.example.uiservice.spi.GameState;
import com.example.uiservice.spi.Piece;
import com.example.uiservice.spi.Position;

/**
 * @author Maja S Bratseth
 */
public interface GameService {

    void startGame(ResultHandler<GameState> resultHandler);

    void move(Piece piece, Position position, ResultHandler<GameState> resultHandler);

    void endGame();

}
