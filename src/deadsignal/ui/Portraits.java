package deadsignal.ui;

import deadsignal.core.Util;
import deadsignal.story.Speaker;

import java.awt.*;
import java.awt.geom.*;

/** Draws a small character portrait for each speaker. */
public final class Portraits {
    private Portraits() { }

    public static void draw(Graphics2D g, Speaker who, int x, int y, int s) {
        Shape oldClip = g.getClip();
        g.setColor(new Color(20, 22, 28)); g.fillRoundRect(x, y, s, s, 10, 10);
        g.clip(new RoundRectangle2D.Float(x, y, s, s, 10, 10));
        g.setPaint(new GradientPaint(x, y, Util.shade(who.getColor(), 0.35), x, y + s, new Color(10, 10, 14))); g.fillRect(x, y, s, s);
        double cx = x + s / 2.0, cy = y + s * 0.52, r = s * 0.27;
        if (who == Speaker.RADIO || who == Speaker.SYSTEM || who == Speaker.TIP) {
            java.util.Random rnd = new java.util.Random(System.nanoTime() / 80_000_000L);
            for (int i = 0; i < 160; i++) { int v = 30 + rnd.nextInt(110); g.setColor(new Color(v, v, v, 120)); g.fillRect(x + rnd.nextInt(s), y + rnd.nextInt(s), 2, 2); }
            g.setColor(new Color(60, 64, 70)); g.fillRoundRect(x + s / 5, y + s / 3, s * 3 / 5, s / 3, 6, 6);
            g.setColor(who == Speaker.SYSTEM ? new Color(255, 70, 60) : new Color(120, 220, 140)); Util.oval(g, x + s * 0.68, y + s / 2.0, 3, 3);
            g.setColor(new Color(150, 150, 150)); g.setStroke(new BasicStroke(2f)); Util.line(g, x + s * 0.7, y + s / 3.0, x + s * 0.82, y + s * 0.15);
        } else {
            Color skin = new Color(204, 160, 124), hair = new Color(30, 22, 16), shirt = new Color(40, 70, 120);
            switch (who) {
                case LENA: skin = new Color(196, 150, 116); hair = new Color(20, 14, 12); shirt = new Color(220, 222, 226); break;
                case GRAVES: skin = new Color(170, 136, 120); hair = new Color(150, 150, 150); shirt = new Color(80, 88, 58); break;
                case VOSS: skin = new Color(170, 160, 190); hair = null; shirt = new Color(60, 20, 70); break;
                case SURVIVOR: skin = new Color(210, 170, 134); hair = new Color(90, 60, 30); shirt = new Color(180, 110, 60); break;
                case NARRATOR: skin = new Color(120, 120, 120); break;
                default: break;
            }
            g.setColor(shirt); g.fill(new Ellipse2D.Double(cx - s * 0.42, cy + r * 0.9, s * 0.84, s * 0.7));
            if (who == Speaker.LENA) { g.setColor(hair); g.fill(new Ellipse2D.Double(cx - r * 1.25, cy - r * 1.15, r * 2.5, r * 2.9)); }
            g.setColor(skin); g.fill(new Ellipse2D.Double(cx - r, cy - r * 1.1, r * 2, r * 2.2));
            if (hair != null) { g.setColor(hair); g.fill(new Arc2D.Double(cx - r, cy - r * 1.15, r * 2, r * 1.4, 0, 180, Arc2D.CHORD)); }
            if (who == Speaker.GRAVES) {
                g.setColor(new Color(70, 78, 50)); g.fill(new Arc2D.Double(cx - r * 1.15, cy - r * 1.4, r * 2.3, r * 1.4, 0, 180, Arc2D.CHORD));
                g.fillRect((int) (cx - r * 1.3), (int) (cy - r * 0.75), (int) (r * 2.6), (int) (r * 0.25));
                g.setColor(new Color(210, 180, 60)); Util.oval(g, cx, cy - r * 0.95, 3, 3);
                g.setColor(new Color(140, 140, 140)); g.fillRect((int) (cx - r * 0.5), (int) (cy + r * 0.35), (int) r, 3);
            }
            if (who == Speaker.VOSS) {
                g.setColor(new Color(240, 236, 220));
                for (int k = 0; k < 5; k++) {
                    double a = Math.PI * 1.15 + k * Math.PI * 0.175;
                    Path2D sp = new Path2D.Double();
                    sp.moveTo(cx + Math.cos(a - 0.15) * r, cy - r * 0.3 + Math.sin(a - 0.15) * r);
                    sp.lineTo(cx + Math.cos(a) * r * 1.7, cy - r * 0.3 + Math.sin(a) * r * 1.7);
                    sp.lineTo(cx + Math.cos(a + 0.15) * r, cy - r * 0.3 + Math.sin(a + 0.15) * r);
                    sp.closePath(); g.fill(sp);
                }
            }
            g.setColor(who == Speaker.VOSS ? new Color(240, 160, 255) : new Color(30, 24, 20));
            Util.oval(g, cx - r * 0.4, cy - r * 0.05, r * 0.13, r * 0.13); Util.oval(g, cx + r * 0.4, cy - r * 0.05, r * 0.13, r * 0.13);
            if (who == Speaker.LENA) {
                g.setColor(new Color(40, 40, 40)); g.setStroke(new BasicStroke(1.5f));
                Util.ring(g, cx - r * 0.4, cy - r * 0.05, r * 0.28, r * 0.24); Util.ring(g, cx + r * 0.4, cy - r * 0.05, r * 0.28, r * 0.24);
            }
            g.setColor(Util.shade(skin, 0.6)); g.setStroke(new BasicStroke(1.5f)); Util.line(g, cx - r * 0.3, cy + r * 0.5, cx + r * 0.3, cy + r * 0.5);
            if (who == Speaker.KAI) { g.setColor(new Color(60, 40, 30, 90)); g.fill(new Arc2D.Double(cx - r, cy - r * 0.4, r * 2, r * 1.5, 180, 180, Arc2D.CHORD)); }
        }
        for (int yy = y; yy < y + s; yy += 3) { g.setColor(new Color(0, 0, 0, 35)); g.fillRect(x, yy, s, 1); }
        g.setClip(oldClip);
        g.setColor(who.getColor()); g.setStroke(new BasicStroke(1.5f)); g.drawRoundRect(x, y, s, s, 10, 10);
    }
}
