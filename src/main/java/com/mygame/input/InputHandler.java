package com.mygame.input;

import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.*;

public class InputHandler implements ActionListener {

    private boolean forward;
    private boolean backward;
    private boolean left;
    private boolean right;
    private boolean drifting;

    private boolean changeCamera;
    private boolean cameraLeft;
    private boolean cameraRight;

    public InputHandler(InputManager inputManager) {

        inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Backward", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Drift", new KeyTrigger(KeyInput.KEY_SPACE));

        // 📷 cámara
        inputManager.addMapping("ChangeCamera", new KeyTrigger(KeyInput.KEY_TAB));
        inputManager.addMapping("CamLeft", new KeyTrigger(KeyInput.KEY_LEFT));
        inputManager.addMapping("CamRight", new KeyTrigger(KeyInput.KEY_RIGHT));

        inputManager.addListener(this,
                "Forward", "Backward", "Left", "Right",
                "Drift", "ChangeCamera",
                "CamLeft", "CamRight"
        );
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {

        switch (name) {

            case "Forward" -> forward = isPressed;
            case "Backward" -> backward = isPressed;
            case "Left" -> left = isPressed;
            case "Right" -> right = isPressed;
            case "Drift" -> drifting = isPressed;

            case "ChangeCamera" -> {
                if (isPressed) changeCamera = true;
            }

            case "CamLeft" -> cameraLeft = isPressed;
            case "CamRight" -> cameraRight = isPressed;
        }
    }

    public boolean isForward() { return forward; }
    public boolean isBackward() { return backward; }
    public boolean isLeft() { return left; }
    public boolean isRight() { return right; }
    public boolean isDrifting() { return drifting; }

    public boolean isChangeCameraPressed() {
        boolean v = changeCamera;
        changeCamera = false;
        return v;
    }

    public boolean isCameraLeft() { return cameraLeft; }
    public boolean isCameraRight() { return cameraRight; }
}