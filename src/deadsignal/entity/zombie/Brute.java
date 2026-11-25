package deadsignal.entity.zombie;

import deadsignal.entity.pickup.CashPickup;
import deadsignal.game.GameWorld;
import deadsignal.gfx.ZombieSprite;

import java.awt.*;

/** Huge and slow. Resists knock-back, shows a health bar and drops extra cash. */
public final class Brute extends Zombie {
    public Brute(float x, float y, float hpMul, float speedMul) {
        super(ZombieSprite.BRUTE, x, y, 21, 230 * hpMul, 44 * speedMul, 26, 25);
    }

    @Override protected float getKnockbackResistance() { return 0.3f; }

    @Override protected void dropLoot(GameWorld w, float mult) {
        super.dropLoot(w, mult);
        w.addPickup(new CashPickup(getX(), getY(), Math.round(20 * mult)));
        if (deadsignal.core.Util.rf() < 0.05f) w.addPickup(new deadsignal.entity.pickup.MagnetPickup(getX(), getY()));
    }

    @Override protected void renderOverlay(Graphics2D g) {
        if (getHealth() >= getMaxHealth()) return;
        int x = (int) getX() - 16, y = (int) (getY() - getRadius() - 12);
        g.setColor(new Color(0, 0, 0, 150)); g.fillRect(x, y, 32, 4);
        g.setColor(new Color(220, 60, 50)); g.fillRect(x, y, (int) (32 * getHealthFraction()), 4);
    }
}
