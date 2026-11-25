package deadsignal.entity.ability;

import deadsignal.core.Util;
import deadsignal.entity.DamageType;
import deadsignal.entity.Player;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;
import deadsignal.gfx.ParticleType;

import java.awt.*;

/** Broodmother ability: a rune circle that erupts after a delay, hurting and JAMMING the player's guns. */
public final class BloodRite extends GroundEffect {
    private static final float RADIUS = 95, WINDUP = 2.6f;

    public BloodRite(float x, float y) { super(x, y, RADIUS, WINDUP, 0.6f); }

    @Override protected void onTrigger(GameWorld w) {
        for (int i = 0; i < 40; i++) {
            float a = Util.rf(0, 6.28f), d = Util.rf(0, RADIUS);
            w.particles().spawn(ParticleType.BLOOD, getX() + (float) Math.cos(a) * d, getY() + (float) Math.sin(a) * d, Util.rf(-40, 40), Util.rf(-160, -40), Util.rf(0.4f, 0.9f), Util.rf(2, 4), new Color(170, 10, 20));
        }
        w.particles().spawn(ParticleType.RING, getX(), getY(), 0, 0, 0.4f, RADIUS, new Color(255, 60, 60));
        w.audio().play("splat", 1f, 0.7f);
        w.audio().play("zap", 0.8f, 0.5f);
        w.shake(8);
        Player p = w.getPlayer();
        if (!p.isDead() && distanceTo(p) < RADIUS) {
            p.hurt(25, getX(), getY(), DamageType.EXPLOSION, w);
            p.jamWeapons(3f);
        }
    }

    @Override
    public void render(Graphics2D g, Assets a) {
        float x = getX(), y = getY();
        if (!isTriggered()) {
            float k = windupProgress();
            g.setColor(new Color(120, 0, 10, 60)); Util.oval(g, x, y, RADIUS, RADIUS);
            g.setColor(new Color(200, 10, 30, (int) (60 + 90 * k))); Util.oval(g, x, y, RADIUS * k, RADIUS * k);
            g.setColor(new Color(255, 60, 70, 220)); g.setStroke(new BasicStroke(2.5f)); Util.ring(g, x, y, RADIUS, RADIUS);
            float spin = k * 3f;
            g.setStroke(new BasicStroke(2f));
            for (int i = 0; i < 8; i++) {
                double an = spin + i * Math.PI / 4;
                double gx = x + Math.cos(an) * RADIUS * 0.78, gy = y + Math.sin(an) * RADIUS * 0.78;
                Util.line(g, gx - 5, gy - 6, gx + 5, gy + 6);
                Util.line(g, gx - 5, gy + 6, gx, gy - 2);
            }
            g.setColor(new Color(255, 90, 90, 200)); Util.ring(g, x, y, RADIUS * 0.55, RADIUS * 0.55);
        } else {
            float k = 1 - activeFraction();
            g.setColor(new Color(255, 40, 50, (int) (160 * k))); Util.oval(g, x, y, RADIUS, RADIUS);
        }
    }
}
