package deadsignal.entity.pickup;

import deadsignal.entity.Player;
import deadsignal.game.GameWorld;
import deadsignal.gfx.PickupSprite;

import java.awt.Color;

/** RARE power-up: for a while every cash, ammo and health pickup on screen flies to the player. */
public final class MagnetPickup extends Pickup {
    public static final float DURATION = 20f;

    public MagnetPickup(float x, float y) { super(x, y); }

    @Override protected PickupSprite getSprite() { return PickupSprite.MAGNET; }
    @Override protected void applyTo(Player p, GameWorld w) {
        p.activateMagnet(DURATION);
        w.addText("MAGNET!", getX(), getY() - 14, new Color(120, 200, 255), 18);
        w.audio().play("medal", 0.7f, 1.3f);
    }
}
