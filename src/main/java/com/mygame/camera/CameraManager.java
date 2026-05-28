package com.mygame.camera;

import com.jme3.math.*;
import com.jme3.renderer.Camera;
import com.mygame.entities.Car;

public class CameraManager {

    private Camera cam;

    private int cameraMode = 0;

    public CameraManager(Camera cam) {

        this.cam = cam;
    }

    public void updateCamera(
            Car car,
            boolean changeCamera) {

        if (changeCamera) {

            cameraMode++;

            if (cameraMode > 3) {

                cameraMode = 0;
            }
        }

        Vector3f carPos =
                car.getNode().getLocalTranslation();

        Quaternion carRot =
                car.getNode().getLocalRotation();

        Vector3f forwardDir =
                carRot.mult(Vector3f.UNIT_Z);

        Vector3f camPos;

        if (cameraMode == 0) {

            camPos = carPos
                    .subtract(forwardDir.mult(20))
                    .add(0, 8, 0);
        }

        else if (cameraMode == 1) {

            camPos = carPos
                    .subtract(forwardDir.mult(35))
                    .add(0, 15, 0);
        }

        else if (cameraMode == 2) {

            camPos = carPos.add(0, 40, 0);
        }

        else{

            camPos = carPos
                    .add(forwardDir.mult(20))
                    .add(0, 4, 0);
        }

        smoothCamera(camPos);

        cam.lookAt(
                carPos.add(0, 2, 0),
                Vector3f.UNIT_Y
        );
    }

    private void smoothCamera(Vector3f targetPos) {

        Vector3f currentCam =
                cam.getLocation();

        Vector3f smoothCam =
                FastMath.interpolateLinear(
                        0.08f,
                        currentCam,
                        targetPos
                );

        cam.setLocation(smoothCam);
    }

    public int getCameraMode() {
    return cameraMode;
}

}