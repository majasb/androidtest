package com.example.uiservice.spi;

public interface ChessService2 {

    Board startGame();

    Board move(Piece selectedPiece, Position position);

    void endGame();

}
