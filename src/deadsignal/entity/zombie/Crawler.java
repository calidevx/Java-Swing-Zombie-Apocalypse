package deadsignal.entity.zombie;

import deadsignal.gfx.ZombieSprite;

/** Tiny, low and very fast - spawned in swarms by the Broodmother and hive nodes. */
public final class Crawler extends Zombie {
    public Crawler(float x, float y, float hpMul, float speedMul) {
        super(ZombieSprite.CRAWLER, x, y, 9, 18 * hpMul, 132 * speedMul, 7, 4);
    }
}
