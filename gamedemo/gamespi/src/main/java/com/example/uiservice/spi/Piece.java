package com.example.uiservice.spi;

import java.io.Serializable;

public class Piece implements Serializable {

    private final int color;

    public Piece(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Piece piece = (Piece) o;

        if (color != piece.color) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return color;
    }
    
    @Override
    public String toString() {
        return super.toString() + "{" + color + "}";
    }

}
