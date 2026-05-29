package com.mygame.builder;

import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

import java.util.ArrayList;
import java.util.List;

public class TrackBuilder {

    private final AssetManager assetManager;
    private final Node rootNode;

    // 🎮 gameplay data
    private final List<Vector3f> checkpoints = new ArrayList<>();
    private final List<Vector3f> boostPads = new ArrayList<>();

    private Spatial map;

    public TrackBuilder(AssetManager assetManager, Node rootNode) {
        this.assetManager = assetManager;
        this.rootNode = rootNode;
    }

    public void build() {

        loadMap();
        setupGameplayPoints();

        System.out.println("🏁 DRIFT TRACK LOADED");
    }

    // 🗺️ CARGAR MAPA REAL
    private void loadMap() {

        map = assetManager.loadModel("Scenes/driftracetrack.glb");

        map.setLocalTranslation(0, 0, 0);
        map.setLocalScale(5f); // ajusta aquí si está grande/pequeño

        rootNode.attachChild(map);
    }

    // 📍 PUNTOS DE JUEGO (BASADOS EN EL MAPA)
    private void setupGameplayPoints() {

        // 🏁 START / FINISH
        checkpoints.add(new Vector3f(0, 0, 0));

        // 📍 CHECKPOINTS MANUALES (ajústalos al track real)
        checkpoints.add(new Vector3f(80, 0, 40));
        checkpoints.add(new Vector3f(160, 0, 0));
        checkpoints.add(new Vector3f(80, 0, -40));
        checkpoints.add(new Vector3f(-80, 0, -40));
        checkpoints.add(new Vector3f(-160, 0, 0));
        checkpoints.add(new Vector3f(-80, 0, 40));

        // ⚡ BOOSTS
        boostPads.add(new Vector3f(60, 0, 20));
        boostPads.add(new Vector3f(-60, 0, -20));
    }

    // 📍 GETTERS
    public List<Vector3f> getCheckpoints() {
        return checkpoints;
    }

    public List<Vector3f> getBoostPads() {
        return boostPads;
    }

    public Spatial getMap() {
        return map;
    }
}