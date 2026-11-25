package deadsignal.story;

import deadsignal.progress.PlayerProfile;

/** Hands out grenades and medkits. */
public final class SupplyReward extends Reward {
    private final int grenades, medkits;

    public SupplyReward(int grenades, int medkits, String description) { super(description); this.grenades = grenades; this.medkits = medkits; }

    @Override public void grantTo(PlayerProfile profile) { profile.addGrenades(grenades); profile.addMedkits(medkits); }
}
