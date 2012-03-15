package com.example.uiservice.spi;

import bratseth.maja.androidtest.service.ExceptionHandler;
import bratseth.maja.androidtest.service.ResultHandler;

public interface GameService {

    void startGame(ResultHandler<GameState> resultHandler);

    void move(Piece piece, Position position, ResultHandler<GameState> resultHandler);

    void endGame(ExceptionHandler... exceptionHandler);

}
