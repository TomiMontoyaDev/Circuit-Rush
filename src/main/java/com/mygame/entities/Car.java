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
    private float maxSpeed = 200f;
    private float maxReverseSpeed = 75f;

    private float acceleration = 72f;
    private float brakePower = 110f;
    private float drag = 8f;

    private float driftScore = 0f;
    private float driftTime = 0f;

    private float driftFactor = 1f;
    private float driftTargetFactor = 1f;
    private float driftLerpSpeed = 9.0f; // how fast driftFactor approaches target (aggressive onset)

    private float heading = FastMath.PI;

    private final Vector3f velocity = new Vector3f();

    // tuning: separate lateral grip handling
    private float baseLateralGrip = 0.9f;
    private float driftLateralGrip = 0.14f; // mucho más suelto en drift

    // front/rear movement factors for drift feel without speed boost
    private float frontMoveFactorDrift = 1f;
    private float rearMoveFactorDrift = 1f;

    // input smoothing for more natural controls
    private float steerSmoothed = 0f;
    private float steerLerp = 6.0f; // higher = faster to target

    private float engineForceSmoothed = 0f;
    private float engineLerp = 5.5f;

    private ParticleEmitter smokeL;
    private ParticleEmitter smokeR;
    private ParticleEmitter driftTail;

    private final List<Observer> observers = new ArrayList<>();
    private final List<Geometry> skidMarks = new ArrayList<>();

    public Car(AssetManager assetManager) {

        this.assetManager = assetManager;
        this.strategy = new ArcadeDrive();
        this.maxSpeed = strategy.getSpeed();

        carNode = new Node("Car");
        wheelsNode = new Node("RearWheelsFX");

        Spatial carModel = assetManager.loadModel("Models/Convertible.glb");

        carModel.setLocalScale(3.0f);
        carModel.center();

        carNode.attachChild(carModel);

        // 🚗 SPAWN (NO TOCADO COMO PEDISTE)
        carNode.setLocalTranslation(-239, 5f, 4);

        // 🧭 ORIENTACIÓN INICIAL
        carNode.setLocalRotation(
            new Quaternion().fromAngles(0, heading, 0)
        );

        // 🎯 nodo de efectos alineado al carro
        carNode.attachChild(wheelsNode);

        createSmoke();
        createDriftTail();
    }

    public void move(
            boolean forward,
            boolean backward,
            boolean left,
            boolean right,
            float tpf) {

        float steerInput = 0f;

        if (left) {
            steerInput += 1f;
        }

        if (right) {
            steerInput -= 1f;
        }

        // suavizar la entrada de volante para evitar saltos bruscos
        steerSmoothed = FastMath.interpolateLinear(steerLerp * tpf, steerSmoothed, steerInput);
        float steerValue = steerSmoothed;

        float speedRatio = FastMath.clamp(
                Math.abs(currentSpeed) / maxSpeed,
                0f,
                1f
        );

        // 💨 Modo drift: soltamos agarre atrás y hacemos la respuesta más viva
        if (drifting) {
            driftTargetFactor = 1f;

            smokeL.setParticlesPerSec(80);
            smokeR.setParticlesPerSec(80);
            driftTail.setParticlesPerSec(160);

        } else {
            driftTargetFactor = 1f;

            smokeL.setParticlesPerSec(0);
            smokeR.setParticlesPerSec(0);
            driftTail.setParticlesPerSec(0);
        }

        // 🧭 El giro depende de la velocidad: más rápido = menos giro brusco
        float steeringStrength = 3.2f - (1.6f * speedRatio);

        if (drifting) {
            steeringStrength += 2.4f; // más responsivo al girar durante drift para mantener el frente pegado
        }

        float reverseSteering = currentSpeed < -1f ? -1f : 1f;

        heading += steerInput * steeringStrength * reverseSteering * tpf;
        carNode.setLocalRotation(
                new Quaternion().fromAngles(0, heading, 0)
        );

        Vector3f forwardDir =
                carNode.getLocalRotation().mult(Vector3f.UNIT_Z);

        Vector3f rightDir =
                carNode.getLocalRotation().mult(Vector3f.UNIT_X);

        // 🚀 Empuje principal: aceleración progresiva para que no se sienta cortada
        float engineForceTarget = 0f;
        if (forward) {
            engineForceTarget = acceleration * (1f - (0.35f * speedRatio));
        } else if (backward) {
            engineForceTarget = -brakePower * (currentSpeed > 0f ? 1f : 0.65f);
        }

        // suavizar cambios de fuerza del motor para sensación más natural
        engineForceSmoothed = FastMath.interpolateLinear(engineLerp * tpf, engineForceSmoothed, engineForceTarget);
        velocity.addLocal(forwardDir.mult(engineForceSmoothed * tpf));

        // 🪶 El giro mete inercia lateral; en drift esa inercia sube bastante
        if (Math.abs(steerValue) > 0.001f) {
            // reducir sensibilidad lateral directa y usar steerValue suavizado
            float driftPush = drifting ? 28f : 6f;
            float lateralBoost = driftPush + (speedRatio * (drifting ? 30f : 4f));
            velocity.addLocal(
                rightDir.mult(steerValue * lateralBoost * tpf)
            );
        }

        Vector3f forwardVelocity =
                forwardDir.mult(velocity.dot(forwardDir));

        Vector3f lateralVelocity =
                velocity.subtract(forwardVelocity);

        // 🛞 Grip: separamos agarre lateral y lo adaptamos según velocidad y drift
        float lateralGrip = drifting ? driftLateralGrip : baseLateralGrip;
        lateralGrip -= speedRatio * (drifting ? 0.22f : 0.05f);
        lateralGrip = FastMath.clamp(lateralGrip, drifting ? 0.08f : 0.78f, 0.95f);

        velocity.set(
            forwardVelocity.add(lateralVelocity.mult(lateralGrip))
        );

        // 🧭 Yaw inducido por deslizamiento: ayuda a mantener/controlar el derrape
        float slipAmount = lateralVelocity.length();
        if (slipAmount > 0.001f) {
            float slipSign = FastMath.sign(lateralVelocity.dot(rightDir));
            // reducir el efecto del yaw inducido por slip durante drift para que el frente siga la dirección
            float yawMultiplier = drifting ? 0.8f : 0.6f;
            float yawFromSlip = slipSign * slipAmount * 0.06f * yawMultiplier;
            heading += yawFromSlip * tpf;
            carNode.setLocalRotation(new Quaternion().fromAngles(0, heading, 0));
        }

        // 🌬️ Drag suave: deja que la velocidad caiga de forma natural
        float dragFactor = FastMath.clamp(
                1f - drag * tpf * (forward || backward ? 0.012f : 0.04f),
                0f,
                1f
        );

        velocity.multLocal(dragFactor);

        if (velocity.lengthSquared() < 0.001f) {
            velocity.set(Vector3f.ZERO);
        }

        currentSpeed = FastMath.clamp(
                velocity.dot(forwardDir),
                -maxReverseSpeed,
                maxSpeed
        );

        // 🚗 Movimiento final: suavizamos driftFactor hacia el objetivo y aplicamos
        driftFactor = FastMath.interpolateLinear(driftLerpSpeed * tpf, driftFactor, driftTargetFactor);

        // separamos movimiento frontal (pegado a la dirección) y trasero (suelto)
        Vector3f frontMovement = forwardVelocity.mult(tpf * driftFactor * (drifting ? frontMoveFactorDrift : 1f));
        Vector3f rearMovement = lateralVelocity.mult(tpf * (drifting ? rearMoveFactorDrift : 1f));

        Vector3f movement = frontMovement.add(rearMovement);

        carNode.move(movement);

        // 🛞 Skid real: solo cuando el coche está deslizando de verdad
        if (drifting && Math.abs(currentSpeed) > 10f && lateralVelocity.length() > 1.5f && Math.random() > 0.45f) {

            Vector3f rearOffset =
                    carNode.getLocalRotation()
                            .mult(new Vector3f(0, 0, -2.0f));

            createSkidMark(
                    carNode.getWorldTranslation().add(rearOffset)
            );
        }

        // 🏁 Puntuación del drift: premiamos velocidad y ángulo de deslizamiento
        if (drifting && Math.abs(currentSpeed) > 15f) {

            driftTime += tpf;

            float driftSpeedFactor = Math.abs(currentSpeed) / maxSpeed;
            float slipFactor = FastMath.clamp(lateralVelocity.length() / 30f, 0f, 1f);
            driftScore += (driftSpeedFactor * driftSpeedFactor * 90f + slipFactor * 35f) * tpf;

        } else {

            driftScore -= 30f * tpf;
            if (driftScore < 0) driftScore = 0;

            driftTime = 0f;
        }

        notifyObservers();
    }

    public void accelerate() {
        Vector3f forwardDir =
            carNode.getLocalRotation().mult(Vector3f.UNIT_Z);

        velocity.addLocal(forwardDir.mult(25f));

        currentSpeed = FastMath.clamp(
            velocity.dot(forwardDir),
            -maxReverseSpeed,
            maxSpeed * 1.4f
        );
    }

    public void setDrifting(boolean drifting) {
        if (drifting && !this.drifting) {
            // entering drift: give a larger lateral kick to help onset
            Vector3f rightDir = carNode.getLocalRotation().mult(Vector3f.UNIT_X);
            velocity.addLocal(rightDir.mult(12f * (currentSpeed >= 0 ? 1f : -1f)));
        }

        this.drifting = drifting;
        // target drift visual/physics intensity will be handled in move()
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

    private void createDriftTail() {

        driftTail = new ParticleEmitter("DriftTail", ParticleMesh.Type.Triangle, 60);

        Material mat = new Material(assetManager,
                "Common/MatDefs/Misc/Particle.j3md");

        driftTail.setMaterial(mat);

        driftTail.setStartColor(new ColorRGBA(0.75f, 0.75f, 0.75f, 0.7f));
        driftTail.setEndColor(new ColorRGBA(0.75f, 0.75f, 0.75f, 0f));

        driftTail.setStartSize(1.4f);
        driftTail.setEndSize(6.5f);

        driftTail.setLowLife(0.45f);
        driftTail.setHighLife(1.6f);

        driftTail.setGravity(0, 0, 0);
        driftTail.setParticlesPerSec(0);

        driftTail.setLocalTranslation(0f, 0.25f, -3.2f);

        wheelsNode.attachChild(driftTail);
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