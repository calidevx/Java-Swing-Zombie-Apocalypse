package deadsignal.entity.projectile;

import deadsignal.entity.Player;
import deadsignal.game.GameWorld;

import java.awt.Color;

/** INHERITANCE + super call: an EnergyOrb that also slows the player on hit. */
public final class PoisonDart extends EnergyOrb {
    public PoisonDart(float x, float y, float angle) { super(x, y, angle, 330, 7, 5, new Color(150, 240, 70)); }

    @Override protected void onHitPlayer(Player p, GameWorld w) {
        super.onHitPlayer(p, w);
        p.applySlow(0.65f, 1.6f);
    }
}
