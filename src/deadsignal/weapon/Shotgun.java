package deadsignal.weapon;

import deadsignal.core.Util;
import deadsignal.game.GameWorld;

import java.awt.*;

/** Eight pellets per shot - devastating up close. */
public final class Shotgun extends Weapon {
    private static final int PELLETS = 8;

    public Shotgun() { super("PUMP SHOTGUN", 1, 6, 36, 1.25f, 1.7f, 6, 7, 32, "shotgun"); }

    @Override protected Weapon newInstance() { return new Shotgun(); }

    @Override protected void fireProjectiles(GameWorld w, float x, float y, float aim, float dmg, int shotId) {
        for (int i = 0; i < PELLETS; i++) fireBullet(w, x, y, aim + Util.gauss() * 0.13f, 820, 0.32f, 13 * dmg, 70, 0, shotId);
    }

    @Override public float getCrosshairSpread() { return super.getCrosshairSpread() + 6; }

    @Override public void drawModel(Graphics2D g) {
        g.setColor(new Color(96, 62, 36)); g.fillRect(3, -2, 11, 5);
        g.setColor(new Color(46, 46, 50)); g.fillRect(12, -2, 21, 4);
        g.setColor(new Color(116, 84, 52)); g.fillRect(17, -3, 8, 6);
    }
}
