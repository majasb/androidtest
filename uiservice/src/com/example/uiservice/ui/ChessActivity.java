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

public class ChessActivity extends Activity {

    private ChessService chessService;
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
                chessService.startGame(new ResultHandler<Board>() {
                    @Override
                    public void result(Board board) {
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
                chessService.endGame();
            }
        });

        chessService = getChessService();


        chessboardView.setOnCellClickListener(new Chessboard.OnCellClickListener() {
            public void onCellClick(Position position) {
                if (selectedPiece == null) {
                    if (board.isOccupiedByPlayer(position)) {
                        selectedPiece = board.get(position);
                    }
                } else {
                    chessService.move(selectedPiece, position, new ResultHandler<Board>() {
                        @Override
                        public void result(Board updatedBoard) {
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

    private ChessService getChessService() {
        // would be in super class or similar
        return new ChessServiceMock(getApplicationContext());
    }

}
