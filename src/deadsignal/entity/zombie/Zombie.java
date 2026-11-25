package deadsignal.entity.zombie;

import deadsignal.core.Util;
import deadsignal.entity.Burnable;
import deadsignal.entity.Creature;
import deadsignal.entity.Decoy;
import deadsignal.entity.Entity;
import deadsignal.entity.DamageType;
import deadsignal.entity.Player;
import deadsignal.entity.pickup.AmmoPickup;
import deadsignal.entity.pickup.CashPickup;
import deadsignal.entity.pickup.GrenadePickup;
import deadsignal.entity.pickup.HealthPickup;
import deadsignal.entity.pickup.MagnetPickup;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;
import deadsignal.gfx.ParticleType;
import deadsignal.gfx.ZombieSprite;
import deadsignal.world.Tile;

import java.awt.*;
import java.util.List;

/**
 * ABSTRACT infected. Implements the shared AI loop (path-finding, crowd separation, melee)
 * as a TEMPLATE METHOD. Each zombie type extends this class and overrides only the hooks
 * that make it different - {@link #behave}, {@link #canMelee}, {@link #dropLoot}, {@link #renderOverlay}...
 */
public abstract class Zombie extends Creature implements Burnable {
    private final ZombieSprite sprite;
    private final int variant;
    private final float speed, attackDamage;
    private final int cashValue;
    private float angle, anim, attackCooldown, losTimer, groanTimer, burnTimer, knockX, knockY;
    private boolean lineOfSight;
    private float danceTimer, danceClock, frenzyTimer, stunTimer;

    protected Zombie(ZombieSprite sprite, float x, float y, float radius, float health, float speed, float damage, int cash) {
        super(x, y, radius, health);
        this.sprite = sprite;
        this.variant = Util.RNG.nextInt(3);
        this.speed = speed * Util.rf(0.85f, 1.15f);
        this.attackDamage = damage;
        this.cashValue = cash;
        this.angle = Util.rf(0, 6.28f);
        this.anim = Util.rf(0, 8);
        this.groanTimer = Util.rf(1, 8);
    }

    @Override
    public final void update(GameWorld w, float dt) {
        tickFlash(dt);
        attackCooldown -= dt; losTimer -= dt; groanTimer -= dt;
        if (burnTimer > 0) {
            burnTimer -= dt;
            if (Util.rf() < dt * 22)
                w.particles().spawn(ParticleType.FIRE, getX() + Util.rf(-getRadius(), getRadius()), getY() + Util.rf(-getRadius(), getRadius()), Util.rf(-15, 15), Util.rf(-50, -20), 0.45f, 5, new Color(255, 150, 40));
            takeDamage(14 * dt, DamageType.FIRE, w);
            if (isDead()) return;
        }
        if (danceTimer > 0) {                       // frozen in the Maestro's dance
            danceTimer -= dt; danceClock += dt;
            anim += dt * 9;
            angle += (float) Math.sin(danceClock * 8) * dt * 6;
            return;
        }
        if (stunTimer > 0) { stunTimer -= dt; return; }   // stunned by Shockwave
        if (frenzyTimer > 0) frenzyTimer -= dt;
        Player p = w.getPlayer();
        // A live decoy nearby steals this zombie's attention.
        Decoy decoy = w.getDecoy();
        Entity target = (decoy != null && !decoy.isDead() && distanceTo(decoy) < Decoy.LURE_RADIUS) ? decoy : p;
        boolean targetAlive = target != p || !p.isDead();
        float dx = target.getX() - getX(), dy = target.getY() - getY(), d = Math.max(0.1f, Util.len(dx, dy));
        if (losTimer <= 0) { losTimer = 0.25f + Util.rf() * 0.15f; lineOfSight = d < 700 && w.getMap().hasLineOfSight(getX(), getY(), target.getX(), target.getY()); }
        float[] dir = chooseDirection(w, targetAlive, dx, dy, d);
        float spd = speed;
        if (w.getMap().tileAt(getX(), getY()) == Tile.WATER) spd *= 0.7f;
        if (frenzyTimer > 0) spd *= 2f;
        spd = behave(w, dt, d, spd, dir);
        if (isDead()) return;
        // crowd separation
        float sx = 0, sy = 0;
        List<Zombie> others = w.getZombies();
        for (Zombie o : others) {
            if (o == this || o.isDead()) continue;
            float ox = getX() - o.getX(), oy = getY() - o.getY(), rr = getRadius() + o.getRadius(), dd = ox * ox + oy * oy;
            if (dd < rr * rr && dd > 0.01f) { float l = (float) Math.sqrt(dd), push = (rr - l) / rr; sx += ox / l * push; sy += oy / l * push; }
        }
        if (target == p && !p.isDead() && d < getRadius() + p.getRadius()) { float push = (getRadius() + p.getRadius() - d) / (getRadius() + p.getRadius()); sx -= dx / d * push * 1.5f; sy -= dy / d * push * 1.5f; }
        float mvx = dir[0] * spd + sx * 110 + knockX, mvy = dir[1] * spd + sy * 110 + knockY;
        float kd = Math.min(1, dt * 9); knockX -= knockX * kd; knockY -= knockY * kd;
        moveWithCollision(w.getMap(), mvx * dt, mvy * dt, getRadius());
        float face = d < 200 && targetAlive ? (float) Math.atan2(dy, dx) : (float) Math.atan2(dir[1], dir[0]);
        if (Math.abs(dir[0]) + Math.abs(dir[1]) > 0.01f) angle = Util.turn(angle, face, 6 * dt);
        anim += dt * spd / 7.5f;
        if (canMelee() && targetAlive && d < getRadius() + target.getRadius() + 5 && attackCooldown <= 0) {
            attackCooldown = 1.0f;
            if (target == decoy) decoy.takeDamage(attackDamage, DamageType.MELEE, w);
            else p.hurt(attackDamage, getX(), getY(), DamageType.MELEE, w);
        }
        if (groanTimer <= 0) {
            groanTimer = Util.rf(5, 14);
            if (d < 520 && Util.rf() < 0.45f) w.audio().play("groan", 0.35f * (1 - d / 520), Util.rf(0.8f, 1.2f));
        }
    }

