package deadsignal.weapon;

import deadsignal.core.Util;
import deadsignal.game.GameWorld;

import java.awt.*;

/** High rate of fire, low damage per bullet. */
public final class SubmachineGun extends Weapon {
    public SubmachineGun() { super("MP5 SMG", 2, 30, 210, 11f, 1.5f, 3, 1.5f, 28, "smg"); }

    @Override protected Weapon newInstance() { return new SubmachineGun(); }

    @Override protected void fireProjectiles(GameWorld w, float x, float y, float aim, float dmg, int shotId) {
        fireBullet(w, x, y, aim + Util.gauss() * 0.085f, 980, 0.62f, 14 * dmg, 35, 0, shotId);
    }

    @Override public void drawModel(Graphics2D g) {
        g.setColor(new Color(28, 28, 32)); g.fillRect(6, -3, 19, 6);
        g.fillRect(13, 3, 4, 6); g.fillRect(25, -1, 4, 2);
        g.setColor(new Color(70, 70, 76)); g.fillRect(6, -3, 19, 1);
    }
}
