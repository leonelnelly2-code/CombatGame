package com.example.combatgame01;

import com.almasb.fxgl.entity.component.Component;

public class PlayerComponent extends Component {
    private GameStats stats;

    public PlayerComponent() {
        // Default health set to 100, can be parameterized
        this.stats = new GameStats(100);
    }

    public GameStats getStats() {
        return stats;
    }

    // Add methods to update stats on events (damage, kill, etc.)
}