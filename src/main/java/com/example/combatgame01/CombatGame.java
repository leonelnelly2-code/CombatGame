package com.example.combatgame01;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.input.UserAction;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.Random;

public class CombatGame extends GameApplication {

    private Entity player;
    private Text healthText, scoreText, ammoText, waveText;
    private Rectangle healthBar, ammoBar;
    private StackPane startMenu, pauseMenu, gameOverMenu;
    
    // Game constants
    private final double PLAYER_SPEED = 5;
    private final double PROJECTILE_SPEED = 15;
    private final int ENEMIES_PER_WAVE = 5;
    private final double POWERUP_DURATION = 10.0;
    
    private double screenWidth, screenHeight;
    private Point2D mouseDirection = new Point2D(1, 0);
    private Random random = new Random();
    
    // Game state
    private int currentWave = 1;
    private int enemiesRemaining = 0;
    private boolean isGameActive = false;
    private boolean rapidFireActive = false;
    private double powerupTimer = 0;
    
    // Player stats
    private PlayerStats stats = new PlayerStats();

    // Entity types
    public enum EntityType {
        PLAYER, ENEMY, PROJECTILE, POWERUP, AMMO_PACK
    }

    // Powerup types
    public enum PowerupType {
        RAPID_FIRE, HEALTH
    }

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setWidth(1000);
        settings.setHeight(700);
        settings.setTitle("Advanced Combat Arena");
        settings.setVersion("2.0");

