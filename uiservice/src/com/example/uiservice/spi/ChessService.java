package com.example.uiservice.spi;

import com.example.uiservice.service.ResultHandler;

public interface ChessService {

    void startGame(ResultHandler<Board> resultHandler);

    void move(Piece selectedPiece, Position position, ResultHandler<Board> resultHandler);

    void endGame();

}
