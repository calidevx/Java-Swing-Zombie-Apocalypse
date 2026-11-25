package deadsignal.entity.projectile;

import deadsignal.core.Util;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;

import java.awt.*;

/** The Maestro's projectile: a music note that weaves side to side as it flies (overrides steer and render). */
public final class MusicNote extends EnergyOrb {
    private final float baseAngle, phase;

    public MusicNote(float x, float y, float angle, Color color) {
        super(x, y, angle, 230, 9, 7, color);
        this.baseAngle = angle;
        this.phase = Util.rf(0, 6.28f);
    }

    @Override protected void steer(GameWorld w, float dt) {
        float wobble = (float) Math.sin(getAge() * 9 + phase) * 90;
        float fx = (float) Math.cos(baseAngle), fy = (float) Math.sin(baseAngle);
        setVelocity(fx * 230 - fy * wobble, fy * 230 + fx * wobble);
    }

    @Override
    public void render(Graphics2D g, Assets a) {
        Color c = getColor();
        g.setColor(Util.alpha(c, 70)); Util.oval(g, getX(), getY(), 13, 13);
        drawNote(g, getX(), getY(), 1.2f, c);
    }

    /** Draws an eighth-note glyph (shared by the zombie dance overlay). */
    public static void drawNote(Graphics2D g, float x, float y, float s, Color c) {
        g.setColor(c);
        Util.oval(g, x - 2 * s, y + 3 * s, 4 * s, 3 * s);
        g.setStroke(new BasicStroke(1.8f * s));
        Util.line(g, x + 1.8 * s, y + 3 * s, x + 1.8 * s, y - 8 * s);
        Util.line(g, x + 1.8 * s, y - 8 * s, x + 6 * s, y - 4 * s);
        g.setColor(new Color(255, 255, 255, 170));
        Util.oval(g, x - 3 * s, y + 2 * s, 1.2 * s, 1 * s);
    }
}
