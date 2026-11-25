package deadsignal.core;

import java.awt.Rectangle;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-safe input state. The Swing thread pushes raw events in; the game thread
 * calls {@link #poll()} once per tick and reads a stable snapshot. All state is private.
 */
public final class Input {
    private final boolean[] keys = new boolean[1024];
    private final ConcurrentLinkedQueue<Integer> pressedQueue = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<int[]> clickQueue = new ConcurrentLinkedQueue<>();
    private final AtomicInteger wheelAcc = new AtomicInteger();
    private volatile int mouseX = Const.WIDTH / 2, mouseY = Const.HEIGHT / 2;
    private volatile boolean leftDown, rightDown;

    private final Set<Integer> pressed = new HashSet<>();
    private final List<int[]> clicks = new ArrayList<>();
    private int wheel;

    // ---------------------------------------------------- event side (Swing thread)
    public void keyDown(int k) { if (k >= 0 && k < keys.length) { if (!keys[k]) pressedQueue.add(k); keys[k] = true; } }
    public void keyUp(int k) { if (k >= 0 && k < keys.length) keys[k] = false; }
    public void mouseMoved(int x, int y) { mouseX = x; mouseY = y; }
    public void mouseButton(int button, boolean down) {
        if (button == 1) leftDown = down; else if (button == 3) rightDown = down;
        if (down) clickQueue.add(new int[]{mouseX, mouseY, button});
    }
    public void wheelMoved(int amount) { wheelAcc.addAndGet(amount); }

    // ---------------------------------------------------- game side
    /** Snapshots the queued events. Call exactly once per update tick. */
    public void poll() {
        pressed.clear(); clicks.clear();
        Integer k; while ((k = pressedQueue.poll()) != null) pressed.add(k);
        int[] c; while ((c = clickQueue.poll()) != null) clicks.add(c);
        wheel = wheelAcc.getAndSet(0);
    }
    public boolean isDown(int k) { return k >= 0 && k < keys.length && keys[k]; }
    public boolean wasPressed(int... ks) { for (int k : ks) if (pressed.contains(k)) return true; return false; }
    public boolean confirmPressed() { return wasPressed(java.awt.event.KeyEvent.VK_ENTER, java.awt.event.KeyEvent.VK_SPACE) || !clicks.isEmpty(); }
    public boolean rightClicked() { for (int[] c : clicks) if (c[2] == 3) return true; return false; }
    public boolean leftClickedIn(Rectangle r) { for (int[] c : clicks) if (c[2] == 1 && r.contains(c[0], c[1])) return true; return false; }
    public int getWheel() { return wheel; }
    public int getMouseX() { return mouseX; }
    public int getMouseY() { return mouseY; }
    public boolean isLeftDown() { return leftDown; }
    public boolean isRightDown() { return rightDown; }

    // ---------------------------------------------------- automation helpers (used by the test bot)
    public void simulateKey(int k, boolean down) { if (down) keyDown(k); else keyUp(k); }
    public void simulatePress(int k) { pressedQueue.add(k); }
    public void simulateMouse(int x, int y, boolean left) { mouseX = x; mouseY = y; leftDown = left; }
}
