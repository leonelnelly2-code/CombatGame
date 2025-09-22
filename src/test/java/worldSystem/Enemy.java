package worldSystem;

import java.awt.*;

public class Enemy extends GameObject {
    private final float speed;

    public Enemy(float x, float y, int w, int h, Color color, float speed) {
        super(x,y,w,h,color);
        this.speed = speed;
    }

    @Override
    public void update(Object param) {
        if (!(param instanceof Player)) return;
        Player p = (Player)param;
        float tx = p.x+p.w/2f, ty = p.y+p.h/2f;
        float cx = x+w/2f, cy = y+h/2f;
        float dx = tx-cx, dy = ty-cy;
        float dist = (float)Math.sqrt(dx*dx+dy*dy);
        if (dist>0.1f) {
            x += (dx/dist)*speed;
            y += (dy/dist)*speed;
        }
    }

    @Override
    public void render(Graphics2D g) {
        g.setColor(color);
        g.fillOval(Math.round(x), Math.round(y), w, h);
    }
}

