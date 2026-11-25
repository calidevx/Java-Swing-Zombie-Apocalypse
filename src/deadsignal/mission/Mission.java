package deadsignal.mission;

import deadsignal.entity.boss.Boss;
import deadsignal.entity.item.ObjectiveItem;
import deadsignal.game.GameWorld;
import deadsignal.story.DialogueLine;

import java.util.*;

/**
 * ABSTRACT objective. Progress and completion are private and can only advance through
 * {@link #advance}/{@link #setProgress} (encapsulation). Subclasses react to game events by
 * overriding the onXxx() hooks - the world simply forwards every event to the current mission
 * without knowing what kind of mission it is (polymorphism).
 */
public abstract class Mission {
    private final String description;
    private final int target;
    private int progress;
    private boolean complete;
    private final List<DialogueLine> briefing = new ArrayList<>(), radioOnStart = new ArrayList<>(), radioOnComplete = new ArrayList<>();
    private final Map<Integer, List<DialogueLine>> progressRadio = new HashMap<>();

    protected Mission(String description, int target) { this.description = description; this.target = target; }

    // ------------------------------------------------------------------ fluent configuration
    /** Blocking dialogue shown before the mission begins. */
    public Mission briefing(DialogueLine... lines) { briefing.addAll(Arrays.asList(lines)); return this; }
    /** Non-blocking radio chatter when the mission begins. */
    public Mission radio(DialogueLine... lines) { radioOnStart.addAll(Arrays.asList(lines)); return this; }
    /** Radio chatter when the mission completes. */
    public Mission completion(DialogueLine... lines) { radioOnComplete.addAll(Arrays.asList(lines)); return this; }
    /** Radio chatter when progress reaches the given value. */
    public Mission onProgress(int value, DialogueLine... lines) { progressRadio.put(value, Arrays.asList(lines)); return this; }

    // ------------------------------------------------------------------ life-cycle
    /** Resets state (missions are reused when a chapter is retried) and sets the mission up. */
    public final void start(GameWorld w) {
        progress = 0;
        complete = false;
        onStart(w);
        w.radio().queue(radioOnStart);
    }

    /** Hook: place items, spawn the boss, etc. */
    protected abstract void onStart(GameWorld w);
    public void update(GameWorld w, float dt) { }

    // ------------------------------------------------------------------ event hooks (default: ignore)
    public void onZombieKilled(GameWorld w) { }
    public void onItemCompleted(GameWorld w, ObjectiveItem item) { }
    public void onBossDefeated(GameWorld w, Boss boss) { }

    /** Hook: how much faster zombies spawn during this mission. */
    public float spawnRateMultiplier() { return 1f; }
    /** Hook: extra zombies allowed alive during this mission. */
    public int extraZombieCap() { return 0; }

    // ------------------------------------------------------------------ progress (encapsulated)
    protected final void advance(GameWorld w) { setProgress(w, progress + 1); }
    protected final void setProgress(GameWorld w, int value) {
        if (complete) return;
        while (progress < value && progress < target) {
            progress++;
            List<DialogueLine> lines = progressRadio.get(progress);
            if (lines != null) w.radio().queue(lines);
        }
        if (progress >= target) { complete = true; w.radio().queue(radioOnComplete); w.onMissionComplete(this); }
    }

    /** Developer/test helper: instantly finish this mission. */
    public final void forceComplete(GameWorld w) { setProgress(w, target); }

    public String getHudText() { return target > 1 ? description + "  (" + progress + "/" + target + ")" : description; }
    public String getDescription() { return description; }
    public int getProgress() { return progress; }
    public int getTarget() { return target; }
    public boolean isComplete() { return complete; }
    public List<DialogueLine> getBriefing() { return Collections.unmodifiableList(briefing); }
}
