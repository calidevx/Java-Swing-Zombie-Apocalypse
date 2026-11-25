package deadsignal.entity.projectile;

import deadsignal.core.Util;
import deadsignal.entity.Player;
import deadsignal.game.GameWorld;

import java.awt.Color;

/** INHERITANCE + OVERRIDING: an EnergyOrb that slowly turns toward the player. */
public final class HomingSpore extends EnergyOrb {
    public HomingSpore(float x, float y, float angle) {
        super(x, y, angle, 150, 12, 8, new Color(255, 255, 140));
        setLife(4.5f);
    }

    @Override protected void steer(GameWorld w, float dt) {
        Player p = w.getPlayer();
        if (p.isDead()) return;
        float a = (float) Math.atan2(getVy(), getVx()), ta = (float) Math.atan2(p.getY() - getY(), p.getX() - getX());
        a = Util.turn(a, ta, 1.8f * dt);
        float sp = getSpeed();
        setVelocity((float) Math.cos(a) * sp, (float) Math.sin(a) * sp);
    }
}
