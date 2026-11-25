package deadsignal.entity.projectile;

import deadsignal.core.Util;
import deadsignal.game.GameWorld;
import deadsignal.gfx.ParticleType;

import java.awt.Color;

/** A thrown boulder that shatters into fragments on impact (overrides onExpire). */
public final class Boulder extends RockFragment {
    public Boulder(float x, float y, float angle, float flightTime) {
        super(x, y, angle, 390, 26, 14, flightTime);
    }

    @Override protected void onExpire(GameWorld w) {
        for (int k = 0; k < 7; k++) {
            float a = k / 7f * 6.28f + Util.rf(0, 0.5f);
            w.addProjectile(new RockFragment(getX() - getVx() * 0.02f, getY() - getVy() * 0.02f, a, 260, 8, 4, 0.4f));
        }
        for (int k = 0; k < 8; k++) w.particles().spawn(ParticleType.SMOKE, getX(), getY(), Util.rf(-60, 60), Util.rf(-60, 60), 0.8f, 10, new Color(120, 110, 100, 140));
        w.shake(5);
        w.audio().play("explode", 0.4f, 1.6f);
    }
}
