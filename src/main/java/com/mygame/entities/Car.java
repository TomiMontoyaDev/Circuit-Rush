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

    private float speedBoost = 1f;
    private float driftFactor = 1f;

    // 🚗 INERCIA (IMPORTANTE PARA “SUELTO”)
    private Vector3f velocity = new Vector3f(0, 0, 0);

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

    // 🚗 MOVIMIENTO SUELTO (ARCADE REAL)
    public void move(
            boolean forward,
            boolean backward,
            boolean left,
            boolean right,
            float tpf) {

        float speed = strategy.getSpeed() * speedBoost;
        float rotation = strategy.getRotationSpeed();

        // 💨 DRIFT SYSTEM
        if (drifting) {
            rotation = 6.5f;
            driftFactor = 0.5f;
            smoke.setParticlesPerSec(25);
        } else {
            driftFactor = 1f;
            smoke.setParticlesPerSec(0);
        }

        // 🧠 DIRECCIÓN
        Vector3f forwardDir = carNode.getLocalRotation().mult(Vector3f.UNIT_Z);

        Vector3f input = new Vector3f(0, 0, 0);

        if (forward) input.addLocal(forwardDir);
        if (backward) input.addLocal(forwardDir.negate());

        if (input.lengthSquared() > 0) {
            input.normalizeLocal().multLocal(speed);
        }

        // 🚗 INERCIA (CLAVE DEL “SUELTO”)
        velocity.interpolateLocal(input, tpf * 3.0f);

        // 🧊 FRICCIÓN
        float friction = drifting ? 2.2f : 4.5f;
        velocity.multLocal(1f - (friction * tpf));

        // 🚗 APLICAR MOVIMIENTO
        carNode.move(velocity.mult(tpf * driftFactor));

        // 🔁 ROTACIÓN SUAVE
        if (left) {
            carNode.rotate(0, rotation * tpf, 0);
        }
        if (right) {
            carNode.rotate(0, -rotation * tpf, 0);
        }

        // 🖤 SKIDMARKS SOLO EN DRIFT
        if (drifting && forward && Math.random() > 0.6) {
            createSkidMark(carNode.getWorldTranslation().clone());
        }

        notifyObservers();
        speedBoost = 1f;
    }

    // 🚀 BOOST
    public void accelerate() {
        speedBoost = 10f;
    }

    // 💨 DRIFT
    public void setDrifting(boolean drifting) {
        this.drifting = drifting;
    }

    // 💨 HUMO
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

    // 🖤 SKID MARKS
    private void createSkidMark(Vector3f pos) {

        Box b = new Box(0.2f, 0.1f, 0.6f);
        Geometry g = new Geometry("skid", b);

        Material mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");

        mat.setColor("Color", new ColorRGBA(0, 0, 0, 0.8f));

        g.setMaterial(mat);
        g.setLocalTranslation(pos.x, 0.1f, pos.z);

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
            observer.update(strategy.getSpeed());
        }
    }

    public Node getNode() {
        return carNode;
    }
}