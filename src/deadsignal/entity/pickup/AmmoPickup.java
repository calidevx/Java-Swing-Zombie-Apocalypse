package deadsignal.entity.pickup;

import deadsignal.entity.Player;
import deadsignal.game.GameWorld;
import deadsignal.gfx.PickupSprite;

import java.awt.Color;

public final class AmmoPickup extends Pickup {
    public AmmoPickup(float x, float y) { super(x, y); }

    @Override protected PickupSprite getSprite() { return PickupSprite.AMMO; }
    @Override protected void applyTo(Player p, GameWorld w) {
        boolean any = p.getProfile().addAmmoToAll(0.8f);
        if (!any) p.getProfile().addCash(15);
        w.addText(any ? "+AMMO" : "+$15", getX(), getY() - 10, new Color(230, 210, 120), 13);
        w.audio().play("pickup");
    }
}
