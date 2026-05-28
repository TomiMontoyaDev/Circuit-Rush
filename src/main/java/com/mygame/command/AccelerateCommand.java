package com.mygame.command;

import com.mygame.entities.Car;

public class AccelerateCommand
        implements Command {

    private Car car;

    public AccelerateCommand(Car car) {

        this.car = car;
    }

    @Override
    public void execute() {

        car.accelerate();
    }
}