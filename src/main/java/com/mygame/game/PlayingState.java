package com.mygame.game;

public class PlayingState
        implements GameState {

    @Override
    public void update(float tpf) {

        System.out.println(
                "Jugando..."
        );
    }
}