package com.mygame.factory;

import com.jme3.asset.AssetManager;
import com.jme3.bullet.PhysicsSpace;
import com.mygame.entities.Car;

public class VehicleFactory {

    public static Car createCar(
            AssetManager assetManager,
            PhysicsSpace physicsSpace) {

        return new Car(assetManager, physicsSpace);
    }
}