package com.mygame.world;

import com.jme3.light.*;
import com.jme3.math.*;
import com.jme3.scene.Node;

public class LightManager {

    public static void createLights(Node rootNode) {

        // ☀️ SOL PRINCIPAL
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-1f, -1.3f, -0.6f));
        sun.setColor(ColorRGBA.White.mult(2.5f)); // más natural
        rootNode.addLight(sun);

        // 🌤 LUZ AMBIENTE (suave)
        AmbientLight ambient = new AmbientLight();
        ambient.setColor(new ColorRGBA(0.6f, 0.7f, 0.9f, 1f).mult(0.8f));
        rootNode.addLight(ambient);
    }
}