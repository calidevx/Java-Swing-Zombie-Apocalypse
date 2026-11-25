package deadsignal.story;

import deadsignal.progress.PlayerProfile;

/** A lump-sum cash reward. */
public final class CashReward extends Reward {
    private final int amount;

    public CashReward(int amount) { super("$" + amount); this.amount = amount; }

    @Override public void grantTo(PlayerProfile profile) { profile.addCash(amount); }
}
