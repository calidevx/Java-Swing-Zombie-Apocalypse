package deadsignal.game;

import deadsignal.core.Util;
import deadsignal.entity.zombie.ZombieType;
import deadsignal.story.Chapter;

/** Decides when and where new infected appear (always off-screen). */
final class SpawnDirector {
    private float timer = 3f;

    void update(GameWorld w, float dt) {
        if (w.getPlayer().isDead()) return;
        Chapter ch = w.getChapter();
        float interval = ch.getSpawnInterval() / w.getDifficulty().getSpawnRate();
        int max = w.getMaxZombies();
        if (w.getMission() != null && w.isMissionActive()) {
            interval /= w.getMission().spawnRateMultiplier();
            max += w.getMission().extraZombieCap();
        }
        if (w.getBoss() != null) max /= 2;
        timer -= dt;
        if (timer > 0 || w.zombieCount() >= max) return;
        timer = interval * Util.rf(0.6f, 1.4f);
        float[] pos = w.findSpawnPoint(430, 820, true);
        if (pos == null) return;
        int group = Util.ri(1, w.getMission() != null && w.getMission().extraZombieCap() > 0 ? 4 : 3);
        for (int i = 0; i < group; i++) {
            float x = pos[0] + Util.rf(-12, 12), y = pos[1] + Util.rf(-12, 12);
            if (!w.getMap().collides(x, y, 12)) {
                ZombieType t = ch.getSpawnTable().pick(Util.RNG);
                w.spawnZombie(t, x, y);
            }
        }
    }
}
