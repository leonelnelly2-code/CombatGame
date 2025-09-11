package com.example.combatgame01;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.input.UserAction;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class CombatGame extends GameApplication {

    private Entity player;
    private final double PLAYER_SPEED = 5;
    private final double PROJECTILE_SPEED = 10;
    private final double ENEMY_SPEED = 2;

    private double screenWidth;
    private double screenHeight;

    private double dirX = 1;
    private double dirY = 0;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(800);
        settings.setHeight(600);
        settings.setTitle("Combat Game");
        settings.setVersion("1.0");

        screenWidth = settings.getWidth();
        screenHeight = settings.getHeight();
    }

    @Override
    protected void initGame() {
        // Player entity
        player = FXGL.entityBuilder()
                .at(screenWidth / 2, screenHeight / 2)
                .type(EntityType.PLAYER)
                .view(new Rectangle(40, 40, Color.BLUE))
                .buildAndAttach();

        // Spawn some enemies
        spawnEnemy(100, 100);
        spawnEnemy(700, 500);
        spawnEnemy(400, 100);
    }

    @Override
    protected void initInput() {
        FXGL.getInput().addAction(new UserAction("Move Up") {
            @Override
            protected void onAction() {
                player.translateY(-PLAYER_SPEED);
                dirX = 0; dirY = -1;
                clampPlayer();
            }
        }, KeyCode.W);

        FXGL.getInput().addAction(new UserAction("Move Down") {
            @Override
            protected void onAction() {
                player.translateY(PLAYER_SPEED);
                dirX = 0; dirY = 1;
                clampPlayer();
            }
        }, KeyCode.S);

        FXGL.getInput().addAction(new UserAction("Move Left") {
            @Override
            protected void onAction() {
                player.translateX(-PLAYER_SPEED);
                dirX = -1; dirY = 0;
                clampPlayer();
            }
        }, KeyCode.A);

        FXGL.getInput().addAction(new UserAction("Move Right") {
            @Override
            protected void onAction() {
                player.translateX(PLAYER_SPEED);
                dirX = 1; dirY = 0;
                clampPlayer();
            }
        }, KeyCode.D);

        FXGL.getInput().addAction(new UserAction("Shoot") {
            @Override
            protected void onActionBegin() {
                spawnProjectile(dirX, dirY);
            }
        }, KeyCode.SPACE);
    }

    private void spawnProjectile(double dx, double dy) {
        FXGL.entityBuilder()
                .at(player.getX() + 20, player.getY() + 18)
                .type(EntityType.PROJECTILE)
                .view(new Rectangle(10, 4, Color.RED))
                .with(new Component() {
                    @Override
                    public void onUpdate(double tpf) {
                        getEntity().translateX(PROJECTILE_SPEED * dx);
                        getEntity().translateY(PROJECTILE_SPEED * dy);

                        // Remove if off-screen
                        if (getEntity().getX() < 0 || getEntity().getX() > screenWidth
                                || getEntity().getY() < 0 || getEntity().getY() > screenHeight) {
                            getEntity().removeFromWorld();
                        }

                        // Check collisions with enemies
                        FXGL.getGameWorld().getEntitiesByType(EntityType.ENEMY)
                                .forEach(enemy -> {
                                    if (getEntity().isColliding(enemy)) {
                                        enemy.removeFromWorld();
                                        getEntity().removeFromWorld();
                                    }
                                });
                    }
                })
                .buildAndAttach();
    }

    private void spawnEnemy(double x, double y) {
        FXGL.entityBuilder()
                .at(x, y)
                .type(EntityType.ENEMY)
                .view(new Rectangle(40, 40, Color.GREEN))
                .with(new Component() {
                    @Override
                    public void onUpdate(double tpf) {
                        double dx = player.getX() - getEntity().getX();
                        double dy = player.getY() - getEntity().getY();
                        double dist = Math.sqrt(dx*dx + dy*dy);
                        if (dist > 0) {
                            getEntity().translateX(ENEMY_SPEED * dx / dist);
                            getEntity().translateY(ENEMY_SPEED * dy / dist);
                        }
                    }
                })
                .buildAndAttach();
    }

    @Override
    protected void onUpdate(double tpf) {
        // Game over check
        FXGL.getGameWorld().getEntitiesByType(EntityType.ENEMY)
                .forEach(enemy -> {
                    if (enemy.isColliding(player)) {
                        FXGL.getDialogService().showMessageBox("Game Over!");
                        Platform.exit();
                    }
                });
    }

    private void clampPlayer() {
        if (player.getX() < 0) player.setX(0);
        if (player.getY() < 0) player.setY(0);
        if (player.getX() + 40 > screenWidth) player.setX(screenWidth - 40);
        if (player.getY() + 40 > screenHeight) player.setY(screenHeight - 40);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
