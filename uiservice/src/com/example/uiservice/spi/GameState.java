package com.example.uiservice.spi;

import java.util.*;

/**
 * @author Maja S Bratseth
 */
public class GameState {

    private final int playerColor;
    private final Map<Position, Piece> occupiedPositions = new HashMap<Position, Piece>();

    public GameState(int playerColor, Map<Position, Piece> occupiedPositions) {
        this.playerColor = playerColor;
        this.occupiedPositions.putAll(occupiedPositions);
    }

    public Piece get(Position position) {
        return occupiedPositions.get(position);
    }

    public Iterable<Position> getOccupiedPositions() {
        return Collections.unmodifiableCollection(occupiedPositions.keySet());
    }

    public boolean isOccupiedByPlayer(Position position) {
        Piece piece = get(position);
        return (piece != null) && piece.getColor() == playerColor;
    }
    
}
