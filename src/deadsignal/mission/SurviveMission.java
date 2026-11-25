package deadsignal.mission;

import deadsignal.game.GameWorld;

/** Stay alive for N seconds while zombies pour in much faster. */
public final class SurviveMission extends Mission {
    private float elapsed;

    public SurviveMission(String description, int seconds) { super(description, seconds); }

    @Override protected void onStart(GameWorld w) { elapsed = 0; }
    @Override public void update(GameWorld w, float dt) { elapsed += dt; setProgress(w, (int) elapsed); }
    @Override public float spawnRateMultiplier() { return isComplete() ? 1f : 3.1f; }
    @Override public int extraZombieCap() { return isComplete() ? 0 : 22; }
    @Override public String getHudText() { return getDescription() + "  (" + Math.max(0, getTarget() - (int) elapsed) + "s)"; }
}
