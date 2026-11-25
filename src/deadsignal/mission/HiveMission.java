package deadsignal.mission;

import deadsignal.entity.item.HiveNode;
import deadsignal.game.GameWorld;

import java.util.ArrayList;
import java.util.List;

/** Destroy N hive nodes (their toughness scales with difficulty). */
public final class HiveMission extends Mission {
    public HiveMission(String description, int count) { super(description, count); }

    @Override protected void onStart(GameWorld w) {
        List<float[]> used = new ArrayList<>();
        for (int k = 0; k < getTarget(); k++) {
            float[] pos = w.pickReachableTile(12, 48, used, 13 * 32, true);
            used.add(pos);
            w.addItem(new HiveNode(pos[0], pos[1], 520 * w.getDifficulty().getEnemyHealth()));
        }
    }

    @Override public void onItemCompleted(GameWorld w, deadsignal.entity.item.ObjectiveItem item) { advance(w); }
}
