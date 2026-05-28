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

    private Node carNode;
    private DrivingStrategy strategy;

    private boolean drifting = false;

    // 🚗 velocidad real acumulada
    private float currentSpeed = 0f;

    // límites estilo arcade/forza
    private float maxSpeed = 140f;
    private float maxReverseSpeed = 70f;

    private float acceleration = 55f;   // qué tan rápido sube velocidad
    private float brakePower = 90f;     // frenado
    private float drag = 10f;           // fricción natural

    private float speedBoost = 1f;

    private float driftFactor = 1f;

    private ParticleEmitter smoke;

    private List<Observer> observers = new ArrayList<>();
    private List<Geometry> skidMarks = new ArrayList<>();

    public Car(AssetManager assetManager) {

        this.assetManager = assetManager;

        strategy = new ArcadeDrive();

        carNode = new Node("Car");

        Spatial carModel = assetManager.loadModel(
                "Models/Convertible.glb"
        );

        carModel.scale(1f);
        carNode.attachChild(carModel);

        carNode.setLocalTranslation(0, 1, 0);

        createSmoke();
    }

    public void move(
            boolean forward,
            boolean backward,
            boolean left,
            boolean right,
            float tpf) {

        float rotation = strategy.getRotationSpeed();

        // 💨 drift tuning
        if (drifting) {
            rotation = 6.5f;
            driftFactor = 0.65f;
            smoke.setParticlesPerSec(25);
        } else {
            driftFactor = 1f;
            smoke.setParticlesPerSec(0);
        }

        // 🧠 ACELERACIÓN REAL (FORZA STYLE)
        if (forward) {
            currentSpeed += acceleration * tpf;
        } else if (backward) {
            currentSpeed -= brakePower * tpf;
        } else {
            // 🧊 desaceleración natural
            if (currentSpeed > 0) {
                currentSpeed -= drag * tpf;
                if (currentSpeed < 0) currentSpeed = 0;
            } else {
                currentSpeed += drag * tpf;
                if (currentSpeed > 0) currentSpeed = 0;
            }
        }

        // 🚧 límites
        currentSpeed = FastMath.clamp(
                currentSpeed,
                -maxReverseSpeed,
                maxSpeed
        );

        // 🧭 dirección del carro
        Vector3f forwardDir =
                carNode.getLocalRotation().mult(Vector3f.UNIT_Z);

        // 🚗 movimiento final
        Vector3f movement =
                forwardDir.mult(currentSpeed * tpf * driftFactor);

        carNode.move(movement);

        // 🔁 rotación
        if (left) {
            carNode.rotate(0, rotation * tpf, 0);
        }
        if (right) {
            carNode.rotate(0, -rotation * tpf, 0);
        }

        // 💨 skid only drift
        if (drifting && Math.abs(currentSpeed) > 10 && Math.random() > 0.6) {
            createSkidMark(carNode.getWorldTranslation().clone());
        }

        notifyObservers();
        speedBoost = 1f;
    }

    // 🚀 BOOST
    public void accelerate() {
        currentSpeed = Math.min(currentSpeed + 10f, maxSpeed * 1.4f);
    }

    public void setDrifting(boolean drifting) {
        this.drifting = drifting;
    }

    public float getSpeed() {
        return currentSpeed;
    }

    private void createSmoke() {

        smoke = new ParticleEmitter("Smoke", ParticleMesh.Type.Triangle, 60);

        Material mat = new Material(assetManager,
                "Common/MatDefs/Misc/Particle.j3md");

        smoke.setMaterial(mat);

        smoke.setImagesX(2);
        smoke.setImagesY(2);

        smoke.setStartColor(new ColorRGBA(0.8f, 0.8f, 0.8f, 0.6f));
        smoke.setEndColor(new ColorRGBA(0.8f, 0.8f, 0.8f, 0f));

        smoke.setStartSize(0.8f);
        smoke.setEndSize(3.5f);

        smoke.setLowLife(0.4f);
        smoke.setHighLife(1.5f);

        smoke.setGravity(0, 0, 0);

        smoke.setParticlesPerSec(0);

        smoke.setLocalTranslation(0, 0.4f, -3f);

        carNode.attachChild(smoke);
    }

    private void createSkidMark(Vector3f pos) {

        Box b = new Box(0.2f, 0.05f, 0.6f);
        Geometry g = new Geometry("skid", b);

        Material mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");

        mat.setColor("Color", new ColorRGBA(0, 0, 0, 0.8f));

        g.setMaterial(mat);
        g.setLocalTranslation(pos.x, 0.05f, pos.z);

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
        for (Observer observer : observers) {
            observer.update(getSpeed());
        }
    }

    public Node getNode() {
        return carNode;
    }
}