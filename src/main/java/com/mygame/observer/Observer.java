package com.mygame.observer;

import com.jme3.math.Vector3f;

public interface Observer {

    void update(
            float speed,
            boolean drifting,
            int cameraMode,
            Vector3f position,
            float driftScore
    );
}