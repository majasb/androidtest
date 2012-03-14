package com.example.uiservice.spi;

import bratseth.maja.androidtest.service.CallbackEvent;

public class PlayerMoved extends CallbackEvent {
    
    private final Move move;

    public PlayerMoved(Move move) {
        this.move = move;
    }

    public Move getMove() {
        return move;
    }

}
