package deadsignal.entity;

/** What kind of harm is being dealt. Lets each Creature react differently (polymorphism). */
public enum DamageType {
    BULLET, FIRE, EXPLOSION, MELEE, ACID, PROJECTILE, ENERGY, PURGE;

    /** Damage-over-time sources ignore the brief invulnerability window after a hit. */
    public boolean isDamageOverTime() { return this == FIRE || this == ACID; }
}
