package com.example.uiservice.spi.events;

import bratseth.maja.androidtest.service.CallbackEvent;
import com.example.uiservice.spi.Move;

public class PlayerMoved extends CallbackEvent {
    
    private final Move move;

    public PlayerMoved(Move move) {
        this.move = move;
    }

    public Move getMove() {
        return move;
    }

}
