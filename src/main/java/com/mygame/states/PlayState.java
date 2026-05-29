package com.mygame.states;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
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

    @Override
protected void initialize(Application app) {

    SimpleApplication sa = (SimpleApplication) app;

    // 🧹 limpiar solo UI del menú
    sa.getGuiNode().detachAllChildren();

    // 🚗 mundo
    TrackBuilder builder = new TrackBuilder(sa.getAssetManager(), sa.getRootNode());
    builder.build();

    LightManager.createLights(sa.getRootNode());

    car = VehicleFactory.createCar(sa.getAssetManager());
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
        car.getNode().getWorldTranslation()
);

}

    @Override protected void onEnable() {}
    @Override protected void onDisable() {}
    @Override protected void cleanup(Application app) {}
}