package deadsignal.entity.zombie;

import deadsignal.gfx.ZombieSprite;

/** Fast and fragile. */
public final class Runner extends Zombie {
    public Runner(float x, float y, float hpMul, float speedMul) {
        super(ZombieSprite.RUNNER, x, y, 11, 28 * hpMul, 122 * speedMul, 9, 8);
    }
}
