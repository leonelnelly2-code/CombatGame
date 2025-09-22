package worldSystem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class GamePanel extends JPanel implements Runnable, KeyListener {
    private final int width, height;
    private Thread thread;
    private boolean running = false;
    private final int TARGET_FPS = 60;

    private Player player;
    private final java.util.List<Enemy> enemies = Collections.synchronizedList(new ArrayList<>());
    private final LevelManager levelManager;
    private final Input input = new Input();

    public GamePanel(int width, int height) {
        this.width = width;
        this.height = height;
        setPreferredSize(new Dimension(width, height));
        setFocusable(true);
        addKeyListener(this);

        player = new Player(width / 2f - 16, height / 2f - 16, 32, 32, Color.BLUE);
        levelManager = new LevelManager(this);
    }

    public void start() {
        if (thread == null) {
            running = true;
            thread = new Thread(this);
            thread.start();
        }
    }

    @Override
    public void run() {
        long targetTime = 1000 / TARGET_FPS;
        while (running) {
            long start = System.currentTimeMillis();

            update();
            repaint();

            long elapsed = System.currentTimeMillis() - start;
            long wait = targetTime - elapsed;
            if (wait < 5) wait = 5;
            try { Thread.sleep(wait); } catch (Exception ignored) {}
        }
    }

    private void update() {
        player.update(input);
        player.clamp(0, 0, width, height);

        synchronized (enemies) {
            Iterator<Enemy> it = enemies.iterator();
            while (it.hasNext()) {
                Enemy e = it.next();
                e.update(player);
                e.clamp(0, 0, width, height);
                if (e.bounds().intersects(player.bounds())) {
                    it.remove();
                    System.out.println("Player hit!");
                }
            }
        }
        levelManager.update();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;

        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, width, height);
        drawGrid(g, 40);
        g.setColor(Color.LIGHT_GRAY);
        g.drawRect(0, 0, width - 1, height - 1);

        if (levelManager.getStage() > 1) {
            g.setColor(new Color(50, 100, 50));  // Green for "forest" in stage 2
            g.fillRect(0, 0, 800, 600);      // Special zone
        }


        player.render(g);
        synchronized (enemies) {
            for (Enemy e : enemies) e.render(g);

        }

        g.setColor(Color.WHITE);
        g.drawString("Stage: " + levelManager.getStage() +
                " Wave: " + levelManager.getWaveIndex() +
                " Enemies: " + enemies.size(), 10, 20);
    }

    private void drawGrid(Graphics2D g, int cell) {
        g.setColor(new Color(60,60,60));
        for (int x=0; x<width; x+=cell) g.drawLine(x,0,x,height);
        for (int y=0; y<height; y+=cell) g.drawLine(0,y,width,y);
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyPressed(KeyEvent e) { input.keyPressed(e); }
    @Override public void keyReleased(KeyEvent e) { input.keyReleased(e); }

    public void spawnEnemy(Enemy e) { enemies.add(e); }
    public int enemyCount() { return enemies.size(); }
    public int getPanelWidth() { return width; }
    public int getPanelHeight() { return height; }
}

