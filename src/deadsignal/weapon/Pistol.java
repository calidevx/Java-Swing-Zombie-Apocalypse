package deadsignal.weapon;

import deadsignal.core.Util;
import deadsignal.game.GameWorld;

import java.awt.*;

/** Starting sidearm with unlimited reserve ammo. */
public final class Pistol extends Weapon {
    public Pistol() { super("M9 PISTOL", 0, 12, 0, 4.5f, 1.0f, 3, 2, 22, "pistol"); }

    @Override public boolean hasInfiniteAmmo() { return true; }
    @Override protected Weapon newInstance() { return new Pistol(); }

    @Override protected void fireProjectiles(GameWorld w, float x, float y, float aim, float dmg, int shotId) {
        fireBullet(w, x, y, aim + Util.gauss() * 0.035f, 950, 0.75f, 24 * dmg, 60, 0, shotId);
    }

    @Override public void drawModel(Graphics2D g) {
        g.setColor(new Color(36, 36, 40)); g.fillRect(8, -2, 14, 4);
        g.setColor(new Color(80, 80, 86)); g.fillRect(8, -2, 14, 1);
    }
}
