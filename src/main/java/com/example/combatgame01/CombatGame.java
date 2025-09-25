package com.example.combatgame01;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;

public class CombatGame extends GameApplication {

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(800);
        settings.setHeight(600);
        settings.setTitle("Combat Game");
        settings.setVersion("1.0");
        settings.setManualResizeEnabled(true);
        settings.setFullScreenAllowed(true);
        settings.setMainMenuEnabled(true);
        settings.setGameMenuEnabled(true);
        


        }


    public static void main(String[] args) {
        launch(args); // this comes from FXGL
    }
}
