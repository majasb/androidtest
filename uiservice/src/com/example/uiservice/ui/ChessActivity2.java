package com.example.uiservice.ui;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.example.uiservice.R;
import com.example.uiservice.service.ResultHandler;
import com.example.uiservice.spi.Board;
import com.example.uiservice.spi.ChessService2;
import com.example.uiservice.spi.Piece;
import com.example.uiservice.spi.Position;
import com.skullab.chess.Chessboard;

public class ChessActivity2 extends Activity {

    private ViewEnvironment viewEnvironment;
    private ChessService2 chessService;
    private Board board;
    private Chessboard chessboardView;
    private Piece selectedPiece;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ViewGroup main = (ViewGroup) View.inflate(this, R.layout.main, null);
        setContentView(main);

        chessboardView = (Chessboard) main.findViewById(R.id.chess);
        chessboardView.setVisibility(View.INVISIBLE);
        final Button startButton = (Button) main.findViewById(R.id.startButton);
        final Button endButton = (Button) main.findViewById(R.id.endButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                viewEnvironment.execute(new BackgroundExecutable<Board>() {
                    @Override
                    public Board runInBackground() {
                        return chessService.startGame();
                    }
                    @Override
                    public void onResult(Board result) {
                        selectedPiece = null;
                        setBoard(board);
                        chessboardView.setVisibility(View.VISIBLE);
                    }
                });
            }
        });
        endButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                chessboardView.setVisibility(View.INVISIBLE);
                viewEnvironment.execute(new BackgroundExecutable<Void>() {
                    @Override
                    public Void runInBackground() {
                        chessService.endGame();
                        return null;
                    }
                    @Override
                    public void onResult(Void result) {
                    }
                });
            }
        });

        chessService = getChessService();


        chessboardView.setOnCellClickListener(new Chessboard.OnCellClickListener() {
            public void onCellClick(final Position position) {
                if (selectedPiece == null) {
                    if (board.isOccupiedByPlayer(position)) {
                        selectedPiece = board.get(position);
                    }
                } else {
                    viewEnvironment.execute(new BackgroundExecutable<Board>() {
                        @Override
                        public Board runInBackground() {
                            return chessService.move(selectedPiece, position);
                        }
                        @Override
                        public void onResult(Board updatedBoard) {
                            setBoard(updatedBoard);
                            selectedPiece = null;
                        }
                    });
                }
            }
        });
    }

    private void updateUi() {
        chessboardView.clearAll();
        for (Position position : board.getOccupiedPositions()) {
            Piece piece = board.get(position);
            chessboardView.setDrawableOnCell(position, createDrawable(piece));
        }
    }

    private ColorDrawable createDrawable(Piece piece) {
        return new ColorDrawable(piece.getColor());
    }

    private void setBoard(Board board) {
        this.board = board;
        updateUi();
    }

    private ChessService2 getChessService() {
        // would be in super class or similar
        return null;
    }

}
