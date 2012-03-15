package com.example.uiservice.spi;

import java.io.Serializable;
import java.util.*;

public class GameState implements Serializable {

    private final int playerColor;
    private final Map<Position, Piece> positions = new HashMap<Position, Piece>(); // stupidly inefficient representation

    public GameState(int playerColor, Map<Position, Piece> positions) {
        this.playerColor = playerColor;
        this.positions.putAll(positions);
    }

    public Piece get(Position position) {
        return positions.get(position);
    }

    public Iterable<Position> getPositions() {
        return Collections.unmodifiableCollection(positions.keySet());
    }

    public boolean isOccupiedByPlayer(Position position) {
        Piece piece = get(position);
        return (piece != null) && piece.getColor() == playerColor;
    }
    
}
