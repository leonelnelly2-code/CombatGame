package com.example.combatgame01;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.*;

public class CombatGame extends Application {

    private static final int WIDTH = 1000;
    private static final int HEIGHT = 700;
    private static final int PLAYER_SIZE = 40;
    private static final int PLAYER_SPEED = 5; // Simpler movement

    private Canvas canvas;
    private GraphicsContext gc;
    private Pane root;

    private Player player;
    private List<Enemy> enemies = new ArrayList<>();
    private List<Projectile> projectiles = new ArrayList<>();
    private Random random = new Random();

    private boolean gameRunning = true;
    private int score = 0;
    private int currentWave = 0;
    private boolean waveCompleted = true;
    private double waveStartTimer = 2.0;

    // Simple boolean movement system
    private boolean wPressed, aPressed, sPressed, dPressed, spacePressed;

    @Override
    public void start(Stage stage) {
        root = new Pane();
        canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();
        root.getChildren().add(canvas);

        // Initialize player at center
        player = new Player(WIDTH / 2 - PLAYER_SIZE/2, HEIGHT / 2 - PLAYER_SIZE/2);

        // Setup input handlers
        setupInputHandlers();

        Scene scene = new Scene(root);
        stage.setTitle("Combat Game - 3 Wave System");
        stage.setScene(scene);
        stage.show();

        // CRITICAL: Request focus after stage is shown
        root.requestFocus();

        // Start game loop
        startGameLoop();
    }

    private void setupInputHandlers() {
        root.setOnKeyPressed(e -> {
            KeyCode code = e.getCode();
            switch (code) {
                case W -> wPressed = true;
                case A -> aPressed = true;
                case S -> sPressed = true;
                case D -> dPressed = true;
                case SPACE -> spacePressed = true;
                case R -> { if (!gameRunning) restartGame(); }
            }
        });

        root.setOnKeyReleased(e -> {
            KeyCode code = e.getCode();
            switch (code) {
                case W -> wPressed = false;
                case A -> aPressed = false;
                case S -> sPressed = false;
                case D -> dPressed = false;
                case SPACE -> spacePressed = false;
            }
        });

        root.setFocusTraversable(true);
    }

    private void restartGame() {
        player = new Player(WIDTH / 2 - PLAYER_SIZE/2, HEIGHT / 2 - PLAYER_SIZE/2);
        enemies.clear();
        projectiles.clear();
        currentWave = 0;
        score = 0;
        gameRunning = true;
        waveCompleted = true;
        waveStartTimer = 2.0;
    }

