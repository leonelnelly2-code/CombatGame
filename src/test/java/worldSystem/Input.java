package worldSystem;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

public class Input {
    private final Set<Integer> pressed = new HashSet<>();

    public void keyPressed(KeyEvent e) { pressed.add(e.getKeyCode()); }
    public void keyReleased(KeyEvent e) { pressed.remove(e.getKeyCode()); }
    public boolean isDown(int keyCode) { return pressed.contains(keyCode); }
}
