package com.mygame.ui;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;

public class Hud {

    private BitmapText speedText;
    private BitmapText driftText;
    private BitmapText cameraText;
    private BitmapText debugText;

    public Hud(BitmapFont font, Node guiNode, Camera cam) {

        speedText = new BitmapText(font);
        speedText.setSize(font.getCharSet().getRenderedSize());
        speedText.setColor(ColorRGBA.White);
        speedText.setLocalTranslation(10, cam.getHeight() - 10, 0);

        driftText = new BitmapText(font);
        driftText.setSize(font.getCharSet().getRenderedSize());
        driftText.setColor(ColorRGBA.Cyan);
        driftText.setLocalTranslation(10, cam.getHeight() - 30, 0);

        cameraText = new BitmapText(font);
        cameraText.setSize(font.getCharSet().getRenderedSize());
        cameraText.setColor(ColorRGBA.Yellow);
        cameraText.setLocalTranslation(10, cam.getHeight() - 50, 0);

        debugText = new BitmapText(font);
        debugText.setSize(font.getCharSet().getRenderedSize());
        debugText.setColor(ColorRGBA.White);
        debugText.setLocalTranslation(10, cam.getHeight() - 100, 0);

        guiNode.attachChild(speedText);
        guiNode.attachChild(driftText);
        guiNode.attachChild(cameraText);
        guiNode.attachChild(debugText);
    }

    // ✅ AHORA SÍ: agregamos position
    public void update(float speed, boolean drifting, int cameraMode, Vector3f position) {

        speedText.setText("Speed: " + (int) speed + " km/h");

        driftText.setText(
                drifting ? "DRIFT: ON 🔥" : "DRIFT: OFF"
        );

        cameraText.setText("Camera Mode: " + cameraMode);

        debugText.setText(
                "X: " + String.format("%.2f", position.x) + "\n" +
                "Y: " + String.format("%.2f", position.y) + "\n" +
                "Z: " + String.format("%.2f", position.z)
        );
    }
}