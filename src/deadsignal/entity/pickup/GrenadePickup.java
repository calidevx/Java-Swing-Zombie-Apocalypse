package deadsignal.entity.pickup;

import deadsignal.entity.Player;
import deadsignal.game.GameWorld;
import deadsignal.gfx.PickupSprite;

import java.awt.Color;

public final class GrenadePickup extends Pickup {
    public GrenadePickup(float x, float y) { super(x, y); }

    @Override protected PickupSprite getSprite() { return PickupSprite.GRENADE; }
    @Override protected void applyTo(Player p, GameWorld w) {
        p.getProfile().addGrenades(1);
        w.addText("+GRENADE", getX(), getY() - 10, new Color(170, 220, 140), 13);
        w.audio().play("pickup");
    }
}
