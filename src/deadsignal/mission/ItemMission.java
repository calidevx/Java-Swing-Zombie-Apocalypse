package deadsignal.mission;

import deadsignal.entity.item.ObjectiveItem;
import deadsignal.game.GameWorld;

import java.util.ArrayList;
import java.util.List;

/** Find / use / collect N objects scattered across the map. */
public class ItemMission extends Mission {
    private final ItemFactory factory;
    private final int minSteps, maxSteps;

    public ItemMission(String description, int count, ItemFactory factory, int minSteps, int maxSteps) {
        super(description, count);
        this.factory = factory; this.minSteps = minSteps; this.maxSteps = maxSteps;
    }

    @Override protected void onStart(GameWorld w) {
        List<float[]> used = new ArrayList<>();
        for (int k = 0; k < getTarget(); k++) {
            float[] pos = w.pickReachableTile(minSteps, maxSteps, used, 13 * 32, true);
            used.add(pos);
            w.addItem(factory.create(pos[0], pos[1]));
        }
    }

    @Override public void onItemCompleted(GameWorld w, ObjectiveItem item) { advance(w); }
    protected ItemFactory getFactory() { return factory; }
}
