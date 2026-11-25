package deadsignal.mission;

import deadsignal.entity.boss.Boss;
import deadsignal.entity.boss.BossType;
import deadsignal.game.GameWorld;

/** Defeat a boss. Zombies spawn more slowly while it is alive. */
public final class BossMission extends Mission {
    private final BossType type;

    public BossMission(String description, BossType type) { super(description, 1); this.type = type; }

    @Override protected void onStart(GameWorld w) { w.spawnBoss(type); }
    @Override public void onBossDefeated(GameWorld w, Boss boss) { advance(w); }
    @Override public float spawnRateMultiplier() { return isComplete() ? 1f : 0.52f; }
    @Override public String getHudText() { return getDescription(); }
}
