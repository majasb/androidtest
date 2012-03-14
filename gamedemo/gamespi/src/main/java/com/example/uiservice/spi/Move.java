package com.example.uiservice.spi;

import java.io.Serializable;

/**
 * @author Maja S Bratseth
 */
public class Move implements Serializable {

    private final Position from;
    private final Position to;

    public Move(Position from, Position to) {
        this.from = from;
        this.to = to;
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

}
