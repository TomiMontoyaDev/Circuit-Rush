package com.mygame.states;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.bullet.BulletAppState;
import com.mygame.entities.Car;
import com.mygame.factory.VehicleFactory;
import com.mygame.input.InputHandler;
import com.mygame.ui.Hud;
import com.mygame.world.LightManager;
import com.mygame.builder.TrackBuilder;
import com.mygame.camera.CameraManager;

public class PlayState extends BaseAppState {

    private Hud hud;
    private CameraManager camera;
    private InputHandler input;
    private Car car;
    private BulletAppState bulletAppState;

    @Override
protected void initialize(Application app) {

    SimpleApplication sa = (SimpleApplication) app;

    bulletAppState = new BulletAppState();
    sa.getStateManager().attach(bulletAppState);

    // 🧹 limpiar solo UI del menú
    sa.getGuiNode().detachAllChildren();

    // 🚗 mundo
    TrackBuilder builder = new TrackBuilder(sa.getAssetManager(), sa.getRootNode(), bulletAppState.getPhysicsSpace());
    builder.build();

    LightManager.createLights(sa.getRootNode());

    car = VehicleFactory.createCar(sa.getAssetManager(), bulletAppState.getPhysicsSpace());
    sa.getRootNode().attachChild(car.getNode());

    input = new InputHandler(sa.getInputManager());
    camera = new CameraManager(sa.getCamera());

    sa.getFlyByCamera().setEnabled(false);

    // 🎮 HUD (DESPUÉS de limpiar TODO)
    hud = new Hud(
        sa.getAssetManager().loadFont("Interface/Fonts/Default.fnt"),
        sa.getGuiNode(),
        sa.getCamera()
    );
}

    @Override
public void update(float tpf) {

    // 💨 DRIFT (ESTO ES LO QUE TE FALTA)
    car.setDrifting(input.isDrifting());

    // 🚗 movimiento
    car.move(
        input.isForward(),
        input.isBackward(),
        input.isLeft(),
        input.isRight(),
        tpf
    );

    // 🎥 cámara
    camera.updateCamera(car, input.isChangeCameraPressed());

    // 🎮 HUD
    hud.update(
        car.getSpeed(),
        input.isDrifting(),
        camera.getCameraMode(),
        car.getNode().getWorldTranslation(),
        car.getDriftScore()
);

}

    @Override protected void onEnable() {}
    @Override protected void onDisable() {}
    @Override protected void cleanup(Application app) {
        if (bulletAppState != null) {
            ((SimpleApplication) app).getStateManager().detach(bulletAppState);
            bulletAppState = null;
        }
    }
}