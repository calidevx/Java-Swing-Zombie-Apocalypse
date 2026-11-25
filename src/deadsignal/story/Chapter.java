package deadsignal.story;

import deadsignal.mission.Mission;
import deadsignal.world.Theme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** One chapter of the campaign. Configured by {@link Campaign}; read-only for everyone else. */
public final class Chapter {
    private final String title, subtitle;
    private final Theme theme;
    private final long seed;
    private float spawnInterval = 1.2f;
    private int maxZombies = 30, prePopulate = 12;
    private SpawnTable spawnTable = new SpawnTable().add(deadsignal.entity.zombie.ZombieType.WALKER, 1);
    private final List<DialogueLine> intro = new ArrayList<>(), outro = new ArrayList<>();
    private final List<Mission> missions = new ArrayList<>();
    private Reward reward;
    private String label = "";
    private boolean bonus;

    Chapter(String title, String subtitle, Theme theme, long seed) { this.title = title; this.subtitle = subtitle; this.theme = theme; this.seed = seed; }

    // package-private fluent configuration (only Campaign may build chapters)
    Chapter spawning(float interval, int max, int pre, SpawnTable table) { spawnInterval = interval; maxZombies = max; prePopulate = pre; spawnTable = table; return this; }
    Chapter intro(DialogueLine... lines) { intro.addAll(Arrays.asList(lines)); return this; }
    Chapter outro(DialogueLine... lines) { outro.addAll(Arrays.asList(lines)); return this; }
    Chapter mission(Mission m) { missions.add(m); return this; }
    Chapter reward(Reward r) { reward = r; return this; }
    /** Marks this as an optional bonus stage (shown as "BONUS STAGE" instead of a chapter number). */
    Chapter bonus() { bonus = true; return this; }
    void setLabel(String l) { label = l; }

    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public Theme getTheme() { return theme; }
    public long getSeed() { return seed; }
    public float getSpawnInterval() { return spawnInterval; }
    public int getMaxZombies() { return maxZombies; }
    public int getPrePopulate() { return prePopulate; }
    public SpawnTable getSpawnTable() { return spawnTable; }
    public List<DialogueLine> getIntro() { return Collections.unmodifiableList(intro); }
    public List<DialogueLine> getOutro() { return Collections.unmodifiableList(outro); }
    public List<Mission> getMissions() { return Collections.unmodifiableList(missions); }
    public Reward getReward() { return reward; }
    /** "CHAPTER 3" or "BONUS STAGE". */
    public String getLabel() { return label; }
    public boolean isBonus() { return bonus; }
}
