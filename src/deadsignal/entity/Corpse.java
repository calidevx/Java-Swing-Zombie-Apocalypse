package deadsignal.entity;

import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;
import deadsignal.core.Util;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/** Plays a short collapse animation, then paints itself permanently onto the ground layer. */
public final class Corpse extends Entity {
    private static final float DURATION = 0.55f;
    private final BufferedImage image;
    private final float angle;
    private float timer;

    public Corpse(BufferedImage image, float x, float y, float angle) { super(x, y, 10); this.image = image; this.angle = angle; }

    @Override
    public void update(GameWorld w, float dt) {
        timer += dt;
        if (timer >= DURATION) {
            Graphics2D d = w.getMap().getDecals();
            BufferedImage dark = Util.darken(image, 0.55f, 0.9f);
            AffineTransform at = d.getTransform();
            d.translate(getX(), getY()); d.rotate(angle + 0.4f); d.scale(1, 0.8);
            d.drawImage(dark, -dark.getWidth() / 2, -dark.getHeight() / 2, null);
            d.setTransform(at);
            remove();
        }
    }

    @Override
    public void render(Graphics2D g, Assets a) {
        float k = timer / DURATION;
        Composite oc = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0.2f, 1 - k * 0.5f)));
        AffineTransform at = g.getTransform();
        g.translate(getX(), getY()); g.rotate(angle + 0.4f * k); g.scale(1 + 0.1f * k, 1 - 0.2f * k);
        g.drawImage(image, -image.getWidth() / 2, -image.getHeight() / 2, null);
        g.setTransform(at);
        g.setComposite(oc);
    }
}
