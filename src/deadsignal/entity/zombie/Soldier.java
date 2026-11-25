package deadsignal.entity.zombie;

import deadsignal.gfx.ZombieSprite;

/** MULTI-LEVEL INHERITANCE: Entity -> Creature -> Zombie -> Walker -> Soldier. Tougher, helmeted walker. */
public final class Soldier extends Walker {
    public Soldier(float x, float y, float hpMul, float speedMul) {
        super(ZombieSprite.SOLDIER, x, y, 58 * hpMul, 60 * speedMul, 13, 9);
    }
}
