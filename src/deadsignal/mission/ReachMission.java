package deadsignal.mission;

import deadsignal.entity.item.ExitZone;
import deadsignal.entity.item.ObjectiveItem;
import deadsignal.game.GameWorld;

/** Get to the extraction point. */
public final class ReachMission extends Mission {
    private final String label;

    public ReachMission(String description, String label) { super(description, 1); this.label = label; }

    @Override protected void onStart(GameWorld w) {
        float[] pos = w.pickReachableTile(22, 40, null, 0, true);
        w.addItem(new ExitZone(pos[0], pos[1], label));
    }

    @Override public void onItemCompleted(GameWorld w, ObjectiveItem item) { if (item instanceof ExitZone) advance(w); }
}
