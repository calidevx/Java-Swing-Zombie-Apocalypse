package deadsignal.story;

import deadsignal.entity.zombie.ZombieType;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;

/** Weighted random choice of which zombie types appear in a chapter. */
public final class SpawnTable {
    private final Map<ZombieType, Integer> weights = new EnumMap<>(ZombieType.class);
    private int total;

    public SpawnTable add(ZombieType type, int weight) { weights.merge(type, weight, Integer::sum); total += weight; return this; }

    public ZombieType pick(Random r) {
        int roll = r.nextInt(Math.max(1, total));
        for (Map.Entry<ZombieType, Integer> e : weights.entrySet()) { roll -= e.getValue(); if (roll < 0) return e.getKey(); }
        return ZombieType.WALKER;
    }
}
