package deadsignal.entity.projectile;

import deadsignal.core.Util;
import deadsignal.gfx.Assets;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/** A tumbling chunk of rock. Base for {@link Boulder}. */
public class RockFragment extends Projectile {
    public RockFragment(float x, float y, float angle, float speed, float damage, float radius, float life) {
        super(x, y, radius, angle, speed, life, damage, false);
    }

    @Override
    public void render(Graphics2D g, Assets a) {
        float r = getRadius();
        Color c = new Color(124, 118, 108);
        AffineTransform at = g.getTransform();
        g.translate(getX(), getY()); g.rotate(getAge() * 8);
        g.setColor(new Color(0, 0, 0, 80)); Util.oval(g, 4, 6, r, r * 0.8);
        g.setColor(c); g.fill(new Rectangle2D.Float(-r, -r * 0.85f, r * 2, r * 1.7f));
        g.setColor(Util.shade(c, 0.6)); g.setStroke(new BasicStroke(1.5f)); g.draw(new Rectangle2D.Float(-r, -r * 0.85f, r * 2, r * 1.7f));
        g.setTransform(at);
    }
}