        screenWidth = settings.getWidth();
        screenHeight = settings.getHeight();
    }

    @Override
    protected void initGame() {
        if (player != null) {
            FXGL.getGameWorld().getEntities().forEach(Entity::removeFromWorld);
        }
        
        // Reset game state
        stats.reset();
        currentWave = 1;
        enemiesRemaining = 0;
        isGameActive = false;
        rapidFireActive = false;
        powerupTimer = 0;
        
        // Create player
        player = FXGL.entityBuilder()
                .at(screenWidth / 2, screenHeight / 2)
                .type(EntityType.PLAYER)
                .viewWithBBox(new Rectangle(40, 40, Color.BLUE))
                .with(new PlayerComponent())
                .collidable()
                .buildAndAttach();

        setupUI();
        showStartMenu();
    }

    private void setupUI() {
        // Clear existing UI
        try {
            FXGL.getGameScene().clearUINodes();
        } catch (Exception e) {
            // If clearUINodes doesn't exist, we'll remove nodes individually later
        }

        // Health bar with background
        Rectangle healthBg = new Rectangle(204, 24, Color.gray(0.3));
        healthBg.setArcWidth(12); healthBg.setArcHeight(12);
        healthBg.setStroke(Color.BLACK); healthBg.setStrokeWidth(2);
        healthBg.setTranslateX(10); healthBg.setTranslateY(10);
        
        healthBar = new Rectangle(200, 20, Color.RED);
        healthBar.setArcWidth(10); healthBar.setArcHeight(10);
        healthBar.setTranslateX(12); healthBar.setTranslateY(12);
        
        healthText = new Text("Health: 100/100");
        healthText.setTranslateX(220); healthText.setTranslateY(25);
        healthText.setFill(Color.WHITE);
        healthText.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        // Ammo bar
        Rectangle ammoBg = new Rectangle(204, 24, Color.gray(0.3));
        ammoBg.setArcWidth(12); ammoBg.setArcHeight(12);
        ammoBg.setStroke(Color.BLACK); ammoBg.setStrokeWidth(2);
        ammoBg.setTranslateX(10); ammoBg.setTranslateY(40);
        
        ammoBar = new Rectangle(200, 20, Color.CYAN);
        ammoBar.setArcWidth(10); ammoBar.setArcHeight(10);
        ammoBar.setTranslateX(12); ammoBar.setTranslateY(42);
        
        ammoText = new Text("Ammo: 30/30");
        ammoText.setTranslateX(220); ammoText.setTranslateY(55);
        ammoText.setFill(Color.WHITE);
        ammoText.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");

        // Score and wave info
        scoreText = new Text("Score: 0");
        scoreText.setTranslateX(10); scoreText.setTranslateY(80);
        scoreText.setFill(Color.GOLD);
        scoreText.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        waveText = new Text("Wave: 1");
        waveText.setTranslateX(10); waveText.setTranslateY(100);
        waveText.setFill(Color.LIGHTGREEN);
        waveText.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");

        // Add all UI elements
        FXGL.addUINode(healthBg); 
        FXGL.addUINode(healthBar);
        FXGL.addUINode(ammoBg); 
        FXGL.addUINode(ammoBar);
        FXGL.addUINode(healthText); 
        FXGL.addUINode(ammoText);
        FXGL.addUINode(scoreText); 
        FXGL.addUINode(waveText);
    }

    private void showStartMenu() {
        Button startBtn = createStyledButton("Start Game", "#28a745");
        Button quitBtn = createStyledButton("Quit", "#dc3545");

        VBox menuBox = new VBox(20, startBtn, quitBtn);
        menuBox.setStyle("-fx-background-color: rgba(0,0,0,0.8); -fx-padding: 40; -fx-background-radius: 20;");
        menuBox.setTranslateX(screenWidth/2 - 100);
        menuBox.setTranslateY(screenHeight/2 - 60);
        
        startMenu = new StackPane(menuBox);
        FXGL.addUINode(startMenu);
        
        startBtn.setOnAction(e -> {
            FXGL.removeUINode(startMenu);
            isGameActive = true;
            startWave();
        });
        quitBtn.setOnAction(e -> Platform.exit());
    }

    private Button createStyledButton(String text, String baseColor) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: " + baseColor + "; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-font-size: 18px; -fx-background-radius: 12; -fx-border-radius: 12; " +
            "-fx-padding: 15 30; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);"
        );
        
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle().replace(baseColor, darkenColor(baseColor))));
        btn.setOnMouseExited(e -> btn.setStyle(btn.getStyle().replace(darkenColor(baseColor), baseColor)));
        
        return btn;
    }

    private String darkenColor(String color) {
        // Simple color darkening for hover effect
        return color.replace("28a7", "2188").replace("dc35", "c823");
    }

    private void showPauseMenu() {
        Button resumeBtn = createStyledButton("Resume", "#28a745");
        Button quitBtn = createStyledButton("Quit", "#dc3545");

        VBox menuBox = new VBox(20, resumeBtn, quitBtn);
        menuBox.setStyle("-fx-background-color: rgba(0,0,0,0.8); -fx-padding: 40; -fx-background-radius: 20;");
        menuBox.setTranslateX(screenWidth/2 - 100);
        menuBox.setTranslateY(screenHeight/2 - 60);
        
        pauseMenu = new StackPane(menuBox);
        FXGL.addUINode(pauseMenu);
        
        resumeBtn.setOnAction(e -> {
            FXGL.removeUINode(pauseMenu);
            isGameActive = true;
        });
        quitBtn.setOnAction(e -> Platform.exit());
    }

    private void showGameOverMenu() {
        Button restartBtn = createStyledButton("Restart", "#28a745");
        Button quitBtn = createStyledButton("Quit", "#dc3545");

        Text gameOverText = new Text("Game Over!\nFinal Score: " + stats.getScore());
        gameOverText.setFill(Color.WHITE);
        gameOverText.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-alignment: center;");

        VBox menuBox = new VBox(20, gameOverText, restartBtn, quitBtn);
        menuBox.setStyle("-fx-background-color: rgba(0,0,0,0.8); -fx-padding: 40; -fx-background-radius: 20; -fx-alignment: center;");
        menuBox.setTranslateX(screenWidth/2 - 150);
        menuBox.setTranslateY(screenHeight/2 - 100);
        
        gameOverMenu = new StackPane(menuBox);
        FXGL.addUINode(gameOverMenu);
        
        restartBtn.setOnAction(e -> {
            FXGL.removeUINode(gameOverMenu);
            initGame();
        });
        quitBtn.setOnAction(e -> Platform.exit());
    }

    private void startWave() {
        enemiesRemaining = ENEMIES_PER_WAVE + (currentWave - 1) * 2;
        waveText.setText("Wave: " + currentWave + " - Enemies: " + enemiesRemaining);
        
        for (int i = 0; i < enemiesRemaining; i++) {
            spawnEnemy();
        }
        
        // Spawn powerups randomly
        if (currentWave > 1 && random.nextDouble() < 0.7) {
            spawnPowerup();
        }
    }

    private void spawnEnemy() {
        // Spawn enemies at random edges
        double x = random.nextBoolean() ? 
            (random.nextBoolean() ? -40 : screenWidth) : 
            random.nextDouble() * screenWidth;
        double y = random.nextBoolean() ? 
            (random.nextBoolean() ? -40 : screenHeight) : 
            random.nextDouble() * screenHeight;
            
        Color enemyColor = Color.hsb(random.nextDouble() * 360, 0.8, 0.9);
        
        FXGL.entityBuilder()
            .at(x, y)
            .type(EntityType.ENEMY)
            .viewWithBBox(new Circle(20, enemyColor))
            .with(new EnemyComponent())
            .collidable()
            .buildAndAttach();
    }

    private void spawnPowerup() {
        double x = 100 + random.nextDouble() * (screenWidth - 200);
        double y = 100 + random.nextDouble() * (screenHeight - 200);
        
        PowerupType type = PowerupType.values()[random.nextInt(PowerupType.values().length)];
        Color color = type == PowerupType.RAPID_FIRE ? Color.GOLD : Color.MAGENTA;
        
        FXGL.entityBuilder()
            .at(x, y)
            .type(EntityType.POWERUP)
            .viewWithBBox(new Circle(15, color))
            .with(new PowerupComponent(type))
            .collidable()
            .buildAndAttach();
    }

    @Override
    protected void initInput() {
        // Movement
        FXGL.onKey(KeyCode.W, () -> movePlayer(0, -1));
        FXGL.onKey(KeyCode.S, () -> movePlayer(0, 1));
        FXGL.onKey(KeyCode.A, () -> movePlayer(-1, 0));
        FXGL.onKey(KeyCode.D, () -> movePlayer(1, 0));
        
        // Shooting - space bar for simplicity
        FXGL.onKey(KeyCode.SPACE, () -> {
            if (isGameActive) spawnProjectile();
        });
        
        // Rapid fire when active
        FXGL.getGameTimer().runAtInterval(() -> {
            if (rapidFireActive && isGameActive) {
                spawnProjectile();
            }
        }, Duration.millis(150));
        
        // Pause menu
        FXGL.onKeyDown(KeyCode.ESCAPE, () -> {
            if (isGameActive) {
                isGameActive = false;
                showPauseMenu();
            }
        });
    }

    private void movePlayer(int dx, int dy) {
        if (!isGameActive) return;
        
        player.translateX(PLAYER_SPEED * dx);
        player.translateY(PLAYER_SPEED * dy);
        clampPlayer();
        
        // Update direction based on movement
        if (dx != 0 || dy != 0) {
            mouseDirection = new Point2D(dx, dy).normalize();
        }
    }

    private void spawnProjectile() {
        if (stats.getAmmo() <= 0 && !rapidFireActive) return;
        
        if (!rapidFireActive) {
            stats.useAmmo(1);
        }
        
        Point2D direction = getMouseDirection();
        
        FXGL.entityBuilder()
            .at(player.getX() + 20, player.getY() + 18)
            .type(EntityType.PROJECTILE)
            .viewWithBBox(new Rectangle(10, 5, rapidFireActive ? Color.GOLD : Color.RED))
            .with(new ProjectileComponent(direction))
            .collidable()
            .buildAndAttach();
    }

    private Point2D getMouseDirection() {
        // For now, use keyboard direction. You can add mouse aiming later
        return mouseDirection;
    }

    @Override
    protected void onUpdate(double tpf) {
        if (!isGameActive) return;
        
        updatePowerups(tpf);
        checkCollisions();
        updateUI();
        
        // Wave completion check
        if (enemiesRemaining <= 0) {
            currentWave++;
            startWave();
        }
    }

    private void updatePowerups(double tpf) {
        if (rapidFireActive) {
            powerupTimer -= tpf;
            if (powerupTimer <= 0) {
                rapidFireActive = false;
            }
        }
    }

    private void checkCollisions() {
        // Projectile-enemy collisions
        FXGL.getGameWorld().getEntitiesByType(EntityType.PROJECTILE).forEach(projectile -> {
            FXGL.getGameWorld().getEntitiesByType(EntityType.ENEMY)
                .stream()
                .filter(enemy -> projectile.isColliding(enemy))
                .findFirst()
                .ifPresent(enemy -> {
                    enemy.removeFromWorld();
                    projectile.removeFromWorld();
                    stats.addScore(10 * currentWave);
                    enemiesRemaining--;
                    
                    // Chance to drop ammo
                    if (random.nextDouble() < 0.3) {
                        spawnAmmoPack(enemy.getX(), enemy.getY());
                    }
                });
        });
        
        // Player-enemy collisions
        FXGL.getGameWorld().getEntitiesByType(EntityType.ENEMY)
            .stream()
            .filter(enemy -> player.isColliding(enemy))
            .findFirst()
            .ifPresent(enemy -> {
                stats.takeDamage(10);
                enemy.removeFromWorld();
                enemiesRemaining--;
                
                if (stats.getHealth() <= 0) {
                    gameOver();
                }
            });
            
        // Powerup collisions
        FXGL.getGameWorld().getEntitiesByType(EntityType.POWERUP)
            .stream()
            .filter(powerup -> player.isColliding(powerup))
            .findFirst()
            .ifPresent(powerup -> {
                activatePowerup(powerup.getComponent(PowerupComponent.class).getType());
                powerup.removeFromWorld();
            });
            
        // Ammo pack collisions
        FXGL.getGameWorld().getEntitiesByType(EntityType.AMMO_PACK)
            .stream()
            .filter(ammo -> player.isColliding(ammo))
            .findFirst()
            .ifPresent(ammo -> {
                stats.addAmmo(15);
                ammo.removeFromWorld();
            });
    }

    private void activatePowerup(PowerupType type) {
        switch (type) {
            case RAPID_FIRE:
                rapidFireActive = true;
                powerupTimer = POWERUP_DURATION;
                break;
            case HEALTH:
                stats.heal(50);
                break;
        }
    }

    private void spawnAmmoPack(double x, double y) {
        FXGL.entityBuilder()
            .at(x, y)
            .type(EntityType.AMMO_PACK)
            .viewWithBBox(new Rectangle(20, 10, Color.CYAN))
            .with(new AmmoPackComponent())
            .collidable()
            .buildAndAttach();
    }

    private void updateUI() {
        healthText.setText(String.format("Health: %d/%d", stats.getHealth(), stats.getMaxHealth()));
        ammoText.setText(String.format("Ammo: %d/%d", stats.getAmmo(), stats.getMaxAmmo()));
        scoreText.setText("Score: " + stats.getScore());
        waveText.setText("Wave: " + currentWave + " - Enemies: " + enemiesRemaining);
        
        double healthPercent = (double) stats.getHealth() / stats.getMaxHealth();
        double ammoPercent = (double) stats.getAmmo() / stats.getMaxAmmo();
        
        healthBar.setWidth(200 * healthPercent);
        ammoBar.setWidth(200 * ammoPercent);
        
        healthBar.setFill(healthPercent > 0.5 ? Color.LIMEGREEN : 
                         healthPercent > 0.25 ? Color.ORANGE : Color.RED);
    }

    private void gameOver() {
        isGameActive = false;
        showGameOverMenu();
    }

    private void clampPlayer() {
        player.setX(Math.max(0, Math.min(screenWidth - 40, player.getX())));
        player.setY(Math.max(0, Math.min(screenHeight - 40, player.getY())));
    }

    public static void main(String[] args) {
        launch(args);
    }
}

