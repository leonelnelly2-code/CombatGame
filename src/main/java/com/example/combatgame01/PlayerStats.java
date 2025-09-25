package com.example.combatgame01;

public class PlayerStats {
    private int health;
    private final int maxHealth;
    private int ammo = 30;
    private int score = 0;

    public PlayerStats(int maxHealth) {
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    // Add a no-argument constructor for FXGL
    public PlayerStats() {
        this(100); // Default to 100 health
    }
    
    public void reset() {
        health = maxHealth;
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

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void damage(int amount) {
        health -= amount;
        if (health < 0) {
            health = 0;
        }
    }

    public boolean isDead() {
        return health <= 0;
    }

    public void heal(int amount) {
        health += amount;
        if (health > maxHealth) {
            health = maxHealth;
        }
    }
    
    public int getAmmo() { return ammo; }
    public int getScore() { return score; }
}