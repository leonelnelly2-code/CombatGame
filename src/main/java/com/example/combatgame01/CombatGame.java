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
import javafx.scene.control.ComboBox;
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
    private StackPane startMenu, gameOverMenu, spriteMenu;
    
    private final double PLAYER_SPEED = 5;
    private final int ENEMIES_PER_WAVE = 3;
    
    private double screenWidth, screenHeight;
    private Point2D playerDirection = new Point2D(1, 0);
    private Random random = new Random();
    
    private int currentWave = 1;
    private int enemiesRemaining = 0;
    private boolean isGameActive = false;
    private boolean isSpawningWave = false;
    
    private boolean canShoot = true;
    private final double SHOOT_COOLDOWN = 0.3;
    private double timeSinceLastShot = 0;
    
    private PlayerStats stats = new PlayerStats();
    
    // Custom sprite configuration
    private String[] playerSprites = {"player.png", "player2.png", "player3.png"};
    private int currentPlayerSpriteIndex = 0;
    private double animationTimer = 0;
    private final double ANIMATION_SPEED = 0.1; // Time between sprite changes
    
    private String projectileSprite = "bullet.png";
    private String[] basicEnemySprites = {"A1.png", "A2.png", "A3.png"};
    private String[] mediumEnemySprites = {"B1.png", "B2.png", "B3.png"};
    private String[] advancedEnemySprites = {"C1.png", "gun.png"};
    
    // Track player state for sprite direction
    private boolean isFacingRight = true;
    private boolean isMoving = false;

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
        System.out.println("=== Initializing Game ===");
        
        // Use a safer way to clear entities to avoid ConcurrentModificationException
        List<Entity> entitiesToRemove = new ArrayList<>(FXGL.getGameWorld().getEntities());
        for (Entity entity : entitiesToRemove) {
            entity.removeFromWorld();
        }
        
        stats.reset();
        currentWave = 1;
        enemiesRemaining = 0;
        isGameActive = false;
        isSpawningWave = false;
        canShoot = true;
        timeSinceLastShot = 0;
        playerDirection = new Point2D(1, 0);
        isFacingRight = true;
        isMoving = false;
        currentPlayerSpriteIndex = 0;
        animationTimer = 0;
        
        // Create player
        createPlayer();
        setupUI();
        showStartMenu();
        
        System.out.println("=== Game Initialized ===");
    }

    private void createPlayer() {
        try {
            // Load initial player sprite and scale it 2x larger
            var texture = FXGL.texture(playerSprites[currentPlayerSpriteIndex]);
            texture.setScaleX(2.0);
            texture.setScaleY(2.0);
            
            player = FXGL.entityBuilder()
                    .at(screenWidth / 2, screenHeight / 2)
                    .type(EntityType.PLAYER)
                    .viewWithBBox(texture)
                    .collidable()
                    .buildAndAttach();
            System.out.println("✓ Player sprite loaded: " + playerSprites[currentPlayerSpriteIndex]);
        } catch (Exception e) {
            System.out.println("✗ Player sprite failed: " + playerSprites[currentPlayerSpriteIndex]);
            System.out.println("  Error: " + e.getMessage());
            // Fallback to colored rectangle (also 2x larger)
            Rectangle playerRect = new Rectangle(80, 80, Color.BLUE); // 2x larger
            player = FXGL.entityBuilder()
                    .at(screenWidth / 2, screenHeight / 2)
                    .type(EntityType.PLAYER)
                    .viewWithBBox(playerRect)
                    .collidable()
                    .buildAndAttach();
            System.out.println("✓ Using fallback player (blue rectangle)");
        }
    }

    private void updatePlayerSprite() {
        if (player != null && player.getViewComponent().getChildren().size() > 0) {
            try {
                var texture = FXGL.texture(playerSprites[currentPlayerSpriteIndex]);
                // Scale the new sprite 2x larger
                texture.setScaleX(2.0);
                texture.setScaleY(2.0);
                // Apply current flip based on direction
                if (!isFacingRight) {
                    texture.setScaleX(2.0); // Flip horizontally for left movement
                }else{
                    texture.setScaleX(-2.0); // Normal scale for right movement
                }
                
                player.getViewComponent().clearChildren();
                player.getViewComponent().addChild(texture);
            } catch (Exception e) {
                System.out.println("✗ Failed to update player animation sprite");
            }
        }
    }

    private void animatePlayerMovement(double tpf) {
        if (isMoving) {
            animationTimer += tpf;
            if (animationTimer >= ANIMATION_SPEED) {
                animationTimer = 0;
                currentPlayerSpriteIndex = (currentPlayerSpriteIndex + 1) % playerSprites.length;
                updatePlayerSprite();
            }
        } else {
            // Reset to first sprite when not moving
            if (currentPlayerSpriteIndex != 0) {
                currentPlayerSpriteIndex = 0;
                updatePlayerSprite();
            }
        }
    }
    
    public void setProjectileSprite(String spritePath) {
        this.projectileSprite = spritePath;
        System.out.println("Projectile sprite set to: " + spritePath);
    }
    
    public void setBasicEnemySprites(String[] spritePaths) {
        this.basicEnemySprites = spritePaths;
        System.out.println("Basic enemy sprites updated");
    }

    private void setupUI() {
        try {
            if (healthBar != null) FXGL.getGameScene().removeUINode(healthBar);
            if (healthText != null) FXGL.getGameScene().removeUINode(healthText);
            if (ammoText != null) FXGL.getGameScene().removeUINode(ammoText);
            if (scoreText != null) FXGL.getGameScene().removeUINode(scoreText);
            if (waveText != null) FXGL.getGameScene().removeUINode(waveText);
            if (startMenu != null) FXGL.getGameScene().removeUINode(startMenu);
            if (gameOverMenu != null) FXGL.getGameScene().removeUINode(gameOverMenu);
            if (spriteMenu != null) FXGL.getGameScene().removeUINode(spriteMenu);
        } catch (Exception e) {
            // Ignore cleanup errors
        }

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

        ammoText = new Text("Ammo: 30/30");
        ammoText.setTranslateX(10);
        ammoText.setTranslateY(40);
        ammoText.setFill(Color.BLUE);
        ammoText.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

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
        Button customSpritesBtn = new Button("Customize Sprites");

        startBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 18px; -fx-padding: 10 20;");
        quitBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-size: 18px; -fx-padding: 10 20;");
        customSpritesBtn.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-size: 18px; -fx-padding: 10 20;");

        VBox menuBox = new VBox(20, startBtn, customSpritesBtn, quitBtn);
        menuBox.setStyle("-fx-background-color: rgba(0,0,0,0.8); -fx-padding: 40; -fx-alignment: center;");
        menuBox.setPrefSize(300, 250);
        
        startMenu = new StackPane(menuBox);
        startMenu.setPrefSize(screenWidth, screenHeight);
        FXGL.addUINode(startMenu);
        
        startBtn.setOnAction(e -> {
            FXGL.getGameScene().removeUINode(startMenu);
            isGameActive = true;
            startWave();
        });
        
        customSpritesBtn.setOnAction(e -> showSpriteCustomizationMenu());
        quitBtn.setOnAction(e -> Platform.exit());
    }

    private void showSpriteCustomizationMenu() {
        // Projectile sprite selection
        Text projectileText = new Text("Select Projectile Sprite:");
        projectileText.setFill(Color.WHITE);
        projectileText.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        
        ComboBox<String> projectileComboBox = new ComboBox<>();
        projectileComboBox.getItems().addAll("bullet.png", "A-attck.png", "B-attck.png");
        projectileComboBox.setValue(projectileSprite);
        projectileComboBox.setStyle("-fx-font-size: 14; -fx-pref-width: 200;");
        
        // Basic enemy sprites selection
        Text basicEnemyText = new Text("Select Basic Enemy Sprites (Wave 1-3):");
        basicEnemyText.setFill(Color.WHITE);
        basicEnemyText.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        
        ComboBox<String> basicEnemyComboBox = new ComboBox<>();
        basicEnemyComboBox.getItems().addAll("A1.png,A2.png,A3.png", "B1.png,B2.png,B3.png", "C1.png,gun.png");
        basicEnemyComboBox.setValue(String.join(",", basicEnemySprites));
        basicEnemyComboBox.setStyle("-fx-font-size: 14; -fx-pref-width: 200;");
        
        // Buttons
        Button applyBtn = new Button("Apply Changes");
        Button backBtn = new Button("Back to Main Menu");
        
        applyBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 16;");
        backBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 16;");

        VBox menuBox = new VBox(15, projectileText, projectileComboBox, 
                               basicEnemyText, basicEnemyComboBox, applyBtn, backBtn);
        menuBox.setStyle("-fx-background-color: rgba(0,0,0,0.9); -fx-padding: 30; -fx-alignment: center;");
        menuBox.setPrefSize(400, 350);
        
        spriteMenu = new StackPane(menuBox);
        spriteMenu.setPrefSize(screenWidth, screenHeight);
        FXGL.addUINode(spriteMenu);
        
        applyBtn.setOnAction(e -> {
            // Apply sprite changes
            setProjectileSprite(projectileComboBox.getValue());
            
            String[] basicSprites = basicEnemyComboBox.getValue().split(",");
            setBasicEnemySprites(basicSprites);
            
            FXGL.getNotificationService().pushNotification("Sprites updated!");
            System.out.println("=== Sprite Configuration Applied ===");
            System.out.println("Projectile: " + projectileComboBox.getValue());
            System.out.println("Basic Enemies: " + String.join(", ", basicSprites));
        });
        
        backBtn.setOnAction(e -> {
            FXGL.getGameScene().removeUINode(spriteMenu);
        });
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
        
        String enemySprite = getEnemySpriteForWave();
        
        try {
            var texture = FXGL.texture(enemySprite);
            // Scale enemy sprites 2x larger
            texture.setScaleX(2.0);
            texture.setScaleY(2.0);
            
            Entity enemy = FXGL.entityBuilder()
                .at(x, y)
                .type(EntityType.ENEMY)
                .viewWithBBox(texture)
                .collidable()
                .buildAndAttach();
                
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
        } catch (Exception e) {
            System.out.println("✗ Failed to spawn enemy with sprite: " + enemySprite);
            // Fallback to red rectangle (2x larger)
            Rectangle enemyRect = new Rectangle(60, 60, Color.RED); // 2x larger
            Entity enemy = FXGL.entityBuilder()
                .at(x, y)
                .type(EntityType.ENEMY)
                .viewWithBBox(enemyRect)
                .collidable()
                .buildAndAttach();
                
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
    }

    private String getEnemySpriteForWave() {
        String[] availableSprites;
        
        if (currentWave <= 3) {
            availableSprites = basicEnemySprites;
        } else if (currentWave <= 6) {
            availableSprites = mediumEnemySprites;
        } else {
            availableSprites = advancedEnemySprites;
        }
        
        // Fallback to basic sprites if the selected array is empty
        if (availableSprites.length == 0) {
            availableSprites = new String[]{"A1.png", "A2.png", "A3.png"};
        }
        
        return availableSprites[random.nextInt(availableSprites.length)];
    }

    @Override
    protected void initInput() {
        FXGL.onKey(KeyCode.W, () -> movePlayer(0, -1));
        FXGL.onKey(KeyCode.S, () -> movePlayer(0, 1));
        FXGL.onKey(KeyCode.A, () -> movePlayer(-1, 0));
        FXGL.onKey(KeyCode.D, () -> movePlayer(1, 0));
        
        FXGL.onKeyDown(KeyCode.SPACE, () -> {
            if (isGameActive && stats.getAmmo() > 0 && canShoot) {
                spawnProjectile();
                stats.useAmmo(1);
                canShoot = false;
                timeSinceLastShot = 0;
            }
        });
        
        FXGL.onKeyDown(KeyCode.R, () -> {
            if (!isGameActive) {
                initGame();
            }
        });
        
        // Removed F1-F3 sprite switching keys
    }

    private void movePlayer(int dx, int dy) {
        if (!isGameActive || player == null) return;
        
        player.translateX(PLAYER_SPEED * dx);
        player.translateY(PLAYER_SPEED * dy);
        clampPlayer();
        
        // Check if player is moving
        isMoving = (dx != 0 || dy != 0);
        
        // Update sprite direction based on horizontal movement
        if (dx < 0 && isFacingRight) {
            // Moving left - flip sprite horizontally
            isFacingRight = false;
            updatePlayerSprite();
        } else if (dx > 0 && !isFacingRight) {
            // Moving right - unflip sprite
            isFacingRight = true;
            updatePlayerSprite();
        }
        
        if (dx != 0 || dy != 0) {
            Point2D newDirection = new Point2D(dx, dy).normalize();
            if (newDirection != null) {
                playerDirection = newDirection;
            }
        }
    }

    private void spawnProjectile() {
        if (player == null) return;
        
        Point2D shootDirection = playerDirection.normalize();
        
        try {
            var texture = FXGL.texture(projectileSprite);
            // Scale projectile sprites 2x larger
            texture.setScaleX(2.0);
            texture.setScaleY(2.0);
            
            // Spawn bullet at a higher position on the player (above center)
            double spawnX = player.getX() + 40; // Center of player (80px wide)
            double spawnY = player.getY() + 20; // Higher position (20px from top of 80px player)
            
            Entity projectile = FXGL.entityBuilder()
                .at(spawnX, spawnY)
                .type(EntityType.PROJECTILE)
                .viewWithBBox(texture)
                .collidable()
                .buildAndAttach();
                
            projectile.addComponent(new Component() {
                private double speed = 15;
                private Point2D direction = shootDirection;
                
                @Override
                public void onUpdate(double tpf) {
                    entity.translateX(direction.getX() * speed);
                    entity.translateY(direction.getY() * speed);
                    
                    if (entity.getX() < -50 || entity.getX() > screenWidth + 50 ||
                        entity.getY() < -50 || entity.getY() > screenHeight + 50) {
                        entity.removeFromWorld();
                        return;
                    }
                    
                    List<Entity> enemiesToRemove = new ArrayList<>();
                    List<Entity> projectilesToRemove = new ArrayList<>();
                    
                    FXGL.getGameWorld().getEntitiesByType(EntityType.ENEMY).forEach(enemy -> {
                        if (entity.getBoundingBoxComponent().isCollidingWith(enemy.getBoundingBoxComponent())) {
                            enemiesToRemove.add(enemy);
                            projectilesToRemove.add(entity);
                        }
                    });
                    
                    for (Entity enemy : enemiesToRemove) {
                        enemy.removeFromWorld();
                        stats.addScore(10);
                        enemiesRemaining--;
                        updateWaveText();
                        
                        if (random.nextDouble() < 0.2) {
                            stats.addAmmo(5);
                        }
                    }
                    
                    for (Entity proj : projectilesToRemove) {
                        proj.removeFromWorld();
                    }
                }
            });
        } catch (Exception e) {
            System.out.println("✗ Failed to spawn projectile with sprite: " + projectileSprite);
            // Fallback to yellow rectangle (2x larger)
            Rectangle projectileRect = new Rectangle(20, 10, Color.YELLOW); // 2x larger
            
            // Spawn bullet at a higher position on the player (above center)
            double spawnX = player.getX() + 40; // Center of player (80px wide)
            double spawnY = player.getY() + 20; // Higher position (20px from top of 80px player)
            
            Entity projectile = FXGL.entityBuilder()
                .at(spawnX, spawnY)
                .type(EntityType.PROJECTILE)
                .viewWithBBox(projectileRect)
                .collidable()
                .buildAndAttach();
                
            projectile.addComponent(new Component() {
                private double speed = 15;
                private Point2D direction = shootDirection;
                
                @Override
                public void onUpdate(double tpf) {
                    entity.translateX(direction.getX() * speed);
                    entity.translateY(direction.getY() * speed);
                    
                    if (entity.getX() < -50 || entity.getX() > screenWidth + 50 ||
                        entity.getY() < -50 || entity.getY() > screenHeight + 50) {
                        entity.removeFromWorld();
                        return;
                    }
                    
                    List<Entity> enemiesToRemove = new ArrayList<>();
                    List<Entity> projectilesToRemove = new ArrayList<>();
                    
                    FXGL.getGameWorld().getEntitiesByType(EntityType.ENEMY).forEach(enemy -> {
                        if (entity.getBoundingBoxComponent().isCollidingWith(enemy.getBoundingBoxComponent())) {
                            enemiesToRemove.add(enemy);
                            projectilesToRemove.add(entity);
                        }
                    });
                    
                    for (Entity enemy : enemiesToRemove) {
                        enemy.removeFromWorld();
                        stats.addScore(10);
                        enemiesRemaining--;
                        updateWaveText();
                        
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
    }

    @Override
    protected void onUpdate(double tpf) {
        if (!isGameActive || player == null) return;
        
        if (!canShoot) {
            timeSinceLastShot += tpf;
            if (timeSinceLastShot >= SHOOT_COOLDOWN) {
                canShoot = true;
            }
        }
        
        // Animate player movement
        animatePlayerMovement(tpf);
        
        checkCollisions();
        updateUI();
        
        int actualEnemyCount = FXGL.getGameWorld().getEntitiesByType(EntityType.ENEMY).size();
        if (actualEnemyCount != enemiesRemaining) {
            enemiesRemaining = actualEnemyCount;
            updateWaveText();
        }
        
        if (enemiesRemaining <= 0 && !isSpawningWave && isGameActive) {
            startNextWave();
        }
    }

    private void checkCollisions() {
        if (player == null) return;
        
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
        
        double x = Math.max(0, Math.min(screenWidth - 80, player.getX())); // Adjusted for larger sprite
        double y = Math.max(0, Math.min(screenHeight - 80, player.getY())); // Adjusted for larger sprite
        player.setPosition(x, y);
    }

    public static void main(String[] args) {
        System.out.println("Starting Combat Game...");
        launch(args);
    }
}