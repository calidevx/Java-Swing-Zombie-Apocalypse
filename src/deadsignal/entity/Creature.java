package deadsignal.entity;

import deadsignal.game.GameWorld;

/**
 * INHERITANCE level 2: a living Entity with health.
 * {@link #takeDamage} is a TEMPLATE METHOD - the damage pipeline is fixed here, while
 * subclasses customise individual steps by overriding the protected hooks (polymorphism).
 * Health itself is private (encapsulation): it can only change through takeDamage()/heal().
 */
public abstract class Creature extends Entity implements Damageable {
    private float health, maxHealth, flashTimer;

    protected Creature(float x, float y, float radius, float maxHealth) {
        super(x, y, radius);
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    @Override
    public final void takeDamage(float amount, DamageType type, GameWorld world) {
        if (isDead() || isImmuneTo(type) || !canBeDamaged(type)) return;
        float dmg = modifyIncomingDamage(amount, type, world);
        if (dmg <= 0) return;
        health -= dmg;
        flashTimer = 0.08f;
        onHurt(dmg, type, world);
        if (health <= 0) { health = 0; onDeath(world, type); }
    }

    /** Hook: return false to ignore this hit (e.g. invulnerability frames). */
    protected boolean canBeDamaged(DamageType type) { return true; }
    /** Hook: scale damage (armor, weak spots, resistances). */
    protected float modifyIncomingDamage(float amount, DamageType type, GameWorld world) { return amount; }
    /** Hook: react to a successful hit. */
    protected void onHurt(float damage, DamageType type, GameWorld world) { }
    /** Every creature must decide what happens when it dies. */
    protected abstract void onDeath(GameWorld world, DamageType type);

    /** Instantly kills this creature (used by self-destructing enemies). */
    protected final void kill(GameWorld world, DamageType type) {
        if (isDead()) return;
        health = 0;
        onDeath(world, type);
    }

    public void heal(float amount) { if (!isDead()) health = Math.min(maxHealth, health + amount); }
    protected void setMaxHealth(float max, boolean refill) { maxHealth = max; if (refill) health = max; }
    protected void tickFlash(float dt) { flashTimer -= dt; }

    public float getHealth() { return health; }
    public float getMaxHealth() { return maxHealth; }
    public float getHealthFraction() { return health / maxHealth; }
    public boolean isFlashing() { return flashTimer > 0; }
    @Override public boolean isDead() { return health <= 0; }
}
