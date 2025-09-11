package com.example.combatgame01;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import javafx.scene.input.KeyCode;

public class PlayerComponent extends Component {

    private static final double SPEED = 200; // pixels/sec

    @Override
    public void onUpdate(double tpf) {
        if (FXGL.getInput().isKeyDown(KeyCode.W)) {
            getEntity().translateY(-SPEED * tpf);
        }
        if (FXGL.getInput().isKeyDown(KeyCode.S)) {
            getEntity().translateY(SPEED * tpf);
        }
        if (FXGL.getInput().isKeyDown(KeyCode.A)) {
            getEntity().translateX(-SPEED * tpf);
        }
        if (FXGL.getInput().isKeyDown(KeyCode.D)) {
            getEntity().translateX(SPEED * tpf);
        }
    }
}
