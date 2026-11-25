package deadsignal.entity.projectile;

import deadsignal.core.Util;
import deadsignal.entity.AcidPool;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;
import deadsignal.gfx.LightEmitter;
import deadsignal.gfx.ParticleType;

import java.awt.*;

/** Spat acid that leaves a burning puddle where it lands. */
public final class AcidGlob extends Projectile implements LightEmitter {
    public AcidGlob(float x, float y, float angle, float speed, float damage, float radius, float flightTime) {
        super(x, y, radius, angle, speed, flightTime, damage, false);
    }

    @Override protected void onExpire(GameWorld w) {
        w.addEffect(new AcidPool(getX(), getY()));
        for (int k = 0; k < 6; k++) w.particles().spawn(ParticleType.DOT, getX(), getY(), Util.rf(-80, 80), Util.rf(-80, 80), 0.4f, 2.5f, new Color(150, 230, 60));
        w.audio().play("splat", 0.35f, 1.3f);
    }

    @Override
    public void render(Graphics2D g, Assets a) {
        float r = getRadius();
        g.setColor(new Color(90, 150, 30)); Util.oval(g, getX(), getY(), r + 1, r + 1);
        g.setColor(new Color(180, 250, 80)); Util.oval(g, getX() - 1, getY() - 1, r * 0.6, r * 0.6);
    }

    @Override public float getLightX() { return getX(); }
    @Override public float getLightY() { return getY(); }
    @Override public float getLightRadius() { return 34; }
    @Override public float getLightIntensity(float t) { return 0.5f; }
}
