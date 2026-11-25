package deadsignal.entity.pickup;

import deadsignal.entity.Player;
import deadsignal.game.GameWorld;
import deadsignal.gfx.PickupSprite;

import java.awt.Color;

public final class CashPickup extends Pickup {
    private final int value;

    public CashPickup(float x, float y, int value) { super(x, y); this.value = value; }

    @Override protected PickupSprite getSprite() { return PickupSprite.CASH; }
    @Override protected void applyTo(Player p, GameWorld w) {
        p.getProfile().addCash(value);
        w.addText("+$" + value, getX(), getY() - 10, new Color(255, 220, 90), 12);
        w.audio().play("cash", 0.4f, 1f);
    }
}