    /** Default steering: straight at the player when visible, otherwise follow the flow field. */
    protected float[] chooseDirection(GameWorld w, boolean targetAlive, float dx, float dy, float d) {
        if (!targetAlive) return new float[]{(float) Math.cos(angle) * 0.2f, (float) Math.sin(angle) * 0.2f};
        if ((lineOfSight && d < 280) || d < 50) return new float[]{dx / d, dy / d};
        float[] f = w.getMap().flowDirection(getX(), getY());
        if (f != null) return f;
        return d < 1400 ? new float[]{dx / d, dy / d} : new float[]{0, 0};
    }

    /** Hook: special behaviour. May change dir[] in place; returns the speed to use. */
    protected float behave(GameWorld w, float dt, float dist, float speed, float[] dir) { return speed; }
    /** Hook: whether this zombie bites. */
    protected boolean canMelee() { return true; }
    /** Hook: 1 = normal knock-back, lower = heavier. */
    protected float getKnockbackResistance() { return 1f; }

    @Override public void applyImpulse(float ix, float iy) { float k = getKnockbackResistance(); knockX += ix * k; knockY += iy * k; }
    @Override public void ignite(float seconds) { burnTimer = Math.max(burnTimer, seconds); }
    /** Freezes this zombie into a dance (Maestro's Dead Man's Groove). */
    public void dance(float seconds) { danceTimer = Math.max(danceTimer, seconds); }
    /** Doubles movement speed for a while (Maestro's Crescendo). */
    public void frenzy(float seconds) { danceTimer = 0; frenzyTimer = Math.max(frenzyTimer, seconds); }
    public boolean isDancing() { return danceTimer > 0; }
    /** Stops the zombie completely (Shockwave skill). */
    public void stun(float seconds) { stunTimer = Math.max(stunTimer, seconds); }

    @Override
    protected void onDeath(GameWorld w, DamageType type) {
        remove();
        w.onZombieKilled(this, type);
        int blood = getRadius() > 18 ? 22 : 12;
        for (int i = 0; i < blood; i++)
            w.particles().spawn(ParticleType.BLOOD, getX(), getY(), Util.rf(-150, 150), Util.rf(-150, 150), Util.rf(0.3f, 0.8f), Util.rf(1.5f, 3.5f), new Color(140, 12, 12));
        for (int i = 0; i < 4; i++) {
            deadsignal.gfx.Particle c = w.particles().spawn(ParticleType.CHUNK, getX(), getY(), Util.rf(-120, 120), Util.rf(-120, 120), 0.7f, 2.5f, new Color(110, 30, 30));
            if (c != null) c.withSpin(Util.rf(-15, 15));
        }
        if (type != DamageType.PURGE) dropLoot(w, w.combo().getMultiplier());
    }

    /** Hook: what this zombie drops. Subclasses may add extra loot (call super). */
    protected void dropLoot(GameWorld w, float cashMultiplier) {
        w.addPickup(new CashPickup(getX(), getY(), Math.round(cashValue * cashMultiplier)));
        float r = Util.rf();
        if (r < 0.012f) { w.addPickup(new MagnetPickup(getX(), getY())); return; }   // rare!
        if (r < 0.09f) w.addPickup(new AmmoPickup(getX(), getY()));
        else if (r < 0.15f) w.addPickup(new HealthPickup(getX(), getY()));
        else if (r < 0.175f) w.addPickup(new GrenadePickup(getX(), getY()));
    }

    @Override
    public void render(Graphics2D g, Assets a) {
        g.setColor(new Color(0, 0, 0, 75)); Util.oval(g, getX() + 3, getY() + 4, getRadius() * 1.15, getRadius() * 0.95);
        if (frenzyTimer > 0) { g.setColor(new Color(255, 40, 30, 90)); Util.oval(g, getX(), getY(), getRadius() * 1.5, getRadius() * 1.5); }
        Util.drawRotated(g, a.zombie(sprite, variant, getAnimFrame(), isFlashing()), getX(), getY(), angle, 1f);
        if (stunTimer > 0) {
            float t = System.nanoTime() / 1e9f;
            for (int k = 0; k < 3; k++) { float an = t * 6 + k * 2.09f; Util.drawStar(g, getX() + (float) Math.cos(an) * 10, getY() - getRadius() - 6 + (float) Math.sin(an) * 3, 3.5f, new Color(170, 230, 255)); }
        }
        if (danceTimer > 0) {
            Color[] c = {new Color(255, 90, 200), new Color(90, 220, 255), new Color(255, 220, 80)};
            float bob = (float) Math.sin(danceClock * 8 + getX()) * 4;
            deadsignal.entity.projectile.MusicNote.drawNote(g, getX() + 4, getY() - getRadius() - 10 + bob, 1f, c[(int) (danceClock * 3 + getX()) % 3]);
        }
        renderOverlay(g);
    }

    /** Hook: extra visuals on top of the sprite (fuse ring, health bar...). */
    protected void renderOverlay(Graphics2D g) { }

    public ZombieSprite getSprite() { return sprite; }
    public int getVariant() { return variant; }
    public int getAnimFrame() { return ((int) anim) & 7; }
    public float getAngle() { return angle; }
    protected boolean hasLineOfSight() { return lineOfSight; }
}
