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


    private final int numberOfEventsPerTest = 500;
    private int numberOfEvents = 0;
    
    private long start;

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
                if (numberOfEvents % 100 == 0) {
                    //Log.i(TAG, "Number of events so far: " + numberOfEvents);
                }
                if (numberOfEvents == numberOfEventsPerTest) {
                    long end = System.currentTimeMillis();
                    Log.i(TAG, "Total time to receive all replies: " + (end - start) + " ms");
                    numberOfEvents = 0;
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
        start = System.currentTimeMillis();
        final int n = 10 * numberOfEventsPerTest;
        for (int i = 0; i < n; i++) {
            if (i % 10 == 0) {
                char x = (char) (i % 8);
                int y = i % 8;
                Position newPosition = new Position(x, y);
                makeMove(newPosition);
            }
        }
        final long end = System.currentTimeMillis();
        Log.i(TAG, "Total time to send all invocations: " + (end - start) + " ms");
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
