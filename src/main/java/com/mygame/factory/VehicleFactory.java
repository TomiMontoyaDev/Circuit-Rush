package com.mygame.factory;

import com.jme3.asset.AssetManager;
import com.mygame.entities.Car;

public class VehicleFactory {

    public static Car createCar(
            AssetManager assetManager) {

        return new Car(assetManager);
    }
}