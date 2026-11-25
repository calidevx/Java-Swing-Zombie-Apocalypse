package deadsignal.entity.projectile;

import deadsignal.core.Util;
import deadsignal.entity.Burnable;
import deadsignal.entity.DamageType;
import deadsignal.entity.Damageable;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;
import deadsignal.gfx.LightEmitter;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/** A growing puff of fire. Passes through enemies, igniting every {@link Burnable} it touches. */
public final class FlameJet extends Projectile implements LightEmitter {
    private final Set<Damageable> touched = new HashSet<>();

    public FlameJet(float x, float y, float angle, float speed, float life, float damage) {
        super(x, y, 6, angle, speed, life, damage, true);
    }

    @Override protected void steer(GameWorld w, float dt) {
        float k = 1 - dt * 2.2f;
        setVelocity(getVx() * k, getVy() * k);
        setRadius(6 + getAge() * 34);
    }

    @Override protected boolean onHitTarget(Damageable target, GameWorld w) {
        if (!touched.add(target)) return false;
        target.takeDamage(getDamage(), DamageType.FIRE, w);
        w.onPlayerDamageDealt(getDamage());
        if (target instanceof Burnable) ((Burnable) target).ignite(2.6f);
        return false; // flames keep going
    }

    @Override
    public void render(Graphics2D g, Assets a) {
        float k = getAgeFraction();
        Color c = k < 0.3f ? Util.mix(new Color(255, 250, 200), new Color(255, 170, 40), k / 0.3f)
                : Util.mix(new Color(255, 170, 40), new Color(160, 30, 10), (k - 0.3f) / 0.7f);
        g.setColor(Util.alpha(c, (int) (220 * (1 - k * 0.7f))));
        Util.oval(g, getX(), getY(), getRadius(), getRadius());
    }

    @Override public float getLightX() { return getX(); }
    @Override public float getLightY() { return getY(); }
    @Override public float getLightRadius() { return 50; }
    @Override public float getLightIntensity(float t) { return 0.5f; }
}
