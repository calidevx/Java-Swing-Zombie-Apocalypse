package deadsignal.entity.projectile;

import deadsignal.core.Util;
import deadsignal.entity.DamageType;
import deadsignal.entity.Damageable;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;
import deadsignal.gfx.ParticleType;

import java.awt.*;
import java.util.HashSet;
import java.util.Set;

/** A player bullet. Can pierce several targets and pushes them back. */
public final class Bullet extends Projectile {
    private final float knockback;
    private final int shotId;
    private int pierce;
    private final Set<Damageable> alreadyHit = new HashSet<>();

    public Bullet(float x, float y, float angle, float speed, float life, float damage, float knockback, int pierce, int shotId) {
        super(x, y, 3, angle, speed, life, damage, true);
        this.knockback = knockback; this.pierce = pierce; this.shotId = shotId;
    }

    @Override
    protected boolean onHitTarget(Damageable target, GameWorld w) {
        if (!alreadyHit.add(target)) return false;
        if (target.isImmuneTo(DamageType.BULLET)) {   // ghostly target: the round passes straight through
            w.particles().spawn(ParticleType.GLOW, getX(), getY(), Util.rf(-40, 40), Util.rf(-40, 40), 0.3f, 2.5f, new Color(170, 230, 255));
            if (Util.rf() < 0.08f) w.addText("IMMUNE", getX(), getY() - 14, new Color(190, 235, 255), 13);
            return false;
        }
        float l = Math.max(1, getSpeed());
        target.applyImpulse(getVx() / l * knockback * 3.5f, getVy() / l * knockback * 3.5f);
        target.takeDamage(getDamage(), DamageType.BULLET, w);
        w.onPlayerDamageDealt(getDamage());
        w.stats().registerHit(shotId);
        for (int i = 0; i < 3; i++)
            w.particles().spawn(ParticleType.BLOOD, getX(), getY(), getVx() / l * Util.rf(40, 160) + Util.rf(-40, 40), getVy() / l * Util.rf(40, 160) + Util.rf(-40, 40),
                    Util.rf(0.3f, 0.6f), Util.rf(1.5f, 3f), new Color(150, 15, 15));
        w.audio().play("hit", 0.5f, 1f);
        if (pierce <= 0) return true;
        pierce--;
        setDamage(getDamage() * 0.75f);
        return false;
    }

    @Override
    protected void onHitWall(GameWorld w) {
        for (int k = 0; k < 4; k++)
            w.particles().spawn(ParticleType.SPARK, getX() - getVx() * 0.01f, getY() - getVy() * 0.01f, -getVx() * Util.rf(0.05f, 0.25f) + Util.rf(-80, 80),
                    -getVy() * Util.rf(0.05f, 0.25f) + Util.rf(-80, 80), 0.2f, 1.5f, new Color(255, 220, 140));
        w.particles().spawn(ParticleType.SMOKE, getX(), getY(), 0, 0, 0.4f, 3, new Color(140, 130, 120, 110));
    }

    @Override
    public void render(Graphics2D g, Assets a) {
        float l = Math.max(1, getSpeed()), tl = Math.min(18, getAge() * l);
        float tx = getX() - getVx() / l * tl, ty = getY() - getVy() / l * tl;
        g.setColor(new Color(255, 210, 120, 120)); g.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Util.line(g, tx, ty, getX(), getY());
        g.setColor(new Color(255, 255, 220)); g.setStroke(new BasicStroke(1.5f));
        Util.line(g, getX() - getVx() / l * tl * 0.6f, getY() - getVy() / l * tl * 0.6f, getX(), getY());
    }
}
