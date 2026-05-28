package com.mygame;

import com.mygame.game.GameState;
import com.mygame.game.PlayState;

import com.jme3.app.SimpleApplication;
import com.mygame.builder.TrackBuilder;
import com.mygame.camera.CameraManager;
import com.mygame.entities.Car;
import com.mygame.factory.VehicleFactory;
import com.mygame.input.InputHandler;
import com.mygame.world.LightManager;

import com.jme3.util.SkyFactory;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Spatial;

import com.mygame.ui.Hud;
import com.jme3.font.BitmapFont;

public class Main extends SimpleApplication {

    private Car car;
    private InputHandler inputHandler;
    private CameraManager cameraManager;
    private GameState gameState;
    private Hud hud;

    public static void main(String[] args) {
        new Main().start();
    }

    @Override
    public void simpleInitApp() {
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");

        hud = new Hud(font, guiNode, cam);
        gameState = new PlayState();

        // 🌤 SKY
        Spatial sky = SkyFactory.createSky(
                assetManager,
                "Textures/Sky/sky.hdr",
                SkyFactory.EnvMapType.EquirectMap
        );

        sky.setQueueBucket(RenderQueue.Bucket.Sky);
        sky.setCullHint(Spatial.CullHint.Never);
        sky.setLocalScale(500f);
        rootNode.attachChild(sky);


        flyCam.setEnabled(false);

        // 💡 LUCES
        LightManager.createLights(rootNode);

        // 🛣 TRACK
        TrackBuilder builder = new TrackBuilder(assetManager, rootNode);
        builder.build();

        // 🚗 CARRO
        car = VehicleFactory.createCar(assetManager);
        rootNode.attachChild(car.getNode());

        inputHandler = new InputHandler(inputManager);
        cameraManager = new CameraManager(cam);
    }

    @Override
    public void simpleUpdate(float tpf) {

        gameState.update(tpf);

        car.setDrifting(inputHandler.isDrifting());

        car.move(
                inputHandler.isForward(),
                inputHandler.isBackward(),
                inputHandler.isLeft(),
                inputHandler.isRight(),
                tpf
        );

        cameraManager.updateCamera(
                car,
                inputHandler.isChangeCameraPressed()
        );
        
        hud.update(
        car.getSpeed(),   // si no tienes speed, te lo arreglo abajo
        inputHandler.isDrifting(),
        cameraManager.getCameraMode()
);
    }
}