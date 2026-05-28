package com.mygame.game;

public class PauseState
        implements GameState {

    @Override
    public void update(float tpf) {

        System.out.println(
                "Juego pausado"
        );
    }
}