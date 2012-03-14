package com.example.uiservice.spi;

import bratseth.maja.androidtest.service.CallbackListener;

public interface GameMoveListener extends CallbackListener {

    void moveHappened();

}
