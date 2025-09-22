package worldSystem;

import java.awt.*;
import java.awt.event.KeyEvent;

public class Player extends GameObject {
    private final float speed = 4.5f;

    public Player(float x, float y, int w, int h, Color color) {
        super(x,y,w,h,color);
    }

    public void update(Input input) {
        float dx=0, dy=0;
        if (input.isDown(KeyEvent.VK_LEFT) || input.isDown(KeyEvent.VK_A)) dx-=1;
        if (input.isDown(KeyEvent.VK_RIGHT)|| input.isDown(KeyEvent.VK_D)) dx+=1;
        if (input.isDown(KeyEvent.VK_UP)   || input.isDown(KeyEvent.VK_W)) dy-=1;
        if (input.isDown(KeyEvent.VK_DOWN) || input.isDown(KeyEvent.VK_S)) dy+=1;

        if (dx!=0 && dy!=0) { dx*=0.707f; dy*=0.707f; }
        x+=dx*speed; y+=dy*speed;
    }

    @Override public void update(Object p) {
        if (p instanceof Input) update((Input)p);
    }

    @Override public void render(Graphics2D g) {
        g.setColor(color);
        g.fillRect(Math.round(x), Math.round(y), w, h);
    }
}

