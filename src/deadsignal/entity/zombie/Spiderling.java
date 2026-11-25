package deadsignal.entity.zombie;

import deadsignal.gfx.ZombieSprite;

/** Hatchling of the Broodmother: tiny, fast and fragile. */
public final class Spiderling extends Zombie {
    public Spiderling(float x, float y, float hpMul, float speedMul) {
        super(ZombieSprite.SPIDERLING, x, y, 8, 16 * hpMul, 142 * speedMul, 6, 3);
    }
}
