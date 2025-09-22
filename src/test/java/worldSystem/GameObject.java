package worldSystem;

import java.awt.*;

public abstract class GameObject {
    protected float x, y;
    protected int w, h;
    protected Color color;

    public GameObject(float x, float y, int w, int h, Color color) {
        this.x=x; this.y=y; this.w=w; this.h=h; this.color=color;
    }

    public Rectangle bounds() {
        return new Rectangle(Math.round(x), Math.round(y), w, h);
    }

    public void clamp(int minX, int minY, int maxX, int maxY) {
        if (x < minX) x = minX;
        if (y < minY) y = minY;
        if (x + w > maxX) x = maxX - w;
        if (y + h > maxY) y = maxY - h;
    }

    public abstract void update(Object param);
    public abstract void render(Graphics2D g);
}

