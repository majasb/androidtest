package com.example.uiservice.gameclient;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import bratseth.maja.androidtest.service.ui.MsgServiceActivity;
import com.example.uiservice.service.ResultHandlerBase;
import com.example.uiservice.spi.*;
import com.skullab.chess.Chessboard;

public class GameActivity extends MsgServiceActivity {

    private GameService gameService;
    private GameState gameState;
    private Chessboard board;
    private Piece selectedPiece;
    private Button startButton;
    private Button endButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();

        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                startGame();
            }
        });
        endButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                endGame();
            }
        });

        board.setOnCellClickListener(new Chessboard.OnCellClickListener() {
            public void onCellClick(Position position) {
                if (selectedPiece == null) {
                    if (gameState.isOccupiedByPlayer(position)) {
                        selectedPiece = gameState.get(position);
                    }
                }
                else {
                    makeMove(position);
                }
            }
        });
    }

    private void startGame() {
        getGameService().startGame(new ResultHandlerBase<GameState>() {
            @Override
            public void result(GameState gameState) {
                selectedPiece = null;
                setGameState(gameState);
                board.setVisibility(View.VISIBLE);
            }
        });
    }

    private void endGame() {
        board.setVisibility(View.INVISIBLE);
        getGameService().endGame();
    }

    private void makeMove(Position newPosition) {
        getGameService().move(selectedPiece, newPosition, new ResultHandlerBase<GameState>() {
            @Override
            public void result(GameState updatedGameState) {
                setGameState(updatedGameState);
                selectedPiece = null;
            }
        });
    }

    private void init() {
        final ViewGroup main = (ViewGroup) View.inflate(this, R.layout.main, null);
        setContentView(main);

        board = (Chessboard) main.findViewById(R.id.chess);
        board.setVisibility(View.INVISIBLE);
        startButton = (Button) main.findViewById(R.id.startButton);
        endButton = (Button) main.findViewById(R.id.endButton);
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
        if (gameService == null) {
            gameService = getServiceLocator().locate(GameService.class);
        }
        return gameService;
    }

}
