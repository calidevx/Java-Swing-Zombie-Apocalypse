package deadsignal.entity.zombie;

import deadsignal.gfx.ZombieSprite;

/** The classic shambler. Also the parent class of {@link Soldier}. */
public class Walker extends Zombie {
    public Walker(float x, float y, float hpMul, float speedMul) {
        this(ZombieSprite.WALKER, x, y, 40 * hpMul, 52 * speedMul, 12, 6);
    }

    /** Protected constructor so subclasses can reuse the walker body with different stats. */
    protected Walker(ZombieSprite sprite, float x, float y, float health, float speed, float damage, int cash) {
        super(sprite, x, y, 12, health, speed, damage, cash);
    }
}
