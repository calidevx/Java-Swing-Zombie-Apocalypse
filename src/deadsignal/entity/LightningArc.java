package deadsignal.entity;

import deadsignal.core.Util;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.List;
import java.util.Random;

/** A brief jagged lightning bolt drawn through a list of points (chain lightning visual). */
public final class LightningArc extends Entity {
    private static final float DURATION = 0.22f;
    private final List<float[]> points;
    private final Color glow, core;
    private final long seed = System.nanoTime();
    private float timer;

    public LightningArc(List<float[]> points, Color glow, Color core) {
        super(points.get(0)[0], points.get(0)[1], 1);
        this.points = points; this.glow = glow; this.core = core;
    }

    @Override public void update(GameWorld w, float dt) { timer += dt; if (timer >= DURATION) remove(); }

    @Override
    public void render(Graphics2D g, Assets a) {
        float k = 1 - timer / DURATION;
        Random r = new Random(seed + (int) (timer * 40));
        Path2D path = new Path2D.Float();
        path.moveTo(points.get(0)[0], points.get(0)[1]);
        for (int i = 1; i < points.size(); i++) {
            float[] p0 = points.get(i - 1), p1 = points.get(i);
            for (int s = 1; s <= 6; s++) {
                float t = s / 6f;
                float jx = s == 6 ? 0 : (float) r.nextGaussian() * 7, jy = s == 6 ? 0 : (float) r.nextGaussian() * 7;
                path.lineTo(p0[0] + (p1[0] - p0[0]) * t + jx, p0[1] + (p1[1] - p0[1]) * t + jy);
            }
        }
        g.setColor(Util.alpha(glow, (int) (150 * k))); g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g.draw(path);
        g.setColor(Util.alpha(core, (int) (255 * k))); g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); g.draw(path);
    }
}
