package com.mygame.game;

public class PlayState implements GameState {

    private int lap = 0;
    private int checkpoint = 0;

    @Override
    public void update(float tpf) {

        if (checkpoint == 4) {
            lap++;
            checkpoint = 0;
            System.out.println("Lap: " + lap);
        }
    }

    public void addCheckpoint() {
        checkpoint++;
    }
}