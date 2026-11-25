package deadsignal.gfx;

import java.awt.Color;

/** A tiny visual effect. Fields are private; only the particle system mutates them. */
public final class Particle {
    private final ParticleType type;
    private final Color color;
    private float x, y, vx, vy, life, size, rotation, spin, drag, grow;
    private final float maxLife;

    Particle(ParticleType type, float x, float y, float vx, float vy, float life, float size, Color color) {
        this.type = type; this.x = x; this.y = y; this.vx = vx; this.vy = vy; this.life = life; this.maxLife = life;
        this.size = size; this.color = color; this.drag = type.drag(); this.grow = type.grow();
        this.rotation = (float) (Math.random() * 6.28);
    }

    /** Advances the particle; returns false when it should be removed. */
    boolean update(float dt) {
        life -= dt;
        if (life <= 0) return false;
        float dm = Math.min(1, dt * drag);
        vx -= vx * dm; vy -= vy * dm;
        x += vx * dt; y += vy * dt;
        size = Math.max(0.3f, size + grow * dt);
        rotation += spin * dt;
        return true;
    }

    public Particle withSpin(float s) { spin = s; return this; }
    public ParticleType getType() { return type; }
    public Color getColor() { return color; }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getVx() { return vx; }
    public float getVy() { return vy; }
    public float getSize() { return size; }
    public float getRotation() { return rotation; }
    float lifeFraction() { return life / maxLife; }
}
