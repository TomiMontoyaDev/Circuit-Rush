package com.mygame.ui;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.*;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.util.SkyFactory;
import com.mygame.states.PlayState;

public class MainMenuState extends BaseAppState implements ActionListener {

    private Node guiNode;
    private Node rootNode;
    private Camera cam;
    private AssetManager assetManager;

    private Spatial car;

    private BitmapText title;
    private BitmapText[] options;

    private final String[] labels = {"JUGAR", "SALIR"};
    private int selectedIndex = 0;

    private float angle = 0f;

    @Override
    protected void initialize(Application app) {

        SimpleApplication sa = (SimpleApplication) app;

        guiNode = sa.getGuiNode();
        rootNode = sa.getRootNode();
        cam = sa.getCamera();
        assetManager = app.getAssetManager();

        // 🌤 SKY
        Spatial sky = SkyFactory.createSky(
                assetManager,
                "Textures/Sky/sky.hdr",
                SkyFactory.EnvMapType.EquirectMap
        );
        rootNode.attachChild(sky);

        // 💡 LUCES
        AmbientLight al = new AmbientLight();
        al.setColor(ColorRGBA.White.mult(1.2f));
        rootNode.addLight(al);

        DirectionalLight dl = new DirectionalLight();
        dl.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
        dl.setColor(ColorRGBA.White);
        rootNode.addLight(dl);

        // 🏎 CARRO
        car = assetManager.loadModel("Models/Convertible.glb");
        car.setLocalTranslation(0, 0, 0);
        rootNode.attachChild(car);

        // 🎨 UI
        BitmapFont font = assetManager.loadFont("Interface/Fonts/Default.fnt");

        title = new BitmapText(font);
        title.setText("CIRCUIT RUSH");
        title.setSize(70);
        title.setLocalTranslation(200, 550, 0);
        guiNode.attachChild(title);
        BitmapText authors = new BitmapText(font);
        authors.setText("Tomás Montoya - Michael Naranjo");
        authors.setSize(20);
        authors.setColor(ColorRGBA.LightGray);
        authors.setLocalTranslation(200, 450, 0);
        guiNode.attachChild(authors);

        options = new BitmapText[labels.length];

        for (int i = 0; i < labels.length; i++) {
            BitmapText txt = new BitmapText(font);
            txt.setText(labels[i]);
            txt.setSize(40);
            txt.setLocalTranslation(250, 350 - (i * 60), 0);
            options[i] = txt;
            guiNode.attachChild(txt);
        }

        updateSelection();

        // 🎮 INPUT
        app.getInputManager().addMapping("UP", new KeyTrigger(KeyInput.KEY_UP));
        app.getInputManager().addMapping("DOWN", new KeyTrigger(KeyInput.KEY_DOWN));
        app.getInputManager().addMapping("SELECT", new KeyTrigger(KeyInput.KEY_RETURN));
        app.getInputManager().addMapping("CLICK", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));

        app.getInputManager().addListener(this, "UP", "DOWN", "SELECT", "CLICK");

        // 🎥 cámara inicial
        cam.setLocation(new Vector3f(5, 3, 8));
        cam.lookAt(car.getLocalTranslation(), Vector3f.UNIT_Y);
    }

    @Override
    public void update(float tpf) {

        // 🏎 animación carro
        angle += tpf * 0.6f;

        float x = FastMath.sin(angle) * 5f;
        float z = FastMath.cos(angle) * 5f;

        car.setLocalTranslation(x, -1, z);
        car.rotate(0, tpf, 0);

        // 🎥 cámara cinemática
        Vector3f camPos = new Vector3f(x + 9, 4, z + 9);
        cam.setLocation(camPos);
        cam.lookAt(car.getLocalTranslation(), Vector3f.UNIT_Y);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {

        if (!isPressed) return;

        switch (name) {

            case "UP":
                selectedIndex--;
                if (selectedIndex < 0) selectedIndex = labels.length - 1;
                updateSelection();
                break;

            case "DOWN":
                selectedIndex++;
                if (selectedIndex >= labels.length) selectedIndex = 0;
                updateSelection();
                break;

            case "SELECT":
            case "CLICK":
                execute();
                break;
        }
    }

    private void execute() {

        switch (selectedIndex) {

            case 0:
                getStateManager().attach(new PlayState());
                getStateManager().detach(this);
                break;

            case 1:
                System.exit(0);
                break;
        }
    }

    private void updateSelection() {

        for (int i = 0; i < options.length; i++) {
            options[i].setColor(
                    (i == selectedIndex)
                            ? ColorRGBA.Yellow
                            : ColorRGBA.White
            );
        }
    }

    @Override
    protected void cleanup(Application app) {

        guiNode.detachChild(title);

        for (BitmapText t : options) {
            guiNode.detachChild(t);
        }

        rootNode.detachChild(car);

        app.getInputManager().deleteMapping("UP");
        app.getInputManager().deleteMapping("DOWN");
        app.getInputManager().deleteMapping("SELECT");
        app.getInputManager().deleteMapping("CLICK");

        app.getInputManager().removeListener(this);
    }

    @Override protected void onEnable() {}
    @Override protected void onDisable() {}
}