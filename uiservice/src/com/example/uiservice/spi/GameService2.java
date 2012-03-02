package com.example.uiservice.spi;

public interface GameService2 {

    GameState startGame();

    GameState move(Piece selectedPiece, Position position);

    void endGame();

}
