package deadsignal.story;

import deadsignal.progress.PlayerProfile;

import java.awt.Graphics2D;

/** ABSTRACT chapter reward. The results screen just calls grantTo()/drawIcon() on whatever it gets. */
public abstract class Reward {
    private final String description;

    protected Reward(String description) { this.description = description; }

    public abstract void grantTo(PlayerProfile profile);
    /** Optional icon; default draws nothing. */
    public void drawIcon(Graphics2D g, int x, int y) { }

    public String getDescription() { return description; }
}
