package worldSystem;

import java.awt.*;
import java.util.Random;

public class LevelManager {
    private final GamePanel panel;
    private int stage=1, wave=0;
    private long lastWave=0;
    private final long cooldown=2000;

    public LevelManager(GamePanel panel) {
        this.panel=panel;
        lastWave=System.currentTimeMillis();
    }

    public void update() {
        long now=System.currentTimeMillis();
        if (panel.enemyCount()==0 && now-lastWave>cooldown) {
            wave++;
            int wavesThisStage=stage+1;
            if (wave>wavesThisStage) { stage++; wave=1; }
            spawnWave(stage,wave);
            lastWave=now;
        }
    }

    private void spawnWave(int stage,int wave) {
        int count=Math.max(1,stage*wave);
        float baseSpeed=1f+stage*0.2f;
        Random r=new Random();

        for(int i=0;i<count;i++){
            int edge=r.nextInt(4);
            int px=0,py=0; int w=20,h=20;
            switch(edge){
                case 0: px=r.nextInt(panel.getPanelWidth()-w); py=0; break;
                case 1: px=panel.getPanelWidth()-w; py=r.nextInt(panel.getPanelHeight()-h); break;
                case 2: px=r.nextInt(panel.getPanelWidth()-w); py=panel.getPanelHeight()-h; break;
                case 3: px=0; py=r.nextInt(panel.getPanelHeight()-h); break;
            }
            Enemy e=new Enemy(px,py,w,h,Color.RED,baseSpeed+r.nextFloat()*0.5f);
            panel.spawnEnemy(e);
        }
    }

    public int getStage(){return stage;}
    public int getWaveIndex(){return wave;}
}

