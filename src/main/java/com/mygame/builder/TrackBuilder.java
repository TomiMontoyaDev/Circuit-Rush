package com.mygame.builder;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;

public class TrackBuilder {

    private final AssetManager assetManager;
    private final Node rootNode;

    public TrackBuilder(AssetManager assetManager, Node rootNode) {
        this.assetManager = assetManager;
        this.rootNode = rootNode;
    }

    public void build() {

        createGround();
        createTrack();
        createWalls();
        createFinishLine();

        System.out.println("🏁 ARCADE TRACK READY");
    }

    // 🌍 PASTO INFINITO
    private void createGround() {

        Box ground = new Box(600, 0.1f, 600);
        Geometry g = new Geometry("Ground", ground);

        Material mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");

        mat.setColor("Color", new ColorRGBA(0.08f, 0.55f, 0.08f, 1f));

        g.setMaterial(mat);
        g.setLocalTranslation(0, -0.2f, 0);

        rootNode.attachChild(g);
    }

    // 🛣️ LOOP PRINCIPAL REAL
    private void createTrack() {

        float y = 0.05f;

        // recta inferior
        road("R1", 0, 0, 200, 20, y);

        // curva derecha
        road("R2", 220, 120, 40, 120, y);

        // recta derecha
        road("R3", 200, 240, 20, 200, y);

        // curva superior
        road("R4", 0, 360, 200, 20, y);

        // recta izquierda
        road("R5", -220, 240, 20, 200, y);

        // curva izquierda
        road("R6", -200, 120, 40, 120, y);
    }

    private void road(String name, float x, float z, float sx, float sz, float y) {

        Box b = new Box(sx, 0.1f, sz);
        Geometry g = new Geometry(name, b);

        Material mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");

        mat.setColor("Color", ColorRGBA.DarkGray);

        g.setMaterial(mat);
        g.setLocalTranslation(x, y, z);

        rootNode.attachChild(g);
    }

    // 🧱 PAREDES SIGUIENDO LA PISTA
    private void createWalls() {

        float h = 4f;

        // borde exterior simple (limpia sensación racing)
        for (int i = -250; i <= 250; i += 30) {

            wall(i, -20, h);
            wall(i, 400, h);

            wall(-250, i, h);
            wall(250, i, h);
        }
    }

    private void wall(float x, float z, float h) {

        Box b = new Box(4, h, 4);
        Geometry g = new Geometry("Wall", b);

        Material mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");

        mat.setColor("Color", new ColorRGBA(0.25f, 0.25f, 0.25f, 1f));

        g.setMaterial(mat);
        g.setLocalTranslation(x, h, z);

        rootNode.attachChild(g);
    }

    // 🏁 META REAL EN INICIO
    private void createFinishLine() {

        Box finish = new Box(30, 0.2f, 3);
        Geometry g = new Geometry("Finish", finish);

        Material mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");

        mat.setColor("Color", ColorRGBA.White);

        g.setMaterial(mat);
        g.setLocalTranslation(0, 0.3f, 0);

        rootNode.attachChild(g);
    }
}