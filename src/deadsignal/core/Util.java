package deadsignal.core;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Static math, colour and drawing helpers shared by every package. */
public final class Util {
    public static final Random RNG = new Random();

    private Util() { }

    public static float rf() { return RNG.nextFloat(); }
    public static float rf(float a, float b) { return a + RNG.nextFloat() * (b - a); }
    public static int ri(int a, int b) { return a + RNG.nextInt(b - a + 1); }
    public static float gauss() { return (float) RNG.nextGaussian(); }
    public static int cl(int v) { return v < 0 ? 0 : (v > 255 ? 255 : v); }
    public static float clamp(float v, float lo, float hi) { return v < lo ? lo : (v > hi ? hi : v); }
    public static float len(float x, float y) { return (float) Math.sqrt(x * x + y * y); }
    public static float dist(float ax, float ay, float bx, float by) { return len(bx - ax, by - ay); }

    public static float turn(float a, float target, float maxStep) {
        float d = (float) Math.atan2(Math.sin(target - a), Math.cos(target - a));
        if (Math.abs(d) <= maxStep) return target;
        return a + Math.signum(d) * maxStep;
    }

    public static Color shade(Color c, double f) {
        return new Color(cl((int) (c.getRed() * f)), cl((int) (c.getGreen() * f)), cl((int) (c.getBlue() * f)), c.getAlpha());
    }
    public static Color mix(Color a, Color b, double t) {
        t = Math.max(0, Math.min(1, t));
        return new Color(cl((int) (a.getRed() + (b.getRed() - a.getRed()) * t)), cl((int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t)),
                cl((int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t)), cl((int) (a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t)));
    }
    public static Color alpha(Color c, int a) { return new Color(c.getRed(), c.getGreen(), c.getBlue(), cl(a)); }

    public static BufferedImage img(int w, int h) { return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB); }
    public static Graphics2D g(BufferedImage im) { Graphics2D g = im.createGraphics(); hq(g); return g; }
    public static void hq(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    public static void oval(Graphics2D g, double cx, double cy, double rx, double ry) { g.fill(new Ellipse2D.Double(cx - rx, cy - ry, rx * 2, ry * 2)); }
    public static void ring(Graphics2D g, double cx, double cy, double rx, double ry) { g.draw(new Ellipse2D.Double(cx - rx, cy - ry, rx * 2, ry * 2)); }
    public static void line(Graphics2D g, double x1, double y1, double x2, double y2) { g.draw(new Line2D.Double(x1, y1, x2, y2)); }
    public static BasicStroke st(float w) { return new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND); }

    public static void drawRotated(Graphics2D g, BufferedImage im, float x, float y, float ang, float scale) {
        AffineTransform at = g.getTransform();
        g.translate(x, y); g.rotate(ang);
        if (scale != 1) g.scale(scale, scale);
        g.drawImage(im, -im.getWidth() / 2, -im.getHeight() / 2, null);
        g.setTransform(at);
    }

    public static void noise(BufferedImage im, Color base, int amp, Random r) {
        for (int y = 0; y < im.getHeight(); y++) for (int x = 0; x < im.getWidth(); x++) {
            int d = r.nextInt(amp * 2 + 1) - amp;
            im.setRGB(x, y, 0xFF000000 | (cl(base.getRed() + d) << 16) | (cl(base.getGreen() + d) << 8) | cl(base.getBlue() + d));
        }
    }
    public static BufferedImage flash(BufferedImage src) {
        BufferedImage o = img(src.getWidth(), src.getHeight());
        for (int y = 0; y < src.getHeight(); y++) for (int x = 0; x < src.getWidth(); x++) {
            int a = src.getRGB(x, y) >>> 24;
            if (a > 0) o.setRGB(x, y, (a << 24) | 0xFFF0F0);
        }
        return o;
    }
    public static BufferedImage darken(BufferedImage src, float f, float a) {
        return new RescaleOp(new float[]{f, f * 0.9f, f * 0.9f, a}, new float[]{0, 0, 0, 0}, null).filter(src, null);
    }

    public static void text(Graphics2D g, String s, int x, int y, Color c) {
        g.setColor(new Color(0, 0, 0, Math.min(200, c.getAlpha()))); g.drawString(s, x + 2, y + 2);
        g.setColor(c); g.drawString(s, x, y);
    }
    public static void ctext(Graphics2D g, String s, int cx, int y, Color c) {
        text(g, s, cx - g.getFontMetrics().stringWidth(s) / 2, y, c);
    }
    public static List<String> wrap(FontMetrics fm, String s, int maxW) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (String w : s.split(" ")) {
            String t = cur.length() == 0 ? w : cur + " " + w;
            if (fm.stringWidth(t) > maxW && cur.length() > 0) { out.add(cur.toString()); cur = new StringBuilder(w); }
            else cur = new StringBuilder(t);
        }
        if (cur.length() > 0) out.add(cur.toString());
        return out;
    }
    public static void splat(Graphics2D g, float x, float y, float size, Random r, Color base) {
        for (int i = 0; i < 7; i++) {
            float ox = (float) r.nextGaussian() * size * 0.5f, oy = (float) r.nextGaussian() * size * 0.5f;
            float rr = size * (0.25f + r.nextFloat() * 0.45f);
            g.setColor(alpha(shade(base, 0.7 + r.nextFloat() * 0.5), 110 + r.nextInt(90)));
            oval(g, x + ox, y + oy, rr, rr * (0.6 + r.nextFloat() * 0.5));
        }
        for (int i = 0; i < 8; i++) {
            double a = r.nextDouble() * Math.PI * 2, d = size * (0.8 + r.nextDouble() * 1.2);
            g.setColor(alpha(base, 150));
            oval(g, x + Math.cos(a) * d, y + Math.sin(a) * d, 1 + r.nextFloat() * 1.8, 1 + r.nextFloat() * 1.8);
        }
    }
    public static void bar(Graphics2D g, int x, int y, int w, int h, float frac, Color a, Color b) {
        g.setColor(new Color(0, 0, 0, 170)); g.fillRoundRect(x - 2, y - 2, w + 4, h + 4, 6, 6);
        g.setColor(new Color(50, 20, 20)); g.fillRect(x, y, w, h);
        int fw = (int) (w * clamp(frac, 0, 1));
        if (fw > 0) { g.setPaint(new GradientPaint(x, y, a, x, y + h, b)); g.fillRect(x, y, fw, h); }
        g.setColor(new Color(255, 255, 255, 50)); g.fillRect(x, y, fw, Math.max(1, h / 3));
    }
    public static void drawStar(Graphics2D g, float cx, float cy, float r, Color c) {
        Path2D s = new Path2D.Float();
        for (int i = 0; i < 10; i++) {
            double a = -Math.PI / 2 + i * Math.PI / 5, rr = i % 2 == 0 ? r : r * 0.45;
            if (i == 0) s.moveTo(cx + Math.cos(a) * rr, cy + Math.sin(a) * rr); else s.lineTo(cx + Math.cos(a) * rr, cy + Math.sin(a) * rr);
        }
        s.closePath();
        g.setColor(c); g.fill(s);
        g.setColor(shade(c, 0.6)); g.setStroke(new BasicStroke(2f)); g.draw(s);
    }
}
