package com.mygame;

import com.jme3.app.SimpleApplication;
import com.mygame.states.PlayState;
import com.mygame.ui.MainMenuState;

public class Main extends SimpleApplication {

    public static void main(String[] args) {
        new Main().start();
    }

    @Override
    public void simpleInitApp() {

        // 🚀 SOLO MENÚ INICIA TODO
        stateManager.attach(new MainMenuState());
    }

    @Override
    public void simpleUpdate(float tpf) {
        // ❌ aquí NO debe haber gameplay
    }
}