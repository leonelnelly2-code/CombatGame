package com.example.combatgame01;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;

import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class CombatGame extends GameApplication {

    private Entity player;
    private Text healthText, scoreText, ammoText, waveText;
    private Rectangle healthBar;
    private StackPane startMenu, gameOverMenu;
    
    private final double PLAYER_SPEED = 5;
    private final int ENEMIES_PER_WAVE = 3;
    
    private double screenWidth, screenHeight;
    private Point2D playerDirection = new Point2D(1, 0);
    private Random random = new Random();
    
    private int currentWave = 1;
    private int enemiesRemaining = 0;
    private boolean isGameActive = false;
    private boolean isSpawningWave = false;
    
    // Add firing cooldown variables
    private boolean canShoot = true;
    private final double SHOOT_COOLDOWN = 0.3; // seconds
    private double timeSinceLastShot = 0;
    
    private PlayerStats stats = new PlayerStats();

    public enum EntityType {
        PLAYER, ENEMY, PROJECTILE
    }

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1000);
        settings.setHeight(700);
        settings.setTitle("Combat Arena");
        settings.setVersion("1.0");
        settings.setMainMenuEnabled(false);

        screenWidth = settings.getWidth();
        screenHeight = settings.getHeight();
    }

    @Override
    protected void initGame() {
        // Clear existing entities
        FXGL.getGameWorld().getEntities().forEach(Entity::removeFromWorld);
        
        // Reset game state
        stats.reset();
        currentWave = 1;
        enemiesRemaining = 0;
        isGameActive = false;
        isSpawningWave = false;
        canShoot = true;
        timeSinceLastShot = 0;
        playerDirection = new Point2D(1, 0); // Reset direction
        
        // Create player
        player = FXGL.entityBuilder()
                .at(screenWidth / 2, screenHeight / 2)
                .type(EntityType.PLAYER)
                .viewWithBBox(new Rectangle(40, 40, Color.BLUE))
                .collidable()
                .buildAndAttach();

        setupUI();
        showStartMenu();
    }

    private void setupUI() {
        // Clear previous UI by removing specific nodes
        try {
            if (healthBar != null) FXGL.getGameScene().removeUINode(healthBar);
            if (healthText != null) FXGL.getGameScene().removeUINode(healthText);
            if (ammoText != null) FXGL.getGameScene().removeUINode(ammoText);
            if (scoreText != null) FXGL.getGameScene().removeUINode(scoreText);
            if (waveText != null) FXGL.getGameScene().removeUINode(waveText);
            if (startMenu != null) FXGL.getGameScene().removeUINode(startMenu);
            if (gameOverMenu != null) FXGL.getGameScene().removeUINode(gameOverMenu);
        } catch (Exception e) {
            // Ignore errors
        }

        // Health bar with background
        Rectangle healthBg = new Rectangle(204, 24, Color.gray(0.3));
        healthBg.setTranslateX(10);
        healthBg.setTranslateY(10);
        
        healthBar = new Rectangle(200, 20, Color.RED);
        healthBar.setTranslateX(12);
        healthBar.setTranslateY(12);
        
        healthText = new Text("Health: 100/100");
        healthText.setTranslateX(220);
        healthText.setTranslateY(25);
        healthText.setFill(Color.WHITE);
        healthText.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        // Ammo text
        ammoText = new Text("Ammo: 30/30");
        ammoText.setTranslateX(10);
        ammoText.setTranslateY(40);
        ammoText.setFill(Color.BLUE);
        ammoText.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        // Score and wave info
        scoreText = new Text("Score: 0");
        scoreText.setTranslateX(10);
        scoreText.setTranslateY(60);
        scoreText.setFill(Color.GOLD);
        scoreText.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        waveText = new Text("Wave: 1");
        waveText.setTranslateX(10);
        waveText.setTranslateY(80);
        waveText.setFill(Color.LIGHTGREEN);
        waveText.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        // Add UI elements
        FXGL.addUINode(healthBg);
        FXGL.addUINode(healthBar);
        FXGL.addUINode(healthText);
        FXGL.addUINode(ammoText);
        FXGL.addUINode(scoreText);
        FXGL.addUINode(waveText);
    }

    private void showStartMenu() {
        Button startBtn = new Button("Start Game");
        Button quitBtn = new Button("Quit");

        // Style buttons
        startBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 18px; -fx-padding: 10 20;");
        quitBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 18px; -fx-padding: 10 20;");

        VBox menuBox = new VBox(20, startBtn, quitBtn);
        menuBox.setStyle("-fx-background-color: rgba(0,0,0,0.8); -fx-padding: 40; -fx-alignment: center;");
        menuBox.setPrefSize(300, 200);
        
        startMenu = new StackPane(menuBox);
        startMenu.setPrefSize(screenWidth, screenHeight);
        FXGL.addUINode(startMenu);
        
        startBtn.setOnAction(e -> {
            FXGL.getGameScene().removeUINode(startMenu);
            isGameActive = true;
            startWave();
        });
        quitBtn.setOnAction(e -> Platform.exit());
    }

    private void startWave() {
        if (isSpawningWave) return;
        
        isSpawningWave = true;
        enemiesRemaining = ENEMIES_PER_WAVE + (currentWave - 1);
        updateWaveText();
        
        for (int i = 0; i < enemiesRemaining; i++) {
            spawnEnemy();
        }
        
        FXGL.getNotificationService().pushNotification("Wave " + currentWave + " started! Enemies: " + enemiesRemaining);
        isSpawningWave = false;
    }

    private void startNextWave() {
        isSpawningWave = true;
        currentWave++;
        
        // Use timer to start next wave with a delay
        FXGL.getGameTimer().runOnceAfter(() -> {
            enemiesRemaining = ENEMIES_PER_WAVE + (currentWave - 1);
            updateWaveText();
            
            for (int i = 0; i < enemiesRemaining; i++) {
                spawnEnemy();
            }
            
            FXGL.getNotificationService().pushNotification("Wave " + currentWave + " started! Enemies: " + enemiesRemaining);
            isSpawningWave = false;
        }, Duration.seconds(2));
    }

    private void spawnEnemy() {
    double x = random.nextDouble() * (screenWidth - 80) + 40;
    double y = random.nextDouble() * (screenHeight - 80) + 40;
        
    while (Math.abs(x - player.getX()) < 100 && Math.abs(y - player.getY()) < 100) {
        x = random.nextDouble() * (screenWidth - 80) + 40;
        y = random.nextDouble() * (screenHeight - 80) + 40;
    }
    
    // Different sprites based on wave number
    String enemySprite;
    if (currentWave <= 3) {
        // Basic enemies for early waves
        String[] basicSprites = {"A1.png"};
        enemySprite = basicSprites[random.nextInt(basicSprites.length)];
    } else if (currentWave <= 6) {
        // Medium enemies
        String[] mediumSprites = {"enemy3.png", "enemy4.png"};
        enemySprite = mediumSprites[random.nextInt(mediumSprites.length)];
    } else {
        // Advanced enemies for later waves
        String[] advancedSprites = {"enemy5.png", "enemy6.png", "enemy_boss.png"};
        enemySprite = advancedSprites[random.nextInt(advancedSprites.length)];
    }
    
    Entity enemy = FXGL.entityBuilder()
        .at(x, y)
        .type(EntityType.ENEMY)
        .viewWithBBox(enemySprite)
        .collidable()
        .buildAndAttach();
        
    // Movement component with speed based on wave
    enemy.addComponent(new Component() {
        private double speed = 1.0 + (random.nextDouble() * 0.5) + (currentWave * 0.1);
        
        @Override
        public void onUpdate(double tpf) {
            if (player != null && isGameActive) {
                Point2D direction = player.getPosition().subtract(entity.getPosition()).normalize();
                if (direction != null) {
                    entity.translateX(direction.getX() * speed);
                    entity.translateY(direction.getY() * speed);
                }
            }
        }
    });
}

    @Override
    protected void initInput() {
        // Movement
        FXGL.onKey(KeyCode.W, () -> movePlayer(0, -1));
        FXGL.onKey(KeyCode.S, () -> movePlayer(0, 1));
        FXGL.onKey(KeyCode.A, () -> movePlayer(-1, 0));
        FXGL.onKey(KeyCode.D, () -> movePlayer(1, 0));
        
        // Shooting - use onKeyDown for single press detection
        FXGL.onKeyDown(KeyCode.SPACE, () -> {
            if (isGameActive && stats.getAmmo() > 0 && canShoot) {
                spawnProjectile();
                stats.useAmmo(1);
                canShoot = false;
                timeSinceLastShot = 0;
            }
        });
        
        // Restart game
        FXGL.onKeyDown(KeyCode.R, () -> {
            if (!isGameActive) {
                initGame();
            }
        });
    }

    private void movePlayer(int dx, int dy) {
        if (!isGameActive || player == null) return;
        
        player.translateX(PLAYER_SPEED * dx);
        player.translateY(PLAYER_SPEED * dy);
        clampPlayer();
        
        if (dx != 0 || dy != 0) {
            Point2D newDirection = new Point2D(dx, dy).normalize();
            if (newDirection != null) {
                playerDirection = newDirection;
            }
        }
    }

    private void spawnProjectile() {
        if (player == null) return;
        
        // Capture the direction at the moment of shooting
        Point2D shootDirection = playerDirection.normalize();
        
        Entity projectile = FXGL.entityBuilder()
            .at(player.getX() + 20, player.getY() + 18)
            .type(EntityType.PROJECTILE)
            .viewWithBBox(new Rectangle(10, 5, Color.RED))
            .collidable()
            .buildAndAttach();
            
        projectile.addComponent(new Component() {
            private double speed = 15;
            private Point2D direction = shootDirection; // Use captured direction
            
            @Override
            public void onUpdate(double tpf) {
                // Move projectile with its own direction
                entity.translateX(direction.getX() * speed);
                entity.translateY(direction.getY() * speed);
                
                // Remove if off-screen
                if (entity.getX() < -50 || entity.getX() > screenWidth + 50 ||
                    entity.getY() < -50 || entity.getY() > screenHeight + 50) {
                    entity.removeFromWorld();
                    return;
                }
                
                // Check collisions with enemies - use a list to avoid concurrent modification
                List<Entity> enemiesToRemove = new ArrayList<>();
                List<Entity> projectilesToRemove = new ArrayList<>();
                
                FXGL.getGameWorld().getEntitiesByType(EntityType.ENEMY).forEach(enemy -> {
                    if (entity.getBoundingBoxComponent().isCollidingWith(enemy.getBoundingBoxComponent())) {
                        enemiesToRemove.add(enemy);
                        projectilesToRemove.add(entity);
                    }
                });
                
                // Remove entities after iteration
                for (Entity enemy : enemiesToRemove) {
                    enemy.removeFromWorld();
                    stats.addScore(10);
                    enemiesRemaining--;
                    updateWaveText();
                    
                    // Chance to drop ammo
                    if (random.nextDouble() < 0.2) {
                        stats.addAmmo(5);
                    }
                }
                
                for (Entity proj : projectilesToRemove) {
                    proj.removeFromWorld();
                }
            }
        });
    }

    @Override
    protected void onUpdate(double tpf) {
        if (!isGameActive || player == null) return;
        
        // Update shooting cooldown
        if (!canShoot) {
            timeSinceLastShot += tpf;
            if (timeSinceLastShot >= SHOOT_COOLDOWN) {
                canShoot = true;
            }
        }
        
        checkCollisions();
        updateUI();
        
        // Update enemy count from actual entities in the world
        int actualEnemyCount = FXGL.getGameWorld().getEntitiesByType(EntityType.ENEMY).size();
        if (actualEnemyCount != enemiesRemaining) {
            enemiesRemaining = actualEnemyCount;
            updateWaveText();
        }
        
        // Start next wave if all enemies defeated and we're not already spawning
        if (enemiesRemaining <= 0 && !isSpawningWave && isGameActive) {
            startNextWave();
        }
    }

    private void checkCollisions() {
        if (player == null) return;
        
        // Player-enemy collisions - use a list to avoid concurrent modification
        List<Entity> enemiesToRemove = new ArrayList<>();
        
        FXGL.getGameWorld().getEntitiesByType(EntityType.ENEMY).forEach(enemy -> {
            if (player.isColliding(enemy)) {
                stats.takeDamage(10);
                enemiesToRemove.add(enemy);
                enemiesRemaining--;
                updateWaveText();
                
                if (stats.getHealth() <= 0) {
                    gameOver();
                }
            }
        });
        
        // Remove enemies after iteration
        for (Entity enemy : enemiesToRemove) {
            enemy.removeFromWorld();
        }
    }

    private void updateUI() {
        healthText.setText("Health: " + stats.getHealth() + "/100");
        ammoText.setText("Ammo: " + stats.getAmmo() + "/30");
        scoreText.setText("Score: " + stats.getScore());
        
        double healthPercent = (double) stats.getHealth() / 100;
        healthBar.setWidth(200 * Math.max(0, healthPercent));
        
        // Change health bar color based on health
        if (healthPercent > 0.6) {
            healthBar.setFill(Color.LIMEGREEN);
        } else if (healthPercent > 0.3) {
            healthBar.setFill(Color.ORANGE);
        } else {
            healthBar.setFill(Color.RED);
        }
    }

    private void updateWaveText() {
        waveText.setText("Wave: " + currentWave + " - Enemies: " + Math.max(0, enemiesRemaining));
    }

    private void gameOver() {
        isGameActive = false;
        showGameOverMenu();
    }

    private void showGameOverMenu() {
        Button restartBtn = new Button("Restart Game");
        Button quitBtn = new Button("Quit");

        restartBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 18px; -fx-padding: 10 20;");
        quitBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 18px; -fx-padding: 10 20;");

        Text gameOverText = new Text("Game Over!\nFinal Score: " + stats.getScore() + "\nWave Reached: " + currentWave);
        gameOverText.setFill(Color.RED);
        gameOverText.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-alignment: center;");

        VBox menuBox = new VBox(20, gameOverText, restartBtn, quitBtn);
        menuBox.setStyle("-fx-background-color: rgba(0,0,0,0.9); -fx-padding: 40; -fx-alignment: center;");
        menuBox.setPrefSize(400, 300);
        
        gameOverMenu = new StackPane(menuBox);
        gameOverMenu.setPrefSize(screenWidth, screenHeight);
        FXGL.addUINode(gameOverMenu);
        
        restartBtn.setOnAction(e -> {
            FXGL.getGameScene().removeUINode(gameOverMenu);
            initGame();
        });
        quitBtn.setOnAction(e -> Platform.exit());
    }

    private void clampPlayer() {
        if (player == null) return;
        
        double x = Math.max(0, Math.min(screenWidth - 40, player.getX()));
        double y = Math.max(0, Math.min(screenHeight - 40, player.getY()));
        player.setPosition(x, y);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

class PlayerStats {
    private int health = 100;
    private int ammo = 30;
    private int score = 0;
    
    public void reset() {
        health = 100;
        ammo = 30;
        score = 0;
    }
    
    public void takeDamage(int damage) { 
        health = Math.max(0, health - damage); 
    }
    
    public void useAmmo(int amount) { 
        ammo = Math.max(0, ammo - amount); 
    }
    
    public void addAmmo(int amount) {
        ammo = Math.min(30, ammo + amount);
    }
    
    public void addScore(int points) { 
        score += points; 
    }
    
    public int getHealth() { return health; }
    public int getAmmo() { return ammo; }
    public int getScore() { return score; }
}