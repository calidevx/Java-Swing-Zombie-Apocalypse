package deadsignal.entity.boss;

import deadsignal.core.Util;
import deadsignal.entity.DamageType;
import deadsignal.entity.Player;
import deadsignal.game.GameWorld;
import deadsignal.gfx.ParticleType;

import java.awt.*;

/** The assassin aspect: blinks behind the player, then lunges. Fragile but relentless. */
public final class ShadeAspect extends ShepherdAspect {
    private float blinkCooldown = 1.6f, lunge, lx, ly;
    private boolean lungeHit;

    public ShadeAspect(Shepherd parent, float x, float y, float hp) { super(parent, "SHADE", new Color(170, 120, 255), x, y, 22, hp * 0.75f); }

    @Override protected float getSpeed() { return lunge > 0 ? 0 : 150; }
    @Override protected float getPreferredRange() { return 150; }

    @Override
    protected void attack(GameWorld w, Player p, float dt, float dx, float dy, float d) {
        blinkCooldown -= dt;
        if (lunge > 0) {
            lunge -= dt;
            moveTo(w, getX() + lx * 640 * dt, getY() + ly * 640 * dt);
            if (Util.rf() < 0.7f) w.particles().spawn(ParticleType.GLOW, getX(), getY(), 0, 0, 0.35f, 5, new Color(170, 120, 255));
            if (!lungeHit && d < getRadius() + p.getRadius() + 6) { lungeHit = true; p.hurt(18, getX(), getY(), DamageType.MELEE, w); }
            return;
        }
        if (blinkCooldown <= 0) {
            // blink to a spot behind the player, then lunge through them
            float back = (float) Math.atan2(p.getMoveY(), p.getMoveX());
            if (p.getMoveX() == 0 && p.getMoveY() == 0) back = (float) Math.atan2(-dy, -dx);
            float bx = p.getX() + (float) Math.cos(back) * 170, by = p.getY() + (float) Math.sin(back) * 170;
            for (int i = 0; i < 14; i++) w.particles().spawn(ParticleType.GLOW, getX(), getY(), Util.rf(-120, 120), Util.rf(-120, 120), 0.4f, 4, new Color(170, 120, 255));
            moveTo(w, bx, by);
            for (int i = 0; i < 14; i++) w.particles().spawn(ParticleType.GLOW, getX(), getY(), Util.rf(-120, 120), Util.rf(-120, 120), 0.4f, 4, new Color(220, 190, 255));
            w.audio().play("zap", 0.7f, 1.6f);
            float ndx = p.getX() - getX(), ndy = p.getY() - getY(), nd = Math.max(1, Util.len(ndx, ndy));
            lx = ndx / nd; ly = ndy / nd; lunge = 0.45f; lungeHit = false;
            blinkCooldown = 2.6f;
        }
    }
}
