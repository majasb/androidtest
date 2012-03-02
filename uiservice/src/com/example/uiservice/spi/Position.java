package com.example.uiservice.spi;

/**
 * @author Maja S Bratseth
 */
public class Position {

    private String x;
    private String y;

    public Position(String x, String y) {
        this.x = x;
        this.y = y;
    }

    public String getX() {
        return x;
    }

    public String getY() {
        return y;
    }
    
    @Override
    public String toString() {
        return x + y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Position position = (Position) o;

        if (!x.equals(position.x)) {
            return false;
        }
        if (!y.equals(position.y)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = x.hashCode();
        result = 31 * result + y.hashCode();
        return result;
    }
}
