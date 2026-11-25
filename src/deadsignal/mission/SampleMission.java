package deadsignal.mission;

import deadsignal.entity.item.BloodSample;
import deadsignal.game.GameWorld;

/** INHERITANCE: an ItemMission that places its single item where the last boss died. */
public final class SampleMission extends ItemMission {
    public SampleMission(String description) { super(description, 1, BloodSample::new, 0, 0); }

    @Override protected void onStart(GameWorld w) {
        float[] pos = w.reachableNear(w.getLastBossX(), w.getLastBossY());
        w.addItem(getFactory().create(pos[0], pos[1]));
    }
}
