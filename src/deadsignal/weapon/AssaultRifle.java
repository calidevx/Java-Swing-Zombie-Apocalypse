package deadsignal.weapon;

import deadsignal.core.Util;
import deadsignal.game.GameWorld;

import java.awt.*;

/** Accurate, hard-hitting and its rounds pierce through one extra target. */
public final class AssaultRifle extends Weapon {
    public AssaultRifle() { super("M4 RIFLE", 3, 30, 180, 7.5f, 1.9f, 3, 2.5f, 36, "rifle"); }

    @Override protected Weapon newInstance() { return new AssaultRifle(); }

    @Override protected void fireProjectiles(GameWorld w, float x, float y, float aim, float dmg, int shotId) {
        fireBullet(w, x, y, aim + Util.gauss() * 0.03f, 1250, 0.9f, 32 * dmg, 70, 1, shotId);
    }

    @Override public void drawModel(Graphics2D g) {
        g.setColor(new Color(34, 36, 32)); g.fillRect(3, -2, 28, 5);
        g.setColor(new Color(70, 74, 64)); g.fillRect(12, -1, 10, 3);
        g.setColor(new Color(24, 24, 26)); g.fillRect(31, -1, 6, 2); g.fillRect(15, 3, 4, 6);
    }
}
