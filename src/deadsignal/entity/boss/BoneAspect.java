package deadsignal.entity.boss;

import deadsignal.core.Util;
import deadsignal.entity.DamageType;
import deadsignal.entity.Player;
import deadsignal.game.GameWorld;
import deadsignal.gfx.ParticleType;

import java.awt.*;

/** The heavy aspect: slow and tough, it hammers the ground with a telegraphed shockwave that slows. */
public final class BoneAspect extends ShepherdAspect {
    private float slamCooldown = 1.5f, windup, tx, ty;

    public BoneAspect(Shepherd parent, float x, float y, float hp) { super(parent, "BONE", new Color(240, 225, 190), x, y, 30, hp * 1.4f); }

    @Override protected float getSpeed() { return windup > 0 ? 0 : 95; }
    @Override protected float getPreferredRange() { return 60; }
    @Override protected float getContactDamage() { return 20; }

    @Override
    protected void attack(GameWorld w, Player p, float dt, float dx, float dy, float d) {
        slamCooldown -= dt;
        if (windup > 0) {
            windup -= dt;
            if (windup <= 0) {
                if (Util.dist(p.getX(), p.getY(), tx, ty) < 130) { p.hurt(22, tx, ty, DamageType.MELEE, w); p.applySlow(0.5f, 1.5f); }
                w.particles().spawn(ParticleType.RING, tx, ty, 0, 0, 0.4f, 130, new Color(240, 225, 190));
                for (int i = 0; i < 18; i++) w.particles().spawn(ParticleType.CHUNK, tx, ty, Util.rf(-200, 200), Util.rf(-200, 200), 0.6f, 3, new Color(220, 205, 170));
                w.shake(12); w.audio().play("explode", 0.7f, 0.7f);
            }
            return;
        }
        if (slamCooldown <= 0 && d < 260) { windup = 0.7f; slamCooldown = 3.2f; tx = p.getX(); ty = p.getY(); w.audio().play("roar", 0.5f, 0.8f); }
    }

    @Override protected void drawMarks(Graphics2D g, float x, float y) {
        if (windup <= 0) return;
        g.setColor(new Color(255, 230, 180, 60)); Util.oval(g, tx, ty, 130, 130);
        g.setColor(new Color(255, 230, 180, 220)); g.setStroke(new BasicStroke(2.5f)); Util.ring(g, tx, ty, 130 * (1 - windup / 0.7f), 130 * (1 - windup / 0.7f));
        Util.ring(g, tx, ty, 130, 130);
    }
}
