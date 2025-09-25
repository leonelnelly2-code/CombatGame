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

import javafx.scene.text.Text;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class CombatGame extends GameApplication {

    private Entity player;
    private Text healthText;
    private Text scoreText;
    private Rectangle healthBar;
    private Text ammoText;
    private Text comboText;
    private Text streakText;
    private StackPane startMenu;
    private StackPane pauseMenu;
    private StackPane gameOverMenu;
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

    // ...existing code...

        @Override
        protected void initGame() {
            // ...existing code...

            // Player entity
            player = FXGL.entityBuilder()
                    .at(screenWidth / 2, screenHeight / 2)
                    .type(EntityType.PLAYER)
                    .view(new Rectangle(40, 40, Color.BLUE))
                    .with(new PlayerComponent())
                    .buildAndAttach();

            // UI for stats
        healthText = new Text("Health: 100");
        healthText.setTranslateX(20);
        healthText.setTranslateY(25);
        healthBar = new Rectangle(200, 20, Color.RED);
        healthBar.setArcWidth(12);
        healthBar.setArcHeight(12);
        healthBar.setStroke(Color.BLACK);
        healthBar.setStrokeWidth(2);
        healthBar.setTranslateX(10);
        healthBar.setTranslateY(10);
        scoreText = new Text("Score: 0");
        scoreText.setTranslateX(10);
        scoreText.setTranslateY(50);
        scoreText.setFont(javafx.scene.text.Font.font("Impact", javafx.scene.text.FontWeight.BOLD, 18));
        scoreText.setFill(Color.YELLOW);
        ammoText = new Text("Ammo: 30");
        ammoText.setTranslateX(10);
        ammoText.setTranslateY(70);
        ammoText.setFont(javafx.scene.text.Font.font("Orbitron", javafx.scene.text.FontWeight.BOLD, 18));
        ammoText.setFill(Color.CYAN);
        comboText = new Text("Combo: 0");
        comboText.setTranslateX(10);
        comboText.setTranslateY(90);
        comboText.setFont(javafx.scene.text.Font.font("Impact", javafx.scene.text.FontWeight.BOLD, 18));
        comboText.setFill(Color.LIME);
        streakText = new Text("Streak: 0");
        streakText.setTranslateX(10);
        streakText.setTranslateY(110);
        streakText.setFont(javafx.scene.text.Font.font("Impact", javafx.scene.text.FontWeight.BOLD, 18));
        streakText.setFill(Color.ORANGE);
        FXGL.addUINode(healthBar);
        FXGL.addUINode(healthText);
        FXGL.addUINode(scoreText);
        FXGL.addUINode(ammoText);
        FXGL.addUINode(comboText);
        FXGL.addUINode(streakText);

            // Spawn some enemies
            spawnEnemy(100, 100);
            spawnEnemy(700, 500);
            spawnEnemy(400, 100);

            // Show start menu
            showStartMenu();
        }

        private void showStartMenu() {
            Button startBtn = new Button("Start Game");
            Button quitBtn = new Button("Quit");

            // Style start button
            startBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px; -fx-background-radius: 12; -fx-border-radius: 12;");
            startBtn.setOnMouseEntered(e -> startBtn.setStyle("-fx-background-color: #218838; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px; -fx-background-radius: 12; -fx-border-radius: 12;"));
            startBtn.setOnMouseExited(e -> startBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px; -fx-background-radius: 12; -fx-border-radius: 12;"));

            // Style quit button
            quitBtn.setStyle("-fx-background-color: #444; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px; -fx-background-radius: 12; -fx-border-radius: 12;");
            quitBtn.setOnMouseEntered(e -> quitBtn.setStyle("-fx-background-color: #222; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px; -fx-background-radius: 12; -fx-border-radius: 12;"));
            quitBtn.setOnMouseExited(e -> quitBtn.setStyle("-fx-background-color: #444; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px; -fx-background-radius: 12; -fx-border-radius: 12;"));

            VBox menuBox = new VBox(20, startBtn, quitBtn);
            menuBox.setTranslateX(300);
            menuBox.setTranslateY(200);
            startMenu = new StackPane(menuBox);
            FXGL.addUINode(startMenu);
            startBtn.setOnAction(e -> {
                FXGL.removeUINode(startMenu);
            });
            quitBtn.setOnAction(e -> Platform.exit());
        }

    private void showPauseMenu() {
        Button resumeBtn = new Button("Resume");
        Button mainMenuBtn = new Button("Main Menu");
        Button quitBtn = new Button("Quit");
        VBox menuBox = new VBox(20, resumeBtn, mainMenuBtn, quitBtn);
        menuBox.setTranslateX(300);
        menuBox.setTranslateY(200);
        pauseMenu = new StackPane(menuBox);
        FXGL.addUINode(pauseMenu);
        resumeBtn.setOnAction(e -> FXGL.removeUINode(pauseMenu));
        mainMenuBtn.setOnAction(e -> {
            FXGL.removeUINode(pauseMenu);
            resetToMainMenu();
        });
        quitBtn.setOnAction(e -> Platform.exit());
    }

    private void showGameOverMenu() {
        Button restartBtn = new Button("Restart");
        Button mainMenuBtn = new Button("Main Menu");
        Button quitBtn = new Button("Quit");
        VBox menuBox = new VBox(20, restartBtn, mainMenuBtn, quitBtn);
        menuBox.setTranslateX(300);
        menuBox.setTranslateY(200);
        gameOverMenu = new StackPane(menuBox);
        FXGL.addUINode(gameOverMenu);
        restartBtn.setOnAction(e -> restartGame());
        mainMenuBtn.setOnAction(e -> {
            FXGL.removeUINode(gameOverMenu);
            resetToMainMenu();
        });
        quitBtn.setOnAction(e -> Platform.exit());
    }
    // Helper to reset game state and show main menu
    private void resetToMainMenu() {
        FXGL.getGameWorld().getEntities().forEach(Entity::removeFromWorld);
        showStartMenu();
    }

    private void restartGame() {
        FXGL.getGameWorld().getEntities().forEach(Entity::removeFromWorld);
        initGame();
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

        // Pause functionality (ESC key)
        FXGL.getInput().addAction(new UserAction("Pause Game") {
            @Override
            protected void onActionBegin() {
                showPauseMenu();
            }
        }, KeyCode.ESCAPE);
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
                                        // Increment combo and streak on kill
                                        PlayerComponent pc = player.getComponent(PlayerComponent.class);
                                        pc.getStats().incrementCombo();
                                        pc.getStats().incrementStreak();
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
        PlayerComponent pc = player.getComponent(PlayerComponent.class);
        FXGL.getGameWorld().getEntitiesByType(EntityType.ENEMY)
                .forEach(enemy -> {
                    if (enemy.isColliding(player)) {
                        showGameOverMenu();
                        // Reset streak on death
                        pc.getStats().resetStreak();
                        pc.getStats().resetCombo();
                    }
                });

        // Update UI stats
        healthText.setText("Health: " + pc.getStats().getHealth());
        scoreText.setText("Score: " + pc.getStats().getScore());
        ammoText.setText("Ammo: " + pc.getStats().getAmmo());
        comboText.setText("Combo: " + pc.getStats().getCombo());
        streakText.setText("Streak: " + pc.getStats().getStreak());
        double healthPercent = (double) pc.getStats().getHealth() / pc.getStats().getMaxHealth();
        healthBar.setWidth(200 * healthPercent);
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
