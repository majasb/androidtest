package com.example.uiservice.gameclient;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import bratseth.maja.androidtest.service.ResultHandlerBase;
import bratseth.maja.androidtest.service.TypedCallbackListener;
import bratseth.maja.msgtransport.ui.MsgServiceActivity;
import com.example.uiservice.spi.*;
import com.example.uiservice.spi.events.PlayerMoved;
import com.skullab.chess.Chessboard;

public class GameActivity extends MsgServiceActivity {

    private static final String TAG = GameActivity.class.getSimpleName();

    private GameService gameService;
    private GameState gameState;
    private Chessboard board;
    private Piece selectedPiece;
    private Button startButton;
    private Button endButton;

    private int numberOfEvents = 0;

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
        getEventBroker().addListener(new TypedCallbackListener<PlayerMoved>(PlayerMoved.class) {
            @Override
            public void handle(PlayerMoved callback) {
                numberOfEvents++;
                if (numberOfEvents / 1000 == 0) {
                    Log.i(TAG, "Number of events so far: " + numberOfEvents);
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
                
                runAutomatedGame();
            }
        });
    }

    private void runAutomatedGame() {
        for (int i = 0; i < 50000; i++) {
            // could prepare the positions in case this ever becomes the bottleneck
            // or if we want to super stress the remoting
            char x = (char) (i % 8);
            int y = i % 8;
            Position newPosition = new Position(x, y);
            makeMove(newPosition);
        }
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
        // let's just assume it's really fast
        // but as a separate test, we should check whether the remoting takes enough time on the ui thread
        // that it becomes visible
        /*
        for (Position position : gameState.getPositions()) {
            Piece piece = gameState.get(position);
            if (piece == null) {
                board.setDefaultDrawableOnCell(position);
            } else {
                board.setDrawableOnCell(position, createDrawable(piece));
            }
        }
        */
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
