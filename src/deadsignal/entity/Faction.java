package deadsignal.entity;

/** Who caused an explosion - decides who it can hurt. */
public enum Faction {
    /** Player grenades: hurt every hostile, never the player. */
    PLAYER,
    /** Exploding infected: hurt other zombies and the player, not bosses. */
    INFECTED,
    /** Boss weapons: only hurt the player. */
    BOSS
}
