package com.mygame.states;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;

/**
 * Base para todos los estados del juego (Menu, Play, Pause, etc.)
 */
public abstract class GameBaseState extends BaseAppState {

    protected Application app;

    @Override
    protected void initialize(Application app) {
        this.app = app;
        onInit(app);
    }

    @Override
    protected void onEnable() {
        onStateEnable();
    }

    @Override
    protected void onDisable() {
        onStateDisable();
    }

    @Override
    protected void cleanup(Application app) {
        onCleanup(app);
    }

    // 🔧 Métodos que tú usas en tus states
    protected abstract void onInit(Application app);

    protected abstract void onStateEnable();

    protected abstract void onStateDisable();

    protected abstract void onCleanup(Application app);
}