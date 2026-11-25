package deadsignal.gfx;

import deadsignal.core.Const;
import deadsignal.core.Util;
import deadsignal.world.GameMap;
import deadsignal.world.Theme;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * Renders darkness, then "cuts" holes in it for every {@link LightEmitter}.
 * Works with any emitter type thanks to the interface (polymorphism).
 */
public final class Lighting {
    private final BufferedImage overlay = Util.img(Const.WIDTH, Const.HEIGHT);

    /**
     * @param emitters dynamic lights (projectiles, items, flashes...)
     * @param hasPlayer whether to draw the player's own light/flashlight
     */
    public void render(Graphics2D g, GameMap map, int sx, int sy, Iterable<? extends LightEmitter> emitters,
                       boolean hasPlayer, float px, float py, float aim, float time, float darknessOverride) {
        Theme th = map.getTheme();
        float d = darknessOverride >= 0 ? darknessOverride : th.getDarkness();
        if (d <= 0.01f) return;
        Graphics2D lg = overlay.createGraphics();
        lg.setComposite(AlphaComposite.Src);
        lg.setColor(Util.alpha(th.getAmbient(), (int) (d * 255)));
        lg.fillRect(0, 0, Const.WIDTH, Const.HEIGHT);
        lg.setComposite(AlphaComposite.DstOut);
        lg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (hasPlayer) {
            float x = px - sx, y = py - sy;
            if (th.hasFlashlight()) {
                cut(lg, x, y, 110, 0.75f);
                float r = 360;
                Arc2D cone = new Arc2D.Float(x - r, y - r, r * 2, r * 2, (float) (-Math.toDegrees(aim) - 27), 54, Arc2D.PIE);
                lg.setPaint(new RadialGradientPaint(new Point2D.Float(x, y), r, new float[]{0f, 0.55f, 1f},
                        new Color[]{new Color(0, 0, 0, 255), new Color(0, 0, 0, 200), new Color(0, 0, 0, 0)}));
                lg.fill(cone);
            } else cut(lg, x, y, 220, 0.9f);
        }
        for (LightEmitter e : map.getLights()) cut(lg, e.getLightX() - sx, e.getLightY() - sy, e.getLightRadius(), e.getLightIntensity(time));
        for (LightEmitter e : emitters) cut(lg, e.getLightX() - sx, e.getLightY() - sy, e.getLightRadius(), e.getLightIntensity(time));
        lg.dispose();
        g.drawImage(overlay, 0, 0, null);
        for (LightEmitter e : map.getLights()) glow(g, e, sx, sy);
        for (LightEmitter e : emitters) glow(g, e, sx, sy);
    }

    private static void cut(Graphics2D lg, float x, float y, float r, float a) {
        if (x < -r || y < -r || x > Const.WIDTH + r || y > Const.HEIGHT + r || r < 2 || a <= 0) return;
        a = Util.clamp(a, 0, 1);
        lg.setPaint(new RadialGradientPaint(new Point2D.Float(x, y), r, new float[]{0f, 0.45f, 1f},
                new Color[]{new Color(0, 0, 0, (int) (255 * a)), new Color(0, 0, 0, (int) (170 * a)), new Color(0, 0, 0, 0)}));
        lg.fill(new Ellipse2D.Float(x - r, y - r, r * 2, r * 2));
    }

    private static void glow(Graphics2D g, LightEmitter e, int sx, int sy) {
        Color c = e.getGlowColor();
        if (c == null || e.getGlowAlpha() <= 0) return;
        float x = e.getLightX() - sx, y = e.getLightY() - sy, r = e.getGlowRadius();
        if (x < -r || y < -r || x > Const.WIDTH + r || y > Const.HEIGHT + r) return;
        g.setPaint(new RadialGradientPaint(new Point2D.Float(x, y), r, new float[]{0f, 1f}, new Color[]{Util.alpha(c, e.getGlowAlpha()), Util.alpha(c, 0)}));
        g.fill(new Ellipse2D.Float(x - r, y - r, r * 2, r * 2));
    }
}
