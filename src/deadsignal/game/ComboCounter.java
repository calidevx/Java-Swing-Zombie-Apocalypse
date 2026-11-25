package deadsignal.game;

/** Chains kills that happen close together into a cash multiplier. */
public final class ComboCounter {
    private static final float WINDOW = 2.6f;
    private int count;
    private float timer;

    public int register() { count = timer > 0 ? count + 1 : 1; timer = WINDOW; return count; }
    public void update(float dt) { if (timer > 0) { timer -= dt; if (timer <= 0) count = 0; } }

    public int getCount() { return count; }
    public float getMultiplier() { return 1 + Math.min(count, 20) * 0.1f; }
    public float getTimerFraction() { return Math.max(0, timer / WINDOW); }
    public boolean isShowing() { return count >= 3 && timer > 0; }
}