// Player Stats class
class PlayerStats {
    private int health = 100;
    private int maxHealth = 100;
    private int ammo = 30;
    private int maxAmmo = 30;
    private int score = 0;
    
    public void reset() {
        health = maxHealth;
        ammo = maxAmmo;
        score = 0;
    }
    
    public void takeDamage(int damage) { health = Math.max(0, health - damage); }
    public void heal(int amount) { health = Math.min(maxHealth, health + amount); }
    public void useAmmo(int amount) { ammo = Math.max(0, ammo - amount); }
    public void addAmmo(int amount) { ammo = Math.min(maxAmmo, ammo + amount); }
    public void addScore(int points) { score += points; }
    
    // Getters
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getAmmo() { return ammo; }
    public int getMaxAmmo() { return maxAmmo; }
    public int getScore() { return score; }
}

// Component classes
class PlayerComponent extends Component {
    // Player-specific logic can go here
}

class EnemyComponent extends Component {
    private double speed = 1.5 + Math.random() * 1.0;
    
    @Override
    public void onUpdate(double tpf) {
        Entity player = FXGL.getGameWorld().getEntitiesByType(CombatGame.EntityType.PLAYER).get(0);
        if (player != null) {
            Point2D direction = player.getPosition().subtract(entity.getPosition()).normalize();
            entity.translateX(direction.getX() * speed);
            entity.translateY(direction.getY() * speed);
        }
    }
}

class ProjectileComponent extends Component {
    private Point2D direction;
    private double speed = 15;
    
    public ProjectileComponent(Point2D direction) {
        this.direction = direction;
    }
    
    @Override
    public void onUpdate(double tpf) {
        entity.translateX(direction.getX() * speed);
        entity.translateY(direction.getY() * speed);
        
        // Remove if off-screen
        if (entity.getX() < -50 || entity.getX() > FXGL.getAppWidth() + 50 ||
            entity.getY() < -50 || entity.getY() > FXGL.getAppHeight() + 50) {
            entity.removeFromWorld();
        }
    }
}

class PowerupComponent extends Component {
    private CombatGame.PowerupType type;
    
    public PowerupComponent(CombatGame.PowerupType type) {
        this.type = type;
    }
    
    public CombatGame.PowerupType getType() { return type; }
}

class AmmoPackComponent extends Component {
    // Simple component to identify ammo packs
}