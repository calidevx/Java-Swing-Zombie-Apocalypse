package deadsignal.entity.projectile;

import deadsignal.entity.DamageType;
import deadsignal.entity.Damageable;
import deadsignal.entity.Entity;
import deadsignal.entity.Player;
import deadsignal.game.GameWorld;

/**
 * ABSTRACT base for everything that flies. The movement/collision loop in {@link #update}
 * is shared (final); what happens on impact is decided by each subclass via hooks.
 */
public abstract class Projectile extends Entity {
    private final boolean friendly;
    private float vx, vy, life, damage, age;
    private final float maxLife;

    protected Projectile(float x, float y, float radius, float angle, float speed, float life, float damage, boolean friendly) {
        super(x, y, radius);
        this.vx = (float) Math.cos(angle) * speed;
        this.vy = (float) Math.sin(angle) * speed;
        this.life = life; this.maxLife = life; this.damage = damage; this.friendly = friendly;
    }

    @Override
    public final void update(GameWorld w, float dt) {
        age += dt; life -= dt;
        steer(w, dt);
        int steps = 2; float sdt = dt / steps;
        for (int s = 0; s < steps; s++) {
            setPosition(getX() + vx * sdt, getY() + vy * sdt);
            if (w.getMap().isSolidAt(getX(), getY())) { onHitWall(w); remove(); return; }
            if (friendly) {
                for (Damageable d : w.getDamageables()) {
                    if (!d.isTargetable()) continue;
                    float rr = d.getRadius() + getRadius(), dx = d.getX() - getX(), dy = d.getY() - getY();
                    if (dx * dx + dy * dy < rr * rr && onHitTarget(d, w)) { remove(); return; }
                }
            } else {
                Player p = w.getPlayer();
                float rr = p.getRadius() + getRadius() - 2, dx = p.getX() - getX(), dy = p.getY() - getY();
                if (!p.isDead() && !p.isRolling() && dx * dx + dy * dy < rr * rr) { onHitPlayer(p, w); remove(); return; }
            }
        }
        if (life <= 0) { onExpire(w); remove(); }
    }

    /** Hook: change velocity each tick (drag, homing...). */
    protected void steer(GameWorld w, float dt) { }
    /** Hook: hit a wall. Default behaves like expiring. */
    protected void onHitWall(GameWorld w) { onExpire(w); }
    /** Hook: lifetime ran out. */
    protected void onExpire(GameWorld w) { }
    /** Hook for friendly projectiles. @return true if the projectile is used up. */
    protected boolean onHitTarget(Damageable target, GameWorld w) { target.takeDamage(damage, DamageType.PROJECTILE, w); return true; }
    /** Hook for enemy projectiles. */
    protected void onHitPlayer(Player p, GameWorld w) { p.hurt(damage, getX() - vx * 0.05f, getY() - vy * 0.05f, DamageType.PROJECTILE, w); onExpire(w); }

    public boolean isFriendly() { return friendly; }
    protected float getVx() { return vx; }
    protected float getVy() { return vy; }
    protected float getSpeed() { return (float) Math.sqrt(vx * vx + vy * vy); }
    protected void setVelocity(float nvx, float nvy) { vx = nvx; vy = nvy; }
    protected float getAge() { return age; }
    protected float getAgeFraction() { return age / maxLife; }
    protected float getDamage() { return damage; }
    protected void setDamage(float d) { damage = d; }
    protected void setLife(float l) { life = l; }
}
