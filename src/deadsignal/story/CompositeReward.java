package deadsignal.story;

import deadsignal.progress.PlayerProfile;

import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.List;

/** COMPOSITE PATTERN: a reward made of several rewards, treated exactly like a single Reward. */
public final class CompositeReward extends Reward {
    private final List<Reward> parts;

    public CompositeReward(String description, Reward... parts) { super(description); this.parts = Arrays.asList(parts); }

    @Override public void grantTo(PlayerProfile profile) { for (Reward r : parts) r.grantTo(profile); }

    @Override public void drawIcon(Graphics2D g, int x, int y) { if (!parts.isEmpty()) parts.get(0).drawIcon(g, x, y); }
}
