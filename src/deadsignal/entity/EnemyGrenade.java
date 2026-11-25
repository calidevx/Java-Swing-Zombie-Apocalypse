package deadsignal.entity;

import deadsignal.core.Util;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;

import java.awt.*;

/** Thrown by Colonel Graves. Shows a red warning circle and only hurts the player. */
public final class EnemyGrenade extends Grenade {
    private static final float RADIUS = 85;

    public EnemyGrenade(float sx, float sy, float tx, float ty, float flightTime) {
        super(sx, sy, tx, ty, flightTime, flightTime + 0.35f);
    }

    @Override protected void detonate(GameWorld w) { w.explode(getX(), getY(), RADIUS, 26, Faction.BOSS); }
    @Override protected Color bodyColor() { return new Color(90, 70, 40); }

    @Override
    public void render(Graphics2D g, Assets a) {
        float k = getFuseProgress();
        g.setColor(new Color(255, 40, 30, (int) (40 + 60 * k))); Util.oval(g, getTargetX(), getTargetY(), RADIUS, RADIUS);
        g.setColor(new Color(255, 60, 40, 200)); g.setStroke(new BasicStroke(2f)); Util.ring(g, getTargetX(), getTargetY(), RADIUS * k, RADIUS * k);
        super.render(g, a);
    }
}
