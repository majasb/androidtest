package com.example.uiservice.ui;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.example.uiservice.R;
import com.example.uiservice.service.ResultHandler;
import com.example.uiservice.spi.*;
import com.skullab.chess.Chessboard;

public class GameActivity extends Activity {

    private GameService gameService;
    private GameState gameState;
    private Chessboard board;
    private Piece selectedPiece;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ViewGroup main = (ViewGroup) View.inflate(this, R.layout.main, null);
        setContentView(main);

        board = (Chessboard) main.findViewById(R.id.chess);
        board.setVisibility(View.INVISIBLE);
        final Button startButton = (Button) main.findViewById(R.id.startButton);
        final Button endButton = (Button) main.findViewById(R.id.endButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                gameService.startGame(new ResultHandler<GameState>() {
                    @Override
                    public void result(GameState gameState) {
                        selectedPiece = null;
                        setGameState(gameState);
                        board.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
        endButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                board.setVisibility(View.INVISIBLE);
                gameService.endGame();
            }
        });

        gameService = getGameService();


        board.setOnCellClickListener(new Chessboard.OnCellClickListener() {
            public void onCellClick(Position position) {
                if (selectedPiece == null) {
                    if (gameState.isOccupiedByPlayer(position)) {
                        selectedPiece = gameState.get(position);
                    }
                }
                else {
                    gameService.move(selectedPiece, position, new ResultHandler<GameState>() {
                        @Override
                        public void result(GameState updatedGameState) {
                            setGameState(updatedGameState);
                            selectedPiece = null;
                        }
                    });
                }
            }
        });
    }

    private void updateUi() {
        board.clearAll();
        for (Position position : gameState.getOccupiedPositions()) {
            Piece piece = gameState.get(position);
            board.setDrawableOnCell(position, createDrawable(piece));
        }
    }

    private ColorDrawable createDrawable(Piece piece) {
        return new ColorDrawable(piece.getColor());
    }

    private void setGameState(GameState gameState) {
        this.gameState = gameState;
        updateUi();
    }

    private GameService getGameService() {
        // would be in super class or similar
        return new GameServiceMock(getApplicationContext());
    }

}
