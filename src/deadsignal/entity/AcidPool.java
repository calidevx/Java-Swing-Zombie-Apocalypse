package deadsignal.entity;

import deadsignal.core.Util;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;

import java.awt.*;

/** A temporary puddle of acid that burns the player while standing in it. */
public final class AcidPool extends Entity {
    private static final float DURATION = 3.2f, DPS = 16;
    private float timeLeft = DURATION, tick;

    public AcidPool(float x, float y) { super(x, y, 24); }

    @Override
    public void update(GameWorld w, float dt) {
        timeLeft -= dt; tick -= dt;
        if (timeLeft <= 0) { remove(); return; }
        Player p = w.getPlayer();
        if (tick <= 0 && !p.isDead() && distanceTo(p) < getRadius() + p.getRadius() * 0.5f) { tick = 0.3f; p.takeDamage(DPS * 0.3f, DamageType.ACID, w); }
    }

    @Override
    public void render(Graphics2D g, Assets a) {
        float k = Math.min(1, timeLeft / 0.6f), r = getRadius();
        g.setPaint(new RadialGradientPaint(getX(), getY(), r, new float[]{0, 0.8f, 1},
                new Color[]{new Color(150, 230, 60, (int) (190 * k)), new Color(90, 170, 30, (int) (150 * k)), new Color(60, 120, 20, 0)}));
        Util.oval(g, getX(), getY(), r, r * 0.85);
    }
}
