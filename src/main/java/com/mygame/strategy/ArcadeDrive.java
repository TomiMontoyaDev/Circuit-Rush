package com.mygame.strategy;

public class ArcadeDrive
        implements DrivingStrategy {

    @Override
    public float getRotationSpeed() {

        return 4f;
    }

    @Override
    public float getSpeed() {

        return 200f;
    }
}