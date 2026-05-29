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

    private float driftScore = 0f;
    private float driftTime = 0f;

    private float driftFactor = 1f;

    private ParticleEmitter smokeL;
    private ParticleEmitter smokeR;

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

        // 🚗 SPAWN (NO TOCADO COMO PEDISTE)
        carNode.setLocalTranslation(-262, 5f, 420);

        // 🧭 ORIENTACIÓN INICIAL
        carNode.setLocalRotation(
                new Quaternion().fromAngles(0, FastMath.PI, 0)
        );

        // 🎯 nodo de efectos alineado al carro
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

        // 💨 DRIFT MODE
        if (drifting) {
            rotation = 6.5f;
            driftFactor = 0.65f;

            smokeL.setParticlesPerSec(35);
            smokeR.setParticlesPerSec(35);

        } else {
            driftFactor = 1f;

            smokeL.setParticlesPerSec(0);
            smokeR.setParticlesPerSec(0);
        }

        // 🚀 SPEED
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

        // 🚗 MOVIMIENTO
        Vector3f forwardDir =
                carNode.getLocalRotation().mult(Vector3f.UNIT_Z);

        Vector3f movement =
                forwardDir.mult(currentSpeed * tpf * driftFactor);

        carNode.move(movement);

        // 🔁 ROTACIÓN
        if (left) carNode.rotate(0, rotation * tpf, 0);
        if (right) carNode.rotate(0, -rotation * tpf, 0);

        // 🛞 SKID REAL (ruedas traseras)
        if (drifting && Math.abs(currentSpeed) > 10f && Math.random() > 0.5f) {

            Vector3f rearOffset =
                    carNode.getLocalRotation()
                            .mult(new Vector3f(0, 0, -2.0f));

            createSkidMark(
                    carNode.getWorldTranslation().add(rearOffset)
            );
        }

        // 🏁 DRIFT SCORE (limpio y estable)
        if (drifting && Math.abs(currentSpeed) > 15f) {

            driftTime += tpf;

            float speedFactor = Math.abs(currentSpeed) / maxSpeed;
            driftScore += speedFactor * speedFactor * 120f * tpf;

        } else {

            driftScore -= 30f * tpf;
            if (driftScore < 0) driftScore = 0;

            driftTime = 0f;
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

    public float getDriftScore() {
        return driftScore;
    }

    public Node getNode() {
        return carNode;
    }

    // 💨 HUMO BIEN COLOCADO EN RUEDAS
    private void createSmoke() {

        smokeL = new ParticleEmitter("SmokeL", ParticleMesh.Type.Triangle, 40);
        smokeR = new ParticleEmitter("SmokeR", ParticleMesh.Type.Triangle, 40);

        Material mat = new Material(assetManager,
                "Common/MatDefs/Misc/Particle.j3md");

        smokeL.setMaterial(mat);
        smokeR.setMaterial(mat);

        smokeL.setStartColor(new ColorRGBA(0.8f, 0.8f, 0.8f, 0.6f));
        smokeR.setStartColor(new ColorRGBA(0.8f, 0.8f, 0.8f, 0.6f));

        smokeL.setEndColor(new ColorRGBA(0.8f, 0.8f, 0.8f, 0f));
        smokeR.setEndColor(new ColorRGBA(0.8f, 0.8f, 0.8f, 0f));

        smokeL.setStartSize(1.2f);
        smokeR.setStartSize(1.2f);

        smokeL.setEndSize(4.0f);
        smokeR.setEndSize(4.0f);

        smokeL.setLowLife(0.4f);
        smokeR.setLowLife(0.4f);

        smokeL.setHighLife(1.5f);
        smokeR.setHighLife(1.5f);

        smokeL.setGravity(0, 0, 0);
        smokeR.setGravity(0, 0, 0);

        smokeL.setParticlesPerSec(0);
        smokeR.setParticlesPerSec(0);

        smokeL.setLocalTranslation(-0.6f, 0.2f, -2.2f);
        smokeR.setLocalTranslation(0.6f, 0.2f, -2.2f);

        wheelsNode.attachChild(smokeL);
        wheelsNode.attachChild(smokeR);
    }

    // 🛞 SKID
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
            o.update(getSpeed(), drifting, 0, carNode.getWorldTranslation(), driftScore);
        }
    }
}