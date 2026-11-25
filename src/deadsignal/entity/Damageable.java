package deadsignal.entity;

import deadsignal.game.GameWorld;

/**
 * ABSTRACTION: anything a player projectile can hit. Zombies, bosses and hive nodes are
 * completely different classes, yet bullets treat them all the same way through this interface.
 */
public interface Damageable {
    float getX();
    float getY();
    float getRadius();
    boolean isDead();
    void takeDamage(float amount, DamageType type, GameWorld world);

    /** Some targets can be untargetable for a moment (e.g. a teleporting boss). */
    default boolean isTargetable() { return !isDead(); }
    /** Hook: complete immunity to a damage type (e.g. a ghostly boss ignoring bullets). */
    default boolean isImmuneTo(DamageType type) { return false; }
    /** Knock-back hook; default does nothing (immovable objects). */
    default void applyImpulse(float ix, float iy) { }
}
