package deadsignal.entity.projectile;

import deadsignal.core.Util;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;
import deadsignal.gfx.LightEmitter;
import deadsignal.gfx.ParticleType;

import java.awt.*;

/** A glowing enemy shot (spores, shards, minigun tracers). Subclassed by {@link HomingSpore}. */
public class EnergyOrb extends Projectile implements LightEmitter {
    private final Color color;

    public EnergyOrb(float x, float y, float angle, float speed, float damage, float radius, Color color) {
        super(x, y, radius, angle, speed, 3.5f, damage, false);
        this.color = color;
    }

    @Override protected void onExpire(GameWorld w) {
        for (int k = 0; k < 3; k++) w.particles().spawn(ParticleType.GLOW, getX(), getY(), Util.rf(-60, 60), Util.rf(-60, 60), 0.25f, 2, color);
    }

    @Override
    public void render(Graphics2D g, Assets a) {
        float r = getRadius();
        g.setColor(Util.alpha(color, 90)); Util.oval(g, getX(), getY(), r * 1.8, r * 1.8);
        g.setColor(color); Util.oval(g, getX(), getY(), r, r);
        g.setColor(new Color(255, 255, 255, 220)); Util.oval(g, getX(), getY(), r * 0.45, r * 0.45);
    }

    protected Color getColor() { return color; }
    @Override public float getLightX() { return getX(); }
    @Override public float getLightY() { return getY(); }
    @Override public float getLightRadius() { return 34; }
    @Override public float getLightIntensity(float t) { return 0.5f; }
}
