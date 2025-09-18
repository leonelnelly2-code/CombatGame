package com.example.combatgame01;

public class GameStats {
    private int health;
    private int maxHealth;
    private int ammo;
    private int maxAmmo;
    private int score;
    private int kills;
    private int deaths;
    private int combo;
    private int streak;

    public GameStats(int health) {
        this.maxHealth = health;
        this.health = health;
        this.maxAmmo = 30; // default max ammo
        this.ammo = 30;    // default starting ammo
        this.score = 0;
        this.kills = 0;
        this.deaths = 0;
        this.combo = 0;
        this.streak = 0;
    }
    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
    }
    public int getAmmo() {
        return ammo;
    }

    public void setAmmo(int ammo) {
        this.ammo = Math.max(0, Math.min(ammo, maxAmmo));
    }

    public int getMaxAmmo() {
        return maxAmmo;
    }

    public void setMaxAmmo(int maxAmmo) {
        this.maxAmmo = maxAmmo;
        this.ammo = Math.min(this.ammo, maxAmmo);
    }

    public void addAmmo(int amount) {
        setAmmo(this.ammo + amount);
    }

    public void useAmmo(int amount) {
        setAmmo(this.ammo - amount);
    }
    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    // Removed duplicate combo and streak fields
    public int getScore() {
        return score;
    }

    public void addScore(int amount) {
        this.score += amount;
    }

    public int getKills() {
        return kills;
    }

    public void addKill() {
        this.kills++;
    }

    public int getDeaths() {
        return deaths;
    }

    public int getCombo() {
        return combo;
    }

    public void incrementCombo() {
        combo++;
    }

    public void resetCombo() {
        combo = 0;
    }

    public int getStreak() {
        return streak;
    }

    public void incrementStreak() {
        streak++;
    }

    public void resetStreak() {
        streak = 0;
    }

    public void addDeath() {
        this.deaths++;
    }
}