    private void startGameLoop() {
        new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;

                if (gameRunning) {
                    update(deltaTime);
                }
                render();
            }
        }.start();
    }

    private void update(double deltaTime) {
        // Update player with simple movement
        player.update(wPressed, aPressed, sPressed, dPressed);

        // Handle shooting
        if (spacePressed) {
            player.shoot(projectiles);
        }

        // Update projectiles
        updateProjectiles(deltaTime);

        // Update enemies
        updateEnemies(deltaTime);

        // Wave management
        updateWaveSystem(deltaTime);

        // Check collisions
        checkCollisions();
    }

    // ======================
    // WAVE SYSTEM (3 Distinct Waves)
    // ======================

    private void updateWaveSystem(double deltaTime) {
        if (waveCompleted) {
            waveStartTimer -= deltaTime;
            if (waveStartTimer <= 0) {
                startNextWave();
                waveCompleted = false;
                waveStartTimer = 2.0; // Reset for next wave
            }
        } else {
            // Check if current wave is completed
            if (enemies.isEmpty()) {
                waveCompleted = true;
                if (currentWave >= 3) {
                    // Player completed all 3 waves
                    gameRunning = true; // Keep game running to show victory
                }
            }
        }
    }

    private void startNextWave() {
        currentWave++;
        System.out.println("Starting Wave " + currentWave);

        switch (currentWave) {
            case 1 -> spawnWave1(); // Basic enemies only
            case 2 -> spawnWave2(); // Strong enemies only
            case 3 -> spawnWave3(); // Boss enemy only
        }
    }

    private void spawnWave1() {
        // Wave 1: 8 Basic enemies in a circle pattern
        int enemyCount = 8;
        for (int i = 0; i < enemyCount; i++) {
            double angle = (2 * Math.PI * i) / enemyCount;
            double distance = 300;
            double x = WIDTH / 2 + Math.cos(angle) * distance;
            double y = HEIGHT / 2 + Math.sin(angle) * distance;
            enemies.add(new Enemy(EnemyType.BASIC, x, y));
        }
    }

    private void spawnWave2() {
        // Wave 2: 6 Strong enemies in a diamond pattern
        int enemyCount = 6;
        for (int i = 0; i < enemyCount; i++) {
            double angle = (2 * Math.PI * i) / enemyCount;
            double distance = 250;
            double x = WIDTH / 2 + Math.cos(angle) * distance;
            double y = HEIGHT / 2 + Math.sin(angle) * distance;
            enemies.add(new Enemy(EnemyType.STRONG, x, y));
        }
    }

    private void spawnWave3() {
        // Wave 3: 1 Boss enemy in the center top
        enemies.add(new Enemy(EnemyType.BOSS, WIDTH / 2 - 40, 100));

        // Add 4 strong enemies as support
        for (int i = 0; i < 4; i++) {
            double angle = (2 * Math.PI * i) / 4;
            double distance = 200;
            double x = WIDTH / 2 + Math.cos(angle) * distance;
            double y = HEIGHT / 2 + Math.sin(angle) * distance;
            enemies.add(new Enemy(EnemyType.STRONG, x, y));
        }
    }

    // ======================
    // PROJECTILE SYSTEM
    // ======================

    private void updateProjectiles(double deltaTime) {
        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            p.update(deltaTime);

            if (p.isOffScreen(WIDTH, HEIGHT)) {
                projectiles.remove(i);
            }
        }
    }

    // ======================
    // ENEMY SYSTEM
    // ======================

    private void updateEnemies(double deltaTime) {
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.update(deltaTime, player);

            if (enemy.isDead()) {
                score += enemy.getPointValue();
                enemies.remove(i);
            }
        }
    }

    // ======================
    // COLLISION SYSTEM
    // ======================

    private void checkCollisions() {
        // Projectile-Enemy collisions
        for (int i = projectiles.size() - 1; i >= 0; i--) {
            Projectile p = projectiles.get(i);
            for (int j = enemies.size() - 1; j >= 0; j--) {
                Enemy enemy = enemies.get(j);
                if (p.collidesWith(enemy)) {
                    enemy.takeDamage(p.getDamage());
                    projectiles.remove(i);
                    break;
                }
            }
        }

        // Enemy-Player collisions
        for (Enemy enemy : enemies) {
            if (enemy.collidesWith(player)) {
                player.takeDamage(10);
                if (player.isDead()) {
                    gameRunning = false;
                }
            }
        }
    }

    // ======================
    // RENDERING
    // ======================

    private void render() {
        // Clear screen
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw game objects
        for (Projectile p : projectiles) p.render(gc);
        for (Enemy e : enemies) e.render(gc);
        player.render(gc);

        // Draw UI
        drawUI();

        // Game over or victory screen
        if (!gameRunning || currentWave > 3) {
            drawGameOverScreen();
        }
    }

    private void drawUI() {
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(16));

        gc.fillText("Wave: " + currentWave + "/3", 20, 30);
        gc.fillText("Score: " + score, 20, 60);
        gc.fillText("Health: " + player.getHealth(), 20, 90);
        gc.fillText("Enemies: " + enemies.size(), 20, 120);

        // Wave countdown
        if (waveCompleted && currentWave < 3) {
            gc.fillText("Next wave in: " + String.format("%.1f", waveStartTimer), 20, 150);
        }

        // Controls reminder
        gc.fillText("Controls: WASD to move, SPACE to shoot, R to restart", 20, HEIGHT - 30);

        // Wave description
        String waveDesc = getWaveDescription();
        gc.fillText(waveDesc, WIDTH - 250, 30);
    }

    private String getWaveDescription() {
        return switch (currentWave) {
            case 1 -> "Wave 1: Basic Enemies";
            case 2 -> "Wave 2: Strong Enemies";
            case 3 -> "Wave 3: BOSS BATTLE!";
            default -> "Prepare for battle...";
        };
    }

    private void drawGameOverScreen() {
        gc.setFill(new Color(0, 0, 0, 0.7));
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font(32));

        if (player.isDead()) {
            gc.setFill(Color.RED);
            gc.fillText("GAME OVER", WIDTH/2 - 100, HEIGHT/2 - 50);
            gc.setFill(Color.WHITE);
            gc.fillText("Waves Survived: " + currentWave, WIDTH/2 - 100, HEIGHT/2);
            gc.fillText("Final Score: " + score, WIDTH/2 - 100, HEIGHT/2 + 50);
        } else if (currentWave > 3) {
            gc.setFill(Color.GREEN);
            gc.fillText("VICTORY!", WIDTH/2 - 80, HEIGHT/2 - 50);
            gc.setFill(Color.WHITE);
            gc.fillText("You defeated all waves!", WIDTH/2 - 150, HEIGHT/2);
            gc.fillText("Final Score: " + score, WIDTH/2 - 100, HEIGHT/2 + 50);
        }

        gc.fillText("Press R to Restart", WIDTH/2 - 120, HEIGHT/2 + 100);
    }

    public static void main(String[] args) {
        launch();
    }

    // ======================
    // PLAYER CLASS (Simplified Movement)
    // ======================
    public class Player {
        private double x, y;
        private int health = 100;
        private double shootCooldown = 0;

        public Player(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public void update(boolean w, boolean a, boolean s, boolean d) {
            // Simple direct movement - no complex physics
            if (w) y -= PLAYER_SPEED;
            if (s) y += PLAYER_SPEED;
            if (a) x -= PLAYER_SPEED;
            if (d) x += PLAYER_SPEED;

            // Keep in bounds
            x = Math.max(0, Math.min(WIDTH - PLAYER_SIZE, x));
            y = Math.max(0, Math.min(HEIGHT - PLAYER_SIZE, y));

            // Update shoot cooldown
            if (shootCooldown > 0) {
                shootCooldown -= 0.016; // Approximate frame time
            }
        }

        public void shoot(List<Projectile> projectiles) {
            if (shootCooldown <= 0) {
                // Shoot in 4 directions
                projectiles.add(new Projectile(x + PLAYER_SIZE/2 - 5, y, 0, -8, 10, Color.CYAN, true)); // Up
                projectiles.add(new Projectile(x + PLAYER_SIZE/2 - 5, y + PLAYER_SIZE, 0, 8, 10, Color.CYAN, true)); // Down
                projectiles.add(new Projectile(x, y + PLAYER_SIZE/2 - 5, -8, 0, 10, Color.CYAN, true)); // Left
                projectiles.add(new Projectile(x + PLAYER_SIZE, y + PLAYER_SIZE/2 - 5, 8, 0, 10, Color.CYAN, true)); // Right
                shootCooldown = 0.3; // 300ms cooldown
            }
        }

        public void takeDamage(int damage) {
            health -= damage;
            health = Math.max(0, health);
        }

        public boolean isDead() {
            return health <= 0;
        }

        public void render(GraphicsContext gc) {
            // Player body
            gc.setFill(Color.BLUE);
            gc.fillRect(x, y, PLAYER_SIZE, PLAYER_SIZE);

            // Health bar
            double healthPercent = health / 100.0;
            gc.setFill(Color.RED);
            gc.fillRect(x, y - 15, PLAYER_SIZE, 5);
            gc.setFill(Color.GREEN);
            gc.fillRect(x, y - 15, PLAYER_SIZE * healthPercent, 5);
        }

        public double getX() { return x + PLAYER_SIZE/2; }
        public double getY() { return y + PLAYER_SIZE/2; }
        public int getHealth() { return health; }
        public double getSize() { return PLAYER_SIZE; }
    }

    // ======================
    // ENEMY TYPE SYSTEM
    // ======================
    public enum EnemyType {
        BASIC(30, 30, 2, 10, 5, 50, Color.GREEN, 100),
        STRONG(80, 40, 1.5, 20, 10, 100, Color.ORANGE, 200),
        BOSS(200, 60, 1, 30, 20, 500, Color.RED, 1000);

        final int health;
        final int size;
        final double speed;
        final int contactDamage;
        final int attackDamage;
        final int pointValue;
        final Color color;
        final int attackRange;

        EnemyType(int health, int size, double speed, int contactDamage,
                  int attackDamage, int pointValue, Color color, int attackRange) {
            this.health = health;
            this.size = size;
            this.speed = speed;
            this.contactDamage = contactDamage;
            this.attackDamage = attackDamage;
            this.pointValue = pointValue;
            this.color = color;
            this.attackRange = attackRange;
        }
    }

    // ======================
    // ENEMY CLASS
    // ======================
    public class Enemy {
        private EnemyType type;
        private double x, y;
        private int health;
        private double attackCooldown = 0;

        public Enemy(EnemyType type, double x, double y) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.health = type.health;
        }

        public void update(double deltaTime, Player player) {
            // Simple chasing AI
            double dx = player.getX() - (x + type.size/2);
            double dy = player.getY() - (y + type.size/2);
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance > 0) {
                // Move toward player
                x += (dx / distance) * type.speed;
                y += (dy / distance) * type.speed;
            }

            // Attack cooldown
            if (attackCooldown > 0) {
                attackCooldown -= deltaTime;
            }

            // Boss special attack
            if (type == EnemyType.BOSS && attackCooldown <= 0 && distance < 300) {
                performBossAttack();
                attackCooldown = 3.0;
            }
        }

        private void performBossAttack() {
            // Boss shoots in 8 directions
            for (int i = 0; i < 8; i++) {
                double angle = (2 * Math.PI * i) / 8;
                projectiles.add(new Projectile(
                        x + type.size/2 - 5, y + type.size/2 - 5,
                        Math.cos(angle) * 4, Math.sin(angle) * 4,
                        15, Color.RED, false
                ));
            }
        }

        public void render(GraphicsContext gc) {
            // Enemy body
            gc.setFill(type.color);
            gc.fillRect(x, y, type.size, type.size);

            // Health bar
            double healthPercent = (double)health / type.health;
            gc.setFill(Color.RED);
            gc.fillRect(x, y - 10, type.size, 5);
            gc.setFill(Color.GREEN);
            gc.fillRect(x, y - 10, type.size * healthPercent, 5);

            // Boss has special visual
            if (type == EnemyType.BOSS) {
                gc.setStroke(Color.YELLOW);
                gc.setLineWidth(2);
                gc.strokeRect(x - 2, y - 2, type.size + 4, type.size + 4);
            }
        }

        public void takeDamage(int damage) {
            health -= damage;
        }

        public boolean isDead() {
            return health <= 0;
        }

        public boolean collidesWith(Player player) {
            double enemyCenterX = x + type.size/2;
            double enemyCenterY = y + type.size/2;
            double playerCenterX = player.getX();
            double playerCenterY = player.getY();

            double dx = enemyCenterX - playerCenterX;
            double dy = enemyCenterY - playerCenterY;
            double distance = Math.sqrt(dx * dx + dy * dy);

            return distance < (type.size/2 + player.getSize()/2);
        }

        public double getX() { return x + type.size/2; }
        public double getY() { return y + type.size/2; }
        public int getContactDamage() { return type.contactDamage; }
        public int getPointValue() { return type.pointValue; }
        public EnemyType getType() { return type; }
    }

    // ======================
    // PROJECTILE CLASS
    // ======================
    public class Projectile {
        private double x, y, vx, vy;
        private int damage;
        private Color color;
        private boolean fromPlayer;

        public Projectile(double x, double y, double vx, double vy, int damage, Color color, boolean fromPlayer) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.damage = damage;
            this.color = color;
            this.fromPlayer = fromPlayer;
        }

        public void update(double deltaTime) {
            x += vx;
            y += vy;
        }

        public boolean isOffScreen(int width, int height) {
            return x < -50 || x > width + 50 || y < -50 || y > height + 50;
        }

        public boolean collidesWith(Enemy enemy) {
            if (!fromPlayer) return false; // Only player projectiles hit enemies

            double projCenterX = x + 5;
            double projCenterY = y + 5;
            double enemyCenterX = enemy.getX();
            double enemyCenterY = enemy.getY();

            double dx = projCenterX - enemyCenterX;
            double dy = projCenterY - enemyCenterY;
            double distance = Math.sqrt(dx * dx + dy * dy);

            return distance < (5 + enemy.getType().size/2);
        }

        public void render(GraphicsContext gc) {
            gc.setFill(color);
            gc.fillOval(x, y, 10, 10);
        }

        public int getDamage() { return damage; }
    }
}