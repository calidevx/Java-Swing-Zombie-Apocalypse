package deadsignal.mission;

import deadsignal.game.GameWorld;

/** Kill N infected. */
public final class KillMission extends Mission {
    public KillMission(String description, int kills) { super(description, kills); }

    @Override protected void onStart(GameWorld w) { }
    @Override public void onZombieKilled(GameWorld w) { advance(w); }
}
