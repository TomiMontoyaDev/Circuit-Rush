package com.mygame.entities;

import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.scene.*;
import com.jme3.scene.shape.Box;

import com.mygame.strategy.*;
import com.mygame.observer.*;

import java.util.ArrayList;
import java.util.List;

public class Car implements Subject {

    private final AssetManager assetManager;

    private final Node carNode;
    private final Node wheelsNode;

    private DrivingStrategy strategy;
    private boolean drifting = false;

    private float currentSpeed = 0f;
    private float maxSpeed = 140f;
    private float maxReverseSpeed = 70f;

    private float acceleration = 55f;
    private float brakePower = 90f;
    private float drag = 10f;

    private float driftFactor = 1f;

    private ParticleEmitter smoke;

    private final List<Observer> observers = new ArrayList<>();
    private final List<Geometry> skidMarks = new ArrayList<>();

    public Car(AssetManager assetManager) {

        this.assetManager = assetManager;
        this.strategy = new ArcadeDrive();

        carNode = new Node("Car");
        wheelsNode = new Node("RearWheelsFX");

        Spatial carModel = assetManager.loadModel("Models/Convertible.glb");

        carModel.setLocalScale(3.0f);
        carModel.center();

        carNode.attachChild(carModel);

        // SPAWN POSICIÓN
        carNode.setLocalTranslation(-262, 5f, 420);

        // FIX IMPORTANTE: ROTACIÓN DEL SPAWN
        carNode.setLocalRotation(
                new Quaternion().fromAngles(0, FastMath.PI, 0)
        );

        wheelsNode.setLocalTranslation(0, 0, 0);
        carNode.attachChild(wheelsNode);

        createSmoke();
    }

    public void move(
            boolean forward,
            boolean backward,
            boolean left,
            boolean right,
            float tpf) {

        float rotation = strategy.getRotationSpeed();

        if (drifting) {
            rotation = 6.5f;
            driftFactor = 0.65f;
            smoke.setParticlesPerSec(35);
        } else {
            driftFactor = 1f;
            smoke.setParticlesPerSec(0);
        }

        if (forward) {
            currentSpeed += acceleration * tpf;
        } else if (backward) {
            currentSpeed -= brakePower * tpf;
        } else {
            if (currentSpeed > 0) {
                currentSpeed -= drag * tpf;
                if (currentSpeed < 0) currentSpeed = 0;
            } else {
                currentSpeed += drag * tpf;
                if (currentSpeed > 0) currentSpeed = 0;
            }
        }

        currentSpeed = FastMath.clamp(currentSpeed, -maxReverseSpeed, maxSpeed);

        Vector3f forwardDir =
                carNode.getLocalRotation().mult(Vector3f.UNIT_Z);

        Vector3f movement =
                forwardDir.mult(currentSpeed * tpf * driftFactor);

        carNode.move(movement);

        if (left) carNode.rotate(0, rotation * tpf, 0);
        if (right) carNode.rotate(0, -rotation * tpf, 0);

        if (drifting && Math.abs(currentSpeed) > 10 && Math.random() > 0.5) {

            Vector3f rearOffset =
                    carNode.getLocalRotation()
                            .mult(new Vector3f(0, 0, -2.0f));

            createSkidMark(
                    carNode.getWorldTranslation().add(rearOffset)
            );
        }

        notifyObservers();
    }

    public void accelerate() {
        currentSpeed = FastMath.clamp(
                currentSpeed + 25f,
                -maxReverseSpeed,
                maxSpeed * 1.4f
        );
    }

    public void setDrifting(boolean drifting) {
        this.drifting = drifting;
    }

    public float getSpeed() {
        return currentSpeed;
    }

    public Node getNode() {
        return carNode;
    }

    private void createSmoke() {

        smoke = new ParticleEmitter("Smoke", ParticleMesh.Type.Triangle, 80);

        Material mat = new Material(assetManager,
                "Common/MatDefs/Misc/Particle.j3md");

        smoke.setMaterial(mat);

        smoke.setStartColor(new ColorRGBA(0.8f, 0.8f, 0.8f, 0.6f));
        smoke.setEndColor(new ColorRGBA(0.8f, 0.8f, 0.8f, 0f));

        smoke.setStartSize(1.2f);
        smoke.setEndSize(4.0f);

        smoke.setLowLife(0.4f);
        smoke.setHighLife(1.5f);

        smoke.setGravity(0, 0, 0);

        smoke.setParticlesPerSec(0);

        smoke.setLocalTranslation(0f, 0.3f, -2.5f);

        wheelsNode.attachChild(smoke);
    }

    private void createSkidMark(Vector3f pos) {

        Box b = new Box(0.35f, 0.01f, 2.0f);

        Geometry g = new Geometry("skid", b);

        Material mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");

        mat.setColor("Color", new ColorRGBA(0, 0, 0, 0.8f));

        g.setMaterial(mat);

        g.setLocalTranslation(pos.x, 0.02f, pos.z);

        if (carNode.getParent() != null) {
            carNode.getParent().attachChild(g);
        }

        skidMarks.add(g);
    }

    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer o : observers) {
            o.update(getSpeed());
        }
    }
}