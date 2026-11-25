package deadsignal.entity.item;

import deadsignal.core.Util;
import deadsignal.entity.Player;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;
import deadsignal.gfx.Fonts;

import java.awt.*;
import java.awt.geom.Path2D;

/** The evacuation point at the end of a chapter. Completes when the player steps inside. */
public final class ExitZone extends ObjectiveItem {
    private final String label;

    public ExitZone(float x, float y, String label) { super(x, y, 50); this.label = label; }

    @Override protected void updateActive(GameWorld w, float dt) {
        Player p = w.getPlayer();
        if (!p.isDead() && distanceTo(p) < 54) complete(w);
    }

    @Override public Color getMinimapColor() { return new Color(100, 255, 120); }
    @Override public float getLightRadius() { return 130; }

    @Override protected void renderItem(Graphics2D g, Assets a, float pulse) {
        float x = getX(), y = getY();
        g.setColor(new Color(80, 255, 120, (int) (50 + 50 * pulse))); Util.oval(g, x, y, 52, 52);
        g.setColor(new Color(120, 255, 150, 220)); g.setStroke(new BasicStroke(3f)); Util.ring(g, x, y, 46 + pulse * 6, 46 + pulse * 6);
        for (int k = 0; k < 4; k++) {
            double an = k * Math.PI / 2 + getAnim();
            double ax = x + Math.cos(an) * 34, ay = y + Math.sin(an) * 34;
            Path2D ar = new Path2D.Double();
            ar.moveTo(ax + Math.cos(an + Math.PI) * 8, ay + Math.sin(an + Math.PI) * 8);
            ar.lineTo(ax + Math.cos(an + 2.4) * 6, ay + Math.sin(an + 2.4) * 6);
            ar.lineTo(ax + Math.cos(an - 2.4) * 6, ay + Math.sin(an - 2.4) * 6);
            ar.closePath();
            g.fill(ar);
        }
        g.setFont(Fonts.MEDIUM); Util.ctext(g, label, (int) x, (int) y - 60, new Color(150, 255, 170));
    }
}
