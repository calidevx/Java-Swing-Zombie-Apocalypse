package deadsignal.story;

import deadsignal.progress.PlayerProfile;

/** A purely narrative reward (the ending). */
public final class StoryReward extends Reward {
    public StoryReward(String description) { super(description); }

    @Override public void grantTo(PlayerProfile profile) { }
}
