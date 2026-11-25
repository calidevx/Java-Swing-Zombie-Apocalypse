package deadsignal.story;

import deadsignal.progress.PlayerProfile;
import deadsignal.weapon.Weapon;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.function.Supplier;

/** Unlocks a new gun. */
public final class WeaponReward extends Reward {
    private final Supplier<Weapon> factory;

    public WeaponReward(Supplier<Weapon> factory, String description) { super(description); this.factory = factory; }

    @Override public void grantTo(PlayerProfile profile) { profile.giveWeapon(factory.get()); }

    @Override public void drawIcon(Graphics2D g, int x, int y) {
        AffineTransform at = g.getTransform();
        g.translate(x, y); g.scale(1.6, 1.6); g.translate(-18, 0);
        factory.get().drawModel(g);
        g.setTransform(at);
    }
}
