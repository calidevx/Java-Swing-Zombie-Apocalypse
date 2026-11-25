package deadsignal.gfx;

import java.awt.Color;

/** A short-lived light burst (muzzle flash, explosion). */
public final class Flash implements LightEmitter {
    private final float x, y, radius, duration;
    private final Color color;
    private float timeLeft;

    public Flash(float x, float y, float radius, float duration, Color color) {
        this.x = x; this.y = y; this.radius = radius; this.duration = duration; this.timeLeft = duration; this.color = color;
    }

    /** @return false once the flash has faded out. */
    public boolean update(float dt) { timeLeft -= dt; return timeLeft > 0; }

    @Override public float getLightX() { return x; }
    @Override public float getLightY() { return y; }
    @Override public float getLightRadius() { return radius; }
    @Override public float getLightIntensity(float time) { return Math.max(0, timeLeft / duration); }
    @Override public Color getGlowColor() { return color; }
    @Override public int getGlowAlpha() { return (int) (90 * Math.max(0, timeLeft / duration)); }
    @Override public float getGlowRadius() { return radius * 0.6f; }
}
