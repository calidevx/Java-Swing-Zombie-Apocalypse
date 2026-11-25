package deadsignal.gfx;

import deadsignal.core.Util;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * POLYMORPHISM with enums: every constant overrides {@link #draw} with its own look,
 * and some override {@link #onExpire} to leave a permanent mark on the ground.
 */
public enum ParticleType {
    DOT {
        @Override void draw(Graphics2D g, Particle p, float k) {
            g.setColor(Util.alpha(p.getColor(), (int) (255 * Math.min(1, k * 2)))); Util.oval(g, p.getX(), p.getY(), p.getSize(), p.getSize());
        }
    },
    SMOKE {
        @Override void draw(Graphics2D g, Particle p, float k) {
            g.setColor(Util.alpha(p.getColor(), (int) (p.getColor().getAlpha() * k))); Util.oval(g, p.getX(), p.getY(), p.getSize(), p.getSize());
        }
        @Override float drag() { return 1.5f; }
        @Override float grow() { return 14; }
    },
    FIRE {
        @Override void draw(Graphics2D g, Particle p, float k) {
            Color c = Util.mix(new Color(140, 30, 10), p.getColor(), k);
            g.setColor(Util.alpha(c, (int) (230 * k)));
            float s = p.getSize() * (0.4f + k * 0.6f); Util.oval(g, p.getX(), p.getY(), s, s);
        }
        @Override float grow() { return -6; }
    },
    SPARK {
        @Override void draw(Graphics2D g, Particle p, float k) {
            g.setColor(Util.alpha(p.getColor(), (int) (255 * k))); g.setStroke(new BasicStroke(p.getSize()));
            Util.line(g, p.getX(), p.getY(), p.getX() - p.getVx() * 0.03f, p.getY() - p.getVy() * 0.03f);
        }
    },
    SHELL {
        @Override void draw(Graphics2D g, Particle p, float k) {
            AffineTransform at = g.getTransform(); g.translate(p.getX(), p.getY()); g.rotate(p.getRotation());
            g.setColor(p.getColor()); g.fillRect(-2, -1, 4, 2); g.setTransform(at);
        }
        @Override void onExpire(Particle p, Graphics2D decal) { decal.setColor(new Color(170, 140, 60)); decal.fillRect((int) p.getX(), (int) p.getY(), 2, 1); }
        @Override float drag() { return 5; }
    },
    BLOOD {
        @Override void draw(Graphics2D g, Particle p, float k) {
            g.setColor(Util.alpha(p.getColor(), (int) (255 * Math.min(1, k * 2)))); Util.oval(g, p.getX(), p.getY(), p.getSize(), p.getSize());
        }
        @Override void onExpire(Particle p, Graphics2D decal) {
            decal.setColor(Util.alpha(Util.shade(p.getColor(), 0.75), 170)); Util.oval(decal, p.getX(), p.getY(), p.getSize() * 0.9, p.getSize() * 0.8);
        }
        @Override float drag() { return 4; }
    },
    RING {
        @Override void draw(Graphics2D g, Particle p, float k) {
            g.setColor(Util.alpha(p.getColor(), (int) (220 * k))); g.setStroke(new BasicStroke(3 + 6 * k));
            float r = p.getSize() * (1 - k * 0.85f); Util.ring(g, p.getX(), p.getY(), r, r);
        }
    },
    CHUNK {
        @Override void draw(Graphics2D g, Particle p, float k) {
            AffineTransform at = g.getTransform(); g.translate(p.getX(), p.getY()); g.rotate(p.getRotation());
            g.setColor(p.getColor()); g.fillRect((int) -p.getSize(), (int) -p.getSize(), (int) (p.getSize() * 2), (int) (p.getSize() * 1.5f));
            g.setTransform(at);
        }
    },
    GLOW {
        @Override void draw(Graphics2D g, Particle p, float k) {
            g.setColor(Util.alpha(p.getColor(), (int) (200 * k))); Util.oval(g, p.getX(), p.getY(), p.getSize(), p.getSize());
        }
    };

    /** Draw one particle; k is remaining life from 1 (new) to 0 (gone). */
    abstract void draw(Graphics2D g, Particle p, float k);
    /** Hook: leave a decal when the particle dies. Default: nothing. */
    void onExpire(Particle p, Graphics2D decal) { }
    float drag() { return 2.5f; }
    float grow() { return 0; }
}
