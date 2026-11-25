package deadsignal.gfx;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/** Owns and recycles every particle. The list itself is never exposed (encapsulation). */
public final class ParticleSystem {
    private static final int MAX = 1600;
    private final List<Particle> particles = new ArrayList<>();

    public Particle spawn(ParticleType t, float x, float y, float vx, float vy, float life, float size, Color c) {
        if (particles.size() >= MAX) return null;
        Particle p = new Particle(t, x, y, vx, vy, life, size, c);
        particles.add(p);
        return p;
    }

    /** @param decal graphics of the ground layer, used by particles that leave stains (may be null). */
    public void update(float dt, Graphics2D decal) {
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            if (!p.update(dt)) {
                if (decal != null) p.getType().onExpire(p, decal);
                int last = particles.size() - 1;
                particles.set(i, particles.get(last));
                particles.remove(last);
            }
        }
    }

    public void render(Graphics2D g) {
        for (Particle p : particles) p.getType().draw(g, p, p.lifeFraction());
    }

    public int count() { return particles.size(); }
    public void clear() { particles.clear(); }
}
