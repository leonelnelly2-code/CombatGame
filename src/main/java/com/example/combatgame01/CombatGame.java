package com.example.combatgame01;

import org.jetbrains.annotations.NotNull;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;

public class CombatGame extends GameApplication {

    @Override
    protected void initSettings(@NotNull GameSettings settings) {
        settings.setWidth(800);
        settings.setHeight(600);
        settings.setTitle("Combat Game");
        settings.setVersion("1.0");
        settings.setManualResizeEnabled(true);
        settings.setFullScreenAllowed(true);
        settings.setMainMenuEnabled(true);
        settings.setGameMenuEnabled(true);
        


        }

        public void startGameThread() {
            // Start game logic here
            gameThread = new Thread(this);
            gameThread.start();
        }

        @Override
        public void run() {


            while(gamethread != null) {

            System.out.printl("The loop is running");
            }


            


        }

    public static void main(String[] args) {
        launch(args); // this comes from FXGL
    }
}
