package com.example.uiservice.ui;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.example.uiservice.R;
import com.example.uiservice.spi.GameState;
import com.example.uiservice.spi.GameService2;
import com.example.uiservice.spi.Piece;
import com.example.uiservice.spi.Position;
import com.skullab.chess.Chessboard;

public class GameActivity2 extends Activity {

    private ViewEnvironment viewEnvironment;
    private GameService2 gameService;
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
                viewEnvironment.execute(new BackgroundExecutable<GameState>() {
                    @Override
                    public GameState runInBackground() {
                        return gameService.startGame();
                    }
                    @Override
                    public void onResult(GameState result) {
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
                viewEnvironment.execute(new BackgroundExecutable<Void>() {
                    @Override
                    public Void runInBackground() {
                        gameService.endGame();
                        return null;
                    }
                    @Override
                    public void onResult(Void result) {
                    }
                });
            }
        });

        gameService = getGameService();


        board.setOnCellClickListener(new Chessboard.OnCellClickListener() {
            public void onCellClick(final Position position) {
                if (selectedPiece == null) {
                    if (gameState.isOccupiedByPlayer(position)) {
                        selectedPiece = gameState.get(position);
                    }
                }
                else {
                    viewEnvironment.execute(new BackgroundExecutable<GameState>() {
                        @Override
                        public GameState runInBackground() {
                            return gameService.move(selectedPiece, position);
                        }

                        @Override
                        public void onResult(GameState updatedGameState) {
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

    private GameService2 getGameService() {
        // would be in super class or similar
        return null;
    }

}
