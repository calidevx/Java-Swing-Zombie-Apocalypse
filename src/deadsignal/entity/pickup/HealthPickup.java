package deadsignal.entity.pickup;

import deadsignal.entity.Player;
import deadsignal.game.GameWorld;
import deadsignal.gfx.PickupSprite;

import java.awt.Color;

public final class HealthPickup extends Pickup {
    public HealthPickup(float x, float y) { super(x, y); }

    @Override protected PickupSprite getSprite() { return PickupSprite.HEALTH; }
    @Override protected void applyTo(Player p, GameWorld w) {
        p.heal(25);
        w.addText("+25 HP", getX(), getY() - 10, new Color(120, 255, 140), 13);
        w.audio().play("heal", 0.6f, 1.2f);
    }
}
