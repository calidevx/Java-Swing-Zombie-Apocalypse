package deadsignal.gfx;

import deadsignal.core.Const;
import deadsignal.core.Util;
import deadsignal.world.Tile;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Sprite factory: generates every tile texture and animated character frame in code.
 * Internal arrays are private; callers ask for frames through typed methods (encapsulation).
 */
public final class Assets {
    // Internal indices (match the enum ordinals of ZombieSprite / BossSprite).
    private static final class Z { static final int WALKER = 0, RUNNER = 1, BRUTE = 2, SPITTER = 3, EXPLODER = 4, CRAWLER = 5, SOLDIER = 6, SPIDERLING = 7, N = 8;
        static final int[] SIZE = {48, 48, 84, 54, 60, 44, 48, 40}; }
    private static final class BT { static final int CENSER = 0, TANK = 1, BROOD = 2, S13 = 3, GRAVES = 4, SHEPHERD = 5, MAESTRO = 6, N = 7;
        static final int[] SIZE = {108, 136, 150, 96, 140, 172, 112}; }
    private static final class IK { static final int RADIO = 0, FUEL = 1, SURVIVOR = 2, GENERATOR = 3, DRIVE = 4, TRANSMITTER = 5, NODE = 6, SAMPLE = 7, EXIT = 8; }
    private static final class PK { static final int CASH = 0, AMMO = 1, HEALTH = 2, GRENADE = 3, MAGNET = 4; }

    private final BufferedImage[][] ground = new BufferedImage[Tile.GROUND_COUNT][];
    private final Color[] groundAvg = new Color[Tile.GROUND_COUNT];
    private BufferedImage[] legs = new BufferedImage[8], survivor = new BufferedImage[4];
    private BufferedImage torso, torsoFlash;
    private final BufferedImage[][][] zf = new BufferedImage[Z.N][3][8], zff = new BufferedImage[Z.N][3][8];
    private final BufferedImage[][] bm = new BufferedImage[BT.N][8], ba = new BufferedImage[BT.N][8], bf = new BufferedImage[BT.N][8];
    private int broodLegs = 6;
    private final BufferedImage[][][] broodFrames = new BufferedImage[3][7][8];   // [normal/attack/flash][legs][frame]
    private final BufferedImage[] canopy = new BufferedImage[6];
    private final BufferedImage[] itemImg = new BufferedImage[9], pickImg = new BufferedImage[5];
    private BufferedImage vignette, redVig;

    static final Color[] ZSKIN = {new Color(120, 148, 100), new Color(146, 150, 118), new Color(108, 128, 112), new Color(156, 138, 118)};
    static final Color[] ZSHIRT = {new Color(120, 40, 40), new Color(50, 70, 110), new Color(92, 92, 92), new Color(150, 140, 100),
            new Color(60, 90, 60), new Color(130, 100, 60), new Color(170, 170, 165), new Color(110, 60, 110)};
    static final Color[] ZPANTS = {new Color(40, 45, 60), new Color(70, 60, 45), new Color(50, 50, 50), new Color(80, 70, 90)};

    public Assets() {
        for (int id = 0; id < Tile.GROUND_COUNT; id++) {
            ground[id] = new BufferedImage[4];
            for (int v = 0; v < 4; v++) {
                BufferedImage im = Util.img(32, 32);
                drawGround(im, id, v, new Random(id * 1000 + v * 31 + 7));
                ground[id][v] = im;
            }
            groundAvg[id] = avg(ground[id][0]);
        }
        buildPlayer();
        for (int t = 0; t < Z.N; t++) for (int v = 0; v < 3; v++) {
            Random pr = new Random(t * 100 + v * 7 + 3);
            Color skin = ZSKIN[pr.nextInt(ZSKIN.length)], shirt = ZSHIRT[pr.nextInt(ZSHIRT.length)], pants = ZPANTS[pr.nextInt(ZPANTS.length)];
            if (t == Z.SOLDIER) { shirt = new Color(80, 88, 58); pants = new Color(62, 68, 46); }
            if (t == Z.RUNNER) skin = Util.shade(skin, 0.9);
            long seed = t * 1000L + v;
            for (int f = 0; f < 8; f++) {
                int S = Z.SIZE[t];
                BufferedImage im = Util.img(S, S);
                Graphics2D g = Util.g(im); g.translate(S / 2, S / 2);
                drawZombie(g, t, f / 8.0 * Math.PI * 2, skin, shirt, pants, new Random(seed));
                g.dispose();
                zf[t][v][f] = im; zff[t][v][f] = Util.flash(im);
            }
        }
        for (int b = 0; b < BT.N; b++) for (int f = 0; f < 8; f++) {
            int S = BT.SIZE[b];
            for (int a = 0; a < 2; a++) {
                BufferedImage im = Util.img(S, S);
                Graphics2D g = Util.g(im); g.translate(S / 2, S / 2);
                drawBoss(g, b, f / 8.0 * Math.PI * 2, a == 1, new Random(b * 77 + 5));
                g.dispose();
                if (a == 0) { bm[b][f] = im; bf[b][f] = Util.flash(im); } else ba[b][f] = im;
            }
        }
        for (int legs = 0; legs <= 6; legs++) for (int f = 0; f < 8; f++) for (int a = 0; a < 2; a++) {
            broodLegs = legs;
            int S = BT.SIZE[BT.BROOD];
            BufferedImage im = Util.img(S, S);
            Graphics2D g = Util.g(im); g.translate(S / 2, S / 2);
            drawBoss(g, BT.BROOD, f / 8.0 * Math.PI * 2, a == 1, new Random(BT.BROOD * 77 + 5));
            g.dispose();
            broodFrames[a][legs][f] = im;
            if (a == 0) broodFrames[2][legs][f] = Util.flash(im);
        }
        broodLegs = 6;
        for (int f = 0; f < 4; f++) {
            BufferedImage im = Util.img(48, 48); Graphics2D g = Util.g(im); g.translate(24, 24);
            drawSurvivor(g, f / 4.0 * Math.PI * 2); g.dispose(); survivor[f] = im;
        }
        for (int k = 0; k < 3; k++) for (int v = 0; v < 2; v++) canopy[k * 2 + v] = makeCanopy(k, v);
        for (int k = 0; k < 9; k++) itemImg[k] = makeItem(k);
        for (int k = 0; k < 5; k++) pickImg[k] = makePickup(k);
        vignette = makeVignette(new Color(0, 0, 0), 190);
        redVig = makeVignette(new Color(160, 0, 0), 210);
    }

    private static Color avg(BufferedImage im) {
        long r = 0, g = 0, b = 0; int n = 0;
        for (int y = 0; y < im.getHeight(); y += 2) for (int x = 0; x < im.getWidth(); x += 2) {
            int c = im.getRGB(x, y); r += (c >> 16) & 255; g += (c >> 8) & 255; b += c & 255; n++;
        }
        return new Color((int) (r / n), (int) (g / n), (int) (b / n));
    }

    private static BufferedImage makeVignette(Color c, int a) {
        BufferedImage im = Util.img(Const.WIDTH, Const.HEIGHT);
        Graphics2D g = Util.g(im);
        float r = (float) Math.hypot(Const.WIDTH / 2.0, Const.HEIGHT / 2.0);
        g.setPaint(new RadialGradientPaint(Const.WIDTH / 2f, Const.HEIGHT / 2f, r, new float[]{0f, 0.55f, 1f},
                new Color[]{Util.alpha(c, 0), Util.alpha(c, 0), Util.alpha(c, a)}));
        g.fillRect(0, 0, Const.WIDTH, Const.HEIGHT);
        g.dispose();
        return im;
    }

    // ---------------------------------------------------------------- tiles
    private void drawGround(BufferedImage im, int id, int v, Random r) {
        Graphics2D g;
        switch (id) {
            case Tile.GRASS:
                Util.noise(im, new Color(56, 86, 42), 9, r); g = Util.g(im);
                for (int i = 0; i < 3; i++) { g.setColor(new Color(100, 104, 52, 55)); Util.oval(g, r.nextInt(32), r.nextInt(32), 4 + r.nextInt(6), 3 + r.nextInt(4)); }
                for (int i = 0; i < 26; i++) {
                    int x = r.nextInt(32), y = r.nextInt(32);
                    g.setColor(r.nextBoolean() ? new Color(80, 122, 56) : new Color(38, 64, 30)); g.drawLine(x, y, x + r.nextInt(3) - 1, y - 2 - r.nextInt(3));
                }
                if (r.nextInt(4) == 0) { g.setColor(new Color(170, 160, 90)); g.fillRect(r.nextInt(30), r.nextInt(30), 2, 2); }
                g.dispose(); break;
            case Tile.DIRT:
                Util.noise(im, new Color(104, 84, 58), 12, r); g = Util.g(im);
                for (int i = 0; i < 6; i++) { g.setColor(new Color(80 + r.nextInt(40), 70 + r.nextInt(30), 56)); Util.oval(g, r.nextInt(32), r.nextInt(32), 1 + r.nextInt(2), 1 + r.nextInt(2)); }
                g.setColor(new Color(70, 56, 40, 70)); Util.oval(g, r.nextInt(32), r.nextInt(32), 6, 4);
                g.dispose(); break;
            case Tile.ROAD: case Tile.LINE_H: case Tile.LINE_V:
                Util.noise(im, new Color(52, 53, 56), 6, r); g = Util.g(im);
                if (r.nextInt(3) == 0) { g.setColor(new Color(28, 28, 30, 90)); Util.oval(g, r.nextInt(32), r.nextInt(32), 6 + r.nextInt(5), 4 + r.nextInt(4)); }
                for (int i = 0; i < 8; i++) { g.setColor(new Color(70, 70, 72)); g.fillRect(r.nextInt(32), r.nextInt(32), 1, 1); }
                g.setColor(new Color(205, 175, 50, 210));
                if (id == Tile.LINE_H && v % 2 == 0) g.fillRect(4, 15, 22, 3);
                if (id == Tile.LINE_V && v % 2 == 0) g.fillRect(15, 4, 3, 22);
                g.dispose(); break;
            case Tile.WALK:
                Util.noise(im, new Color(128, 126, 120), 7, r); g = Util.g(im);
                g.setColor(new Color(98, 96, 92)); g.drawLine(0, 0, 31, 0); g.drawLine(0, 16, 31, 16); g.drawLine(0, 0, 0, 31); g.drawLine(16, 0, 16, 31);
                if (r.nextInt(3) == 0) { g.setColor(new Color(90, 88, 84)); g.drawLine(r.nextInt(16), r.nextInt(16), 8 + r.nextInt(16), 8 + r.nextInt(16)); }
                g.dispose(); break;
            case Tile.WOOD:
                g = Util.g(im);
                for (int row = 0; row < 4; row++) {
                    Color c = new Color(112 + r.nextInt(24), 76 + r.nextInt(14), 46 + r.nextInt(10));
                    g.setColor(c); g.fillRect(0, row * 8, 32, 8);
                    for (int k = 0; k < 10; k++) { g.setColor(Util.shade(c, 0.85 + r.nextFloat() * 0.3)); g.fillRect(r.nextInt(32), row * 8 + 1 + r.nextInt(6), 4 + r.nextInt(6), 1); }
                    g.setColor(new Color(60, 40, 24)); g.drawLine(0, row * 8 + 7, 31, row * 8 + 7);
                    int seam = (row * 13 + v * 7) % 32; g.drawLine(seam, row * 8, seam, row * 8 + 7);
                }
                g.dispose(); break;
            case Tile.CARPET:
                Util.noise(im, new Color(92, 40, 46), 6, r); g = Util.g(im);
                g.setColor(new Color(130, 100, 60, 90)); for (int k = 4; k < 32; k += 8) for (int j = 4; j < 32; j += 8) g.fillRect(k, j, 2, 2);
                g.dispose(); break;
            case Tile.WTILE:
                Util.noise(im, new Color(196, 202, 202), 4, r); g = Util.g(im);
                g.setColor(new Color(150, 156, 156)); g.drawLine(0, 0, 31, 0); g.drawLine(0, 16, 31, 16); g.drawLine(0, 0, 0, 31); g.drawLine(16, 0, 16, 31);
                if (r.nextInt(3) == 0) { g.setColor(new Color(150, 140, 110, 70)); Util.oval(g, r.nextInt(32), r.nextInt(32), 5, 4); }
                g.dispose(); break;
            case Tile.METAL:
                Util.noise(im, new Color(80, 88, 96), 5, r); g = Util.g(im);
                g.setColor(new Color(100, 108, 118)); for (int k = 0; k < 32; k += 6) for (int j = (k / 6) % 2 * 3; j < 32; j += 6) g.drawLine(k, j, k + 2, j + 2);
                g.setColor(new Color(50, 56, 62)); g.drawRect(0, 0, 31, 31);
                g.setColor(new Color(130, 138, 146)); g.fillRect(2, 2, 2, 2); g.fillRect(28, 2, 2, 2); g.fillRect(2, 28, 2, 2); g.fillRect(28, 28, 2, 2);
                g.dispose(); break;
            case Tile.CONC:
                Util.noise(im, new Color(112, 110, 104), 9, r); g = Util.g(im);
                if (r.nextInt(2) == 0) { g.setColor(new Color(80, 78, 74)); int x = r.nextInt(32), y = r.nextInt(32); g.drawLine(x, y, x + r.nextInt(14) - 7, y + r.nextInt(14) - 7); }
                g.setColor(new Color(0, 0, 0, 30)); Util.oval(g, r.nextInt(32), r.nextInt(32), 7, 5);
                g.dispose(); break;
            case Tile.SEWER:
                g = Util.g(im);
                for (int row = 0; row < 4; row++) for (int k = -1; k < 3; k++) {
                    int bx = k * 16 + (row % 2) * 8, by = row * 8;
                    g.setColor(new Color(66 + r.nextInt(14), 60 + r.nextInt(10), 48 + r.nextInt(8))); g.fillRect(bx, by, 15, 7);
                }
                g.setColor(new Color(44, 40, 34)); for (int row = 0; row < 4; row++) g.drawLine(0, row * 8 + 7, 31, row * 8 + 7);
                if (r.nextInt(2) == 0) { g.setColor(new Color(60, 100, 50, 110)); Util.oval(g, r.nextInt(32), r.nextInt(32), 4, 3); }
                g.dispose(); break;
            case Tile.WATER:
                Util.noise(im, new Color(30, 62, 56), 3, new Random(77)); g = Util.g(im);
                g.setColor(new Color(70, 120, 105, 120)); g.setStroke(new BasicStroke(1.2f));
                for (int k = 0; k < 4; k++) {
                    double ph = v * Math.PI / 2 + k * 1.7;
                    int yy = (k * 8 + 4);
                    Path2D p = new Path2D.Double(); p.moveTo(0, yy + Math.sin(ph) * 2);
                    for (int x = 0; x <= 32; x += 4) p.lineTo(x, yy + Math.sin(ph + x * 0.4) * 2);
                    g.draw(p);
                }
                g.setColor(new Color(110, 100, 60, 120)); Util.oval(g, (v * 8 + 5) % 32, 20, 2, 1.5);
                g.dispose(); break;
            case Tile.ASH: case Tile.FIRE:
                Util.noise(im, new Color(42, 36, 33), 8, new Random(id == Tile.FIRE ? 55 : r.nextInt())); g = Util.g(im);
                for (int i = 0; i < 4; i++) { g.setColor(new Color(70, 64, 60, 90)); Util.oval(g, r.nextInt(32), r.nextInt(32), 4, 3); }
                for (int i = 0; i < 3; i++) { g.setColor(new Color(255, 110, 30, 150)); g.fillRect(r.nextInt(32), r.nextInt(32), 1, 1); }
                if (id == Tile.FIRE) {
                    Random fr = new Random(900 + v);
                    for (int k = 0; k < 4; k++) {
                        float fx = 6 + fr.nextInt(20), fy = 10 + fr.nextInt(16), fh = 9 + fr.nextInt(8);
                        g.setPaint(new RadialGradientPaint(fx, fy, fh, new float[]{0f, 0.45f, 1f},
                                new Color[]{new Color(255, 240, 150, 240), new Color(255, 130, 30, 200), new Color(200, 30, 0, 0)}));
                        g.fill(new Ellipse2D.Float(fx - fh * 0.6f, fy - fh * 1.2f, fh * 1.2f, fh * 1.6f));
                    }
                }
                g.dispose(); break;
            case Tile.TOXIC:
                Util.noise(im, new Color(80, 88, 96), 5, new Random(66)); g = Util.g(im);
                g.setPaint(new RadialGradientPaint(16, 16, 17, new float[]{0, 0.8f, 1}, new Color[]{new Color(120, 255, 90, 230), new Color(60, 190, 50, 210), new Color(40, 120, 30, 0)}));
                g.fillOval(1, 2, 30, 28);
                Random br = new Random(300 + v);
                for (int k = 0; k < 4; k++) { g.setColor(new Color(200, 255, 170, 200)); Util.ring(g, 6 + br.nextInt(20), 6 + br.nextInt(20), 1 + v % 3, 1 + v % 3); }
                g.dispose(); break;
            case Tile.RUBBLE:
                Util.noise(im, new Color(92, 88, 82), 10, r); g = Util.g(im);
                for (int i = 0; i < 7; i++) {
                    int x = r.nextInt(30), y = r.nextInt(30), s = 2 + r.nextInt(5);
                    g.setColor(new Color(0, 0, 0, 70)); g.fillRect(x + 1, y + 1, s, s);
                    g.setColor(new Color(120 + r.nextInt(30), 116 + r.nextInt(25), 108 + r.nextInt(20))); g.fillRect(x, y, s, s);
                }
                g.dispose(); break;
            default:
                Util.noise(im, new Color(80, 80, 80), 8, r);
        }
    }

    // ---------------------------------------------------------------- body parts
    private static void leg(Graphics2D g, double hx, double hy, double fx, double fy, float w, Color pants, Color boots) {
        g.setStroke(Util.st(w)); g.setColor(pants); Util.line(g, hx, hy, fx, fy);
        g.setColor(boots); Util.oval(g, fx + (fx >= hx ? 2 : -1), fy, w * 0.75, w * 0.55);
    }
    private static void limb(Graphics2D g, double x1, double y1, double x2, double y2, Color sleeve, Color skin, float w) {
        double mx = x1 + (x2 - x1) * 0.45, my = y1 + (y2 - y1) * 0.45;
        g.setStroke(Util.st(w + 1)); g.setColor(sleeve); Util.line(g, x1, y1, mx, my);
        g.setStroke(Util.st(w)); g.setColor(skin); Util.line(g, mx, my, x2, y2);
        g.setColor(Util.shade(skin, 0.8)); Util.oval(g, x2, y2, w * 0.58, w * 0.58);
    }
    private static void torsoShape(Graphics2D g, double cx, double rx, double ry, Color shirt, Random r, int blood) {
        g.setPaint(new GradientPaint((float) -rx, (float) -ry, Util.shade(shirt, 1.2), (float) rx, (float) ry, Util.shade(shirt, 0.68)));
        Util.oval(g, cx, 0, rx, ry);
        for (int i = 0; i < 3; i++) {
            double a = r.nextDouble() * Math.PI * 2, ex = cx + Math.cos(a) * rx * 0.85, ey = Math.sin(a) * ry * 0.85;
            g.setColor(Util.shade(shirt, 0.45));
            Path2D p = new Path2D.Double(); p.moveTo(ex, ey); p.lineTo(ex + r.nextGaussian() * 3, ey + r.nextGaussian() * 3); p.lineTo(ex + r.nextGaussian() * 3, ey + r.nextGaussian() * 3); p.closePath();
            g.fill(p);
        }
        for (int i = 0; i < blood; i++) {
            g.setColor(new Color(110, 12, 12, 200));
            Util.oval(g, cx + (r.nextDouble() - 0.5) * rx, (r.nextDouble() - 0.5) * ry * 1.2, 1.5 + r.nextDouble() * 2.5, 1.5 + r.nextDouble() * 2.5);
        }
        g.setColor(Util.shade(shirt, 0.45)); g.setStroke(new BasicStroke(1.2f)); Util.ring(g, cx, 0, rx, ry);
    }
    private static void zHead(Graphics2D g, double x, double y, double rr, Color skin, Random r, boolean redEyes, boolean helmet) {
        g.setColor(skin); Util.oval(g, x, y, rr, rr);
        g.setColor(Util.shade(skin, 0.72)); g.fill(new Arc2D.Double(x - rr, y - rr, rr * 2, rr * 2, 100, 160, Arc2D.CHORD));
        g.setColor(new Color(100, 20, 20, 210)); Util.oval(g, x - rr * 0.3 + r.nextDouble() * rr * 0.4, y + (r.nextDouble() - 0.5) * rr, rr * 0.28, rr * 0.22);
        Color e = redEyes ? new Color(255, 50, 30) : new Color(40, 10, 10);
        g.setColor(e); Util.oval(g, x + rr * 0.55, y - rr * 0.38, rr * 0.18, rr * 0.18); Util.oval(g, x + rr * 0.55, y + rr * 0.38, rr * 0.18, rr * 0.18);
        if (helmet) {
            g.setColor(new Color(72, 82, 52)); Util.oval(g, x - 1, y, rr * 1.12, rr * 1.12);
            g.setColor(new Color(52, 60, 38)); g.setStroke(new BasicStroke(1.3f)); Util.ring(g, x - 1, y, rr * 1.12, rr * 1.12);
            g.setColor(new Color(96, 106, 70)); Util.oval(g, x - 3, y - 2, rr * 0.4, rr * 0.3);
        } else if (r.nextBoolean()) {
            g.setColor(new Color(50, 40, 30, 210)); g.fill(new Arc2D.Double(x - rr, y - rr, rr * 2, rr * 2, 120, 120, Arc2D.CHORD));
        }
        g.setColor(Util.shade(skin, 0.5)); g.setStroke(new BasicStroke(1f)); Util.ring(g, x, y, rr, rr);
    }

    private void buildPlayer() {
        Color pants = new Color(38, 42, 54), boots = new Color(24, 22, 20);
        for (int f = 0; f < 8; f++) {
            BufferedImage im = Util.img(48, 48); Graphics2D g = Util.g(im); g.translate(24, 24);
            double s = Math.sin(f / 8.0 * Math.PI * 2);
            leg(g, 0, -5.5, s * 9, -6, 6.5f, pants, boots);
            leg(g, 0, 5.5, -s * 9, 6, 6.5f, pants, boots);
            g.dispose(); legs[f] = im;
        }
        torso = Util.img(60, 60);
        Graphics2D g = Util.g(torso); g.translate(30, 30);
        Color skin = new Color(198, 152, 118);
        g.setColor(new Color(72, 66, 48)); g.fill(new RoundRectangle2D.Double(-18, -9, 10, 18, 5, 5));
        g.setColor(new Color(46, 42, 30)); g.setStroke(new BasicStroke(1.2f)); g.draw(new RoundRectangle2D.Double(-18, -9, 10, 18, 5, 5));
        g.setColor(new Color(150, 30, 30)); g.fillRect(-15, -2, 4, 4);
        g.setPaint(new GradientPaint(-6, -14, new Color(50, 86, 146), 6, 14, new Color(24, 44, 86)));
        Util.oval(g, 0, 0, 9.5, 14);
        g.setColor(new Color(14, 24, 48)); g.setStroke(new BasicStroke(1.4f)); Util.ring(g, 0, 0, 9.5, 14);
        g.setColor(new Color(230, 220, 90)); g.setStroke(new BasicStroke(2f)); Util.line(g, -3, -12.5, -3, 12.5);
        Color sleeve = new Color(36, 64, 112);
        g.setStroke(Util.st(6)); g.setColor(sleeve); Util.line(g, 1, -11, 9, -6); Util.line(g, 1, 11, 7, 6);
        g.setStroke(Util.st(4.5f)); g.setColor(skin); Util.line(g, 9, -6, 13, -3); Util.line(g, 7, 6, 10, 3);
        Util.oval(g, 13, -2.5, 3, 3); Util.oval(g, 10.5, 3, 3, 3);
        g.setColor(skin); Util.oval(g, 1, 0, 7.5, 7.5);
        g.setColor(new Color(34, 24, 18)); g.fill(new Arc2D.Double(-6.5, -7.5, 15, 15, 80, 200, Arc2D.CHORD));
        g.setColor(Util.shade(skin, 0.7)); g.setStroke(new BasicStroke(1.1f)); Util.ring(g, 1, 0, 7.5, 7.5);
        g.dispose();
        torsoFlash = Util.flash(torso);
    }

    private void drawZombie(Graphics2D g, int type, double ph, Color skin, Color shirt, Color pants, Random r) {
        double s = Math.sin(ph), c = Math.cos(ph);
        Color boots = new Color(30, 26, 24);
        switch (type) {
            case Z.WALKER: case Z.SOLDIER: {
                leg(g, 0, -5, s * 7, -5.5, 6f, pants, boots);
                leg(g, 0, 5, -s * 5 - 1, 5.5, 6f, pants, boots);
                limb(g, 0, -10, 15 + s * 2, -8 + c * 1.5, shirt, skin, 4.5f);
                limb(g, 0, 10, 14 - s * 2, 8 - c * 1.5, shirt, skin, 4.5f);
                torsoShape(g, 0, 8.5, 13, shirt, r, type == Z.SOLDIER ? 2 : 3);
                if (type == Z.SOLDIER) { g.setColor(new Color(50, 56, 36)); g.fillRect(-12, -2, 6, 4); g.setColor(new Color(110, 116, 80)); Util.oval(g, -3, -6, 3, 2); Util.oval(g, 1, 5, 3, 2); }
                zHead(g, 3, 0, 7, skin, r, false, type == Z.SOLDIER);
                break;
            }
            case Z.RUNNER: {
                leg(g, 0, -4.5, s * 11, -5, 5.5f, pants, boots);
                leg(g, 0, 4.5, -s * 11, 5, 5.5f, pants, boots);
                limb(g, 0, -9, 4 + s * 9, -13, shirt, skin, 4f);
                limb(g, 0, 9, 4 - s * 9, 13, shirt, skin, 4f);
                torsoShape(g, 1, 7.5, 12, shirt, r, 4);
                zHead(g, 5, 0, 6.5, skin, r, true, false);
                break;
            }
            case Z.BRUTE: {
                leg(g, -2, -10, s * 9 - 2, -12, 11f, pants, boots);
                leg(g, -2, 10, -s * 9 - 2, 12, 11f, pants, boots);
                limb(g, 0, -18, 26 + s * 3, -16 + c * 2, skin, skin, 10f);
                limb(g, 0, 18, 26 - s * 3, 16 - c * 2, skin, skin, 10f);
                g.setColor(Util.shade(skin, 0.75)); Util.oval(g, 28 + s * 3, -16, 6, 6); Util.oval(g, 28 - s * 3, 16, 6, 6);
                g.setPaint(new GradientPaint(-15, -22, Util.shade(skin, 1.1), 15, 22, Util.shade(skin, 0.6))); Util.oval(g, 0, 0, 15, 23);
                g.setColor(Util.shade(skin, 0.55)); g.setStroke(new BasicStroke(1.4f));
                Util.line(g, -4, -14, 6, -6); Util.line(g, -4, 14, 6, 6); Util.ring(g, 0, 0, 15, 23);
                g.setColor(new Color(120, 20, 20, 200)); Util.oval(g, 3, -8, 3, 2); Util.oval(g, -6, 9, 4, 2);
                g.setColor(new Color(90, 90, 96)); g.setStroke(new BasicStroke(2.5f)); Util.line(g, -13, -10, 10, 18);
                g.setColor(Util.shade(skin, 0.8)); for (int k = -3; k <= 3; k++) Util.oval(g, -11, k * 5, 2.4, 2);
                zHead(g, 10, 0, 8.5, Util.shade(skin, 0.95), r, true, false);
                break;
            }
            case Z.SPITTER: {
                leg(g, 0, -5, s * 6, -5.5, 6f, pants, boots);
                leg(g, 0, 5, -s * 6, 5.5, 6f, pants, boots);
                limb(g, 0, -10, 6 + s * 2, -14, shirt, skin, 4f);
                limb(g, 0, 10, 6 - s * 2, 14, shirt, skin, 4f);
                torsoShape(g, 0, 8.5, 12.5, shirt, r, 2);
                double sw = 1 + 0.08 * Math.sin(ph * 2);
                g.setPaint(new RadialGradientPaint(7, 0, (float) (11 * sw), new float[]{0, 1}, new Color[]{new Color(210, 230, 90), new Color(120, 150, 40)}));
                Util.oval(g, 7, 0, 8 * sw, 9.5 * sw);
                g.setColor(new Color(90, 110, 30)); g.setStroke(new BasicStroke(1f));
                Util.line(g, 2, -5, 11, -1); Util.line(g, 2, 5, 11, 2); Util.ring(g, 7, 0, 8 * sw, 9.5 * sw);
                zHead(g, 14, 0, 5.5, skin, r, false, false);
                g.setColor(new Color(160, 230, 60, 200)); Util.oval(g, 20, 1, 2, 1.5);
                break;
            }
            case Z.EXPLODER: {
                leg(g, 0, -7, s * 5, -8, 7f, pants, boots);
                leg(g, 0, 7, -s * 5, 8, 7f, pants, boots);
                limb(g, 0, -13, 9 + s * 2, -15, skin, skin, 4f);
                limb(g, 0, 13, 9 - s * 2, 15, skin, skin, 4f);
                Color body = new Color(172, 140, 96);
                double sw = 1 + 0.05 * Math.sin(ph * 2);
                g.setPaint(new RadialGradientPaint(-3, -3, 20, new float[]{0, 1}, new Color[]{Util.shade(body, 1.2), Util.shade(body, 0.65)}));
                Util.oval(g, 0, 0, 14 * sw, 16 * sw);
                for (int k = 0; k < 9; k++) {
                    double a = r.nextDouble() * Math.PI * 2, d = r.nextDouble() * 11;
                    double pr = (2.5 + r.nextDouble() * 2.5) * (1 + 0.25 * Math.sin(ph * 2 + k));
                    double px = Math.cos(a) * d, py = Math.sin(a) * d * 1.1;
                    g.setPaint(new RadialGradientPaint((float) px, (float) py, (float) pr, new float[]{0, 1}, new Color[]{new Color(255, 230, 110), new Color(220, 130, 30)}));
                    Util.oval(g, px, py, pr, pr);
                }
                g.setColor(Util.shade(body, 0.5)); g.setStroke(new BasicStroke(1.2f)); Util.ring(g, 0, 0, 14 * sw, 16 * sw);
                zHead(g, 13, 0, 5, skin, r, false, false);
                break;
            }
            case Z.SPIDERLING: {
                for (int side = -1; side <= 1; side += 2) for (int i = 0; i < 4; i++) {
                    double phi = ph * 1.5 + i * 1.6 + (side > 0 ? Math.PI : 0);
                    double bx = 4 - i * 4, kx = bx + 4 + Math.sin(phi) * 3, ky = side * 11, fx = bx + 7 + Math.sin(phi + 0.7) * 4 - i * 3, fy = side * 16;
                    g.setColor(new Color(50, 24, 54)); g.setStroke(Util.st(2.2f)); Util.line(g, bx, side * 3, kx, ky); Util.line(g, kx, ky, fx, fy);
                }
                g.setPaint(new RadialGradientPaint(-6, -2, 10, new float[]{0, 1}, new Color[]{new Color(150, 90, 160), new Color(60, 28, 70)}));
                Util.oval(g, -6, 0, 8, 7);
                g.setColor(new Color(170, 240, 90)); Util.oval(g, -7, -2, 1.6, 1.6); Util.oval(g, -4, 2, 1.4, 1.4);
                g.setColor(new Color(70, 36, 78)); Util.oval(g, 4, 0, 5, 4.5);
                g.setColor(new Color(255, 40, 40)); Util.oval(g, 7, -1.6, 1.1, 1.1); Util.oval(g, 7, 1.6, 1.1, 1.1); Util.oval(g, 6, 0, 0.9, 0.9);
                break;
            }
            case Z.CRAWLER: {
                leg(g, -7, -4, -17 - s * 2, -8, 4.5f, pants, boots);
                leg(g, -7, 4, -17 + s * 2, 8, 4.5f, pants, boots);
                limb(g, 3, -5, 14 + s * 5, -11, skin, skin, 3.5f);
                limb(g, 3, 5, 14 - s * 5, 11, skin, skin, 3.5f);
                g.setPaint(new GradientPaint(-10, -6, Util.shade(skin, 1.05), 10, 6, Util.shade(skin, 0.6))); Util.oval(g, -2, 0, 10, 6.5);
                g.setColor(Util.shade(skin, 0.55)); for (int k = -8; k <= 4; k += 3) Util.oval(g, k, 0, 1.3, 1.3);
                g.setColor(new Color(110, 15, 15, 200)); Util.oval(g, -4, 2, 3, 2);
                zHead(g, 9, 0, 5.5, skin, r, true, false);
                break;
            }
        }
    }

    private void drawSurvivor(Graphics2D g, double ph) {
        Color skin = new Color(206, 164, 128), shirt = new Color(190, 120, 60), pants = new Color(60, 70, 90);
        leg(g, 0, -5, 0, -5.5, 6f, pants, new Color(40, 30, 24));
        leg(g, 0, 5, 0, 5.5, 6f, pants, new Color(40, 30, 24));
        limb(g, 0, -10, 6 + Math.sin(ph) * 5, -20, shirt, skin, 4f);
        limb(g, 0, 10, 6, 11, shirt, skin, 4f);
        torsoShape(g, 0, 8.5, 12.5, shirt, new Random(4), 0);
        g.setColor(skin); Util.oval(g, 2, 0, 7, 7);
        g.setColor(new Color(80, 50, 30)); g.fill(new Arc2D.Double(-5, -7, 14, 14, 70, 220, Arc2D.CHORD));
    }

    private void drawBoss(Graphics2D g, int type, double ph, boolean atk, Random r) {
        double s = Math.sin(ph), c = Math.cos(ph);
        Color boots = new Color(28, 24, 22);
        switch (type) {
            case BT.CENSER: {
                // Hooded plague-priest swinging a smoking censer on a chain.
                Color robe = new Color(66, 74, 58), robeDark = new Color(38, 44, 34), bone = new Color(206, 200, 182);
                g.setColor(new Color(30, 26, 22));
                Util.oval(g, s * 10 + 4, -11, 7, 5); Util.oval(g, -s * 10 + 4, 11, 7, 5);
                // left arm: bony hand clutching prayer beads
                limb(g, 0, -22, 20 + s * 3, -22, robe, bone, 7f);
                g.setColor(new Color(120, 30, 30)); for (int k = 0; k < 5; k++) Util.oval(g, 18 + s * 3 + k * 2.4, -16 - k * 1.6, 1.7, 1.7);
                // right arm + chain + censer
                double hx = atk ? 26 : 14, hy = atk ? 10 : 24;
                limb(g, 0, 22, hx, hy, robe, bone, 7f);
                double swing = atk ? -0.9 : Math.sin(ph) * 0.6;
                double cx = hx + Math.cos(swing) * 22, cy = hy + 8 + Math.sin(swing) * 14 + (atk ? -14 : 6);
                g.setColor(new Color(150, 140, 110)); g.setStroke(new BasicStroke(1.6f));
                for (int k = 0; k <= 6; k++) Util.oval(g, hx + (cx - hx) * k / 6.0, hy + (cy - hy) * k / 6.0, 1.3, 1.3);
                for (int k = 0; k < 4; k++) {
                    g.setColor(new Color(120, 200, 80, 70 - k * 12));
                    Util.oval(g, cx - 6 - k * 6 + Math.sin(ph + k) * 3, cy - 3 - k * 3, 6 + k * 3, 5 + k * 2);
                }
                g.setPaint(new RadialGradientPaint((float) cx - 2, (float) cy - 2, 9, new float[]{0, 1}, new Color[]{new Color(214, 176, 90), new Color(110, 80, 34)}));
                Util.oval(g, cx, cy, 7.5, 7.5);
                g.setColor(new Color(60, 44, 20)); g.setStroke(new BasicStroke(1.2f)); g.draw(new Ellipse2D.Double(cx - 7.5, cy - 7.5, 15, 15));
                g.setColor(new Color(170, 255, 110)); for (int k = 0; k < 4; k++) Util.oval(g, cx + Math.cos(k * 1.57 + ph) * 4, cy + Math.sin(k * 1.57 + ph) * 4, 1.3, 1.3);
                // tattered robe body
                Path2D body = new Path2D.Double();
                Random rr = new Random(7);
                for (int k = 0; k <= 22; k++) {
                    double an = k / 22.0 * Math.PI * 2, rad = (k % 2 == 0 ? 1 : 0.9) * (an > Math.PI * 0.6 && an < Math.PI * 1.4 ? 1.08 : 1.0);
                    double px = Math.cos(an) * 23 * rad + (rr.nextDouble() - 0.5) * 2, py = Math.sin(an) * 28 * rad + (rr.nextDouble() - 0.5) * 2;
                    if (k == 0) body.moveTo(px, py); else body.lineTo(px, py);
                }
                body.closePath();
                g.setPaint(new GradientPaint(-22, -26, Util.shade(robe, 1.15), 22, 26, robeDark)); g.fill(body);
                g.setColor(new Color(24, 28, 22)); g.setStroke(new BasicStroke(1.6f)); g.draw(body);
                g.setColor(new Color(150, 120, 60)); g.setStroke(new BasicStroke(2.2f)); Util.line(g, -4, -24, -4, 24);
                g.setColor(new Color(110, 90, 50)); g.fillRect(-9, -3, 10, 6);
                // candles on the shoulders
                for (int sgn = -1; sgn <= 1; sgn += 2) {
                    g.setColor(new Color(230, 222, 196)); g.fillRoundRect(-12, sgn * 17 - 3, 7, 6, 3, 3);
                    g.setColor(new Color(255, 200, 90, 220)); Util.oval(g, -13, sgn * 17, 2.2 + Math.sin(ph * 3 + sgn) * 0.6, 1.6);
                }
                // hood with a gaunt face
                g.setColor(robeDark); Util.oval(g, 9, 0, 13, 13);
                g.setColor(new Color(20, 22, 18)); Util.oval(g, 13, 0, 8.5, 8.5);
                g.setColor(new Color(150, 150, 136)); Util.oval(g, 16, 0, 5.5, 6.5);
                g.setColor(new Color(170, 255, 110)); Util.oval(g, 19, -2.6, 1.5, 1.2); Util.oval(g, 19, 2.6, 1.5, 1.2);
                g.setColor(Util.shade(robeDark, 0.7)); g.setStroke(new BasicStroke(1.4f)); g.draw(new Ellipse2D.Double(-4, -13, 26, 26));
                break;
            }
            case BT.TANK: {
                Color skin = new Color(128, 124, 108);
                leg(g, -10, -14, -10 + s * 7, -18, 14f, new Color(60, 56, 50), boots); leg(g, -10, 14, -10 - s * 7, 18, 14f, new Color(60, 56, 50), boots);
                double ax = atk ? -8 : 36 + s * 4, ay = atk ? 44 : 28;
                limb(g, 2, -30, ax, -ay, skin, skin, 20f);
                limb(g, 2, 30, atk ? -8 : 36 - s * 4, ay, skin, skin, 20f);
                g.setColor(Util.shade(skin, 0.7)); Util.oval(g, ax + 2, -ay, 12, 12); Util.oval(g, (atk ? -8 : 36 - s * 4) + 2, ay, 12, 12);
                g.setPaint(new GradientPaint(-30, -36, Util.shade(skin, 1.15), 30, 36, Util.shade(skin, 0.6))); Util.oval(g, 0, 0, 30, 36);
                for (int k = 0; k < 14; k++) {
                    g.setColor(Util.shade(skin, 0.6 + r.nextDouble() * 0.5));
                    Util.oval(g, -22 + r.nextInt(40), -28 + r.nextInt(56), 3 + r.nextInt(5), 3 + r.nextInt(4));
                }
                g.setColor(Util.shade(skin, 0.45)); g.setStroke(new BasicStroke(2f)); Util.ring(g, 0, 0, 30, 36);
                Util.line(g, -10, -20, 6, -8); Util.line(g, -12, 18, 4, 4);
                if (atk) {
                    g.setColor(new Color(100, 96, 90)); Util.oval(g, -14, 0, 20, 18);
                    g.setColor(new Color(70, 66, 62)); g.setStroke(new BasicStroke(2f)); Util.ring(g, -14, 0, 20, 18);
                }
                g.setColor(Util.shade(skin, 0.9)); Util.oval(g, 24, 0, 9, 9);
                g.setColor(new Color(255, 210, 60)); Util.oval(g, 29, -3.5, 1.8, 1.8); Util.oval(g, 29, 3.5, 1.8, 1.8);
                break;
            }
            case BT.BROOD: {
                Color leg = new Color(64, 30, 64);
                for (int side = -1; side <= 1; side += 2) for (int i = 0; i < 3; i++) {
                    double bx = 10 - i * 14, by = side * 16, phi = ph + i * 2.1 + (side > 0 ? Math.PI : 0);
                    int legId = (2 - i) * 2 + (side > 0 ? 1 : 0);          // back legs are lost first
                    if (legId >= broodLegs) {
                        g.setColor(leg); g.setStroke(Util.st(6)); Util.line(g, bx, by, bx + 5, by + side * 9);
                        g.setColor(new Color(170, 30, 40)); Util.oval(g, bx + 5, by + side * 9, 3.5, 3.5);
                        continue;
                    }
                    double kx = bx + 10 + Math.sin(phi) * 7, ky = side * 46;
                    double fx = bx + 24 + Math.sin(phi + 0.8) * 9 - i * 6, fy = side * (62 - i * 2);
                    g.setColor(leg); g.setStroke(Util.st(6)); Util.line(g, bx, by, kx, ky);
                    g.setStroke(Util.st(4)); Util.line(g, kx, ky, fx, fy);
                    g.setColor(new Color(30, 10, 30)); Util.oval(g, fx, fy, 2.5, 2.5);
                }
                double pul = 1 + 0.05 * Math.sin(ph * 2);
                g.setPaint(new RadialGradientPaint(-26, -6, 40, new float[]{0, 1}, new Color[]{new Color(150, 90, 150), new Color(60, 30, 70)}));
                Util.oval(g, -26, 0, 34 * pul, 30 * pul);
                g.setColor(new Color(110, 40, 90)); g.setStroke(new BasicStroke(1.4f));
                for (int k = 0; k < 6; k++) Util.line(g, -26, 0, -26 + Math.cos(k) * 30, Math.sin(k) * 26);
                for (int k = 0; k < 7; k++) {
                    double ex = -40 + r.nextInt(30), ey = -18 + r.nextInt(36), er = 3 + r.nextInt(3);
                    g.setPaint(new RadialGradientPaint((float) ex, (float) ey, (float) (er * 1.4), new float[]{0, 1}, new Color[]{new Color(255, 250, 160), new Color(200, 170, 40)}));
                    Util.oval(g, ex, ey, er, er);
                }
                g.setColor(new Color(40, 20, 44)); g.setStroke(new BasicStroke(2f)); Util.ring(g, -26, 0, 34 * pul, 30 * pul);
                g.setPaint(new GradientPaint(0, -16, new Color(90, 50, 90), 20, 16, new Color(40, 20, 44))); Util.oval(g, 10, 0, 18, 16);
                g.setColor(new Color(70, 36, 74)); Util.oval(g, 30, 0, 10, 10);
                double mo = atk ? 9 : 4;
                g.setColor(new Color(220, 210, 190)); g.setStroke(Util.st(3));
                g.draw(new QuadCurve2D.Double(36, -4, 46, -mo - 4, 50, -2)); g.draw(new QuadCurve2D.Double(36, 4, 46, mo + 4, 50, 2));
                g.setColor(new Color(255, 40, 40));
                Util.oval(g, 35, -5, 1.8, 1.8); Util.oval(g, 35, 5, 1.8, 1.8); Util.oval(g, 33, -2, 1.5, 1.5); Util.oval(g, 33, 2, 1.5, 1.5);
                break;
            }
            case BT.S13: {
                Color skin = new Color(176, 190, 206);
                leg(g, 0, -6, s * 11, -7, 6f, new Color(50, 56, 66), boots); leg(g, 0, 6, -s * 11, 7, 6f, new Color(50, 56, 66), boots);
                double hx1 = atk ? 14 : 28 + s * 4, hy1 = atk ? -32 : -14, hx2 = atk ? 14 : 28 - s * 4, hy2 = atk ? 32 : 14;
                limb(g, 0, -11, hx1, hy1, skin, skin, 5f); limb(g, 0, 11, hx2, hy2, skin, skin, 5f);
                g.setColor(new Color(240, 250, 255)); g.setStroke(Util.st(1.6f));
                for (int k = -1; k <= 1; k++) { Util.line(g, hx1, hy1, hx1 + 9, hy1 + k * 3); Util.line(g, hx2, hy2, hx2 + 9, hy2 + k * 3); }
                g.setPaint(new GradientPaint(-9, -14, Util.shade(skin, 1.1), 9, 14, Util.shade(skin, 0.65))); Util.oval(g, 0, 0, 9, 14);
                g.setColor(new Color(40, 44, 52)); g.setStroke(new BasicStroke(2.2f)); Util.line(g, -6, -11, 6, 11); Util.line(g, -6, 11, 6, -11);
                g.setColor(new Color(80, 230, 255)); g.setStroke(Util.st(2f)); Util.line(g, -7, -8, -7, 8); Util.line(g, -3, -10, -3, 10);
                g.setColor(skin); Util.oval(g, 7, 0, 8, 6);
                g.setColor(new Color(120, 240, 255)); Util.oval(g, 12, -2.5, 1.8, 1.4); Util.oval(g, 12, 2.5, 1.8, 1.4);
                g.setColor(Util.shade(skin, 0.5)); g.setStroke(new BasicStroke(1f)); Util.ring(g, 7, 0, 8, 6);
                break;
            }
            case BT.GRAVES: {
                // Redesign: hulking mutated officer - armored, crimson growths, bone blades, minigun, red-eyed gas mask.
                Color armor = new Color(44, 50, 40), armorHi = new Color(84, 92, 70), flesh = new Color(146, 28, 38), fleshHi = new Color(214, 76, 62);
                Color bone = new Color(228, 216, 190), gun = new Color(56, 58, 64);
                leg(g, -2, -14, s * 10 - 2, -16, 14f, new Color(36, 40, 32), boots); leg(g, -2, 14, -s * 10 - 2, 16, 14f, new Color(36, 40, 32), boots);
                // ammo drum + feed belt
                g.setColor(new Color(50, 52, 56)); Util.oval(g, -30, 16, 13, 13);
                g.setColor(new Color(80, 82, 88)); g.setStroke(new BasicStroke(2f)); Util.ring(g, -30, 16, 13, 13);
                g.setColor(new Color(214, 170, 70)); for (int k = 0; k < 9; k++) Util.oval(g, -22 + k * 4.2, 22 + Math.sin(k + ph) * 1.5, 1.8, 2.4);
                // mutated claw arm
                double cx = 36 + s * 4, cy = -32;
                limb(g, 0, -28, cx, cy, flesh, flesh, 17f);
                g.setColor(fleshHi); Util.oval(g, 14, -32, 7, 6); Util.oval(g, 24, -34, 5, 5);
                g.setColor(new Color(255, 120, 60, 200)); g.setStroke(new BasicStroke(1.5f)); Util.line(g, 6, -30, 30, -34);
                g.setColor(bone);
                for (int k = -1; k <= 1; k++) {
                    Path2D blade = new Path2D.Double();
                    blade.moveTo(cx + 2, cy + k * 6 - 3); blade.quadTo(cx + 16, cy + k * 9, cx + 26 + Math.abs(k) * -4, cy + k * 12); blade.lineTo(cx + 2, cy + k * 6 + 3); blade.closePath();
                    g.fill(blade);
                }
                // minigun arm
                limb(g, 0, 28, 16, 30, armor, new Color(112, 120, 104), 13f);
                g.setPaint(new GradientPaint(12, 20, Util.shade(gun, 1.4), 12, 40, Util.shade(gun, 0.7))); g.fill(new RoundRectangle2D.Double(10, 20, 44, 20, 8, 8));
                g.setColor(new Color(30, 30, 34)); for (int k = 0; k < 6; k++) g.fillRect(52, 21 + k * 3, 20, 2);
                g.setColor(new Color(90, 92, 100)); g.fillRect(52, 20, 3, 20); g.fillRect(68, 20, 3, 20);
                if (atk) { g.setPaint(new RadialGradientPaint(76, 30, 13, new float[]{0, 1}, new Color[]{new Color(255, 230, 130, 255), new Color(255, 100, 20, 0)})); Util.oval(g, 76, 30, 13, 13); }
                // armored torso
                g.setPaint(new GradientPaint(-25, -31, armorHi, 25, 31, Util.shade(armor, 0.7))); Util.oval(g, 0, 0, 25, 31);
                g.setColor(new Color(30, 34, 26)); g.fill(new RoundRectangle2D.Double(-14, -30, 24, 15, 8, 8)); g.fill(new RoundRectangle2D.Double(-14, 15, 24, 15, 8, 8));
                g.setColor(new Color(220, 180, 50)); g.fillRect(-8, -26, 8, 5);
                g.setColor(new Color(255, 90, 50, 210)); g.setStroke(new BasicStroke(1.8f));
                Util.line(g, -10, -8, 4, 0); Util.line(g, 4, 0, -2, 10); Util.line(g, 8, -12, 16, -4);
                // crimson growths and bone spikes on the back
                for (int k = 0; k < 5; k++) {
                    double gx = -14 - (k % 2) * 6, gy = -16 + k * 8, gr = 6 + (k % 3) * 1.5 + Math.sin(ph * 2 + k) * 1.2;
                    g.setPaint(new RadialGradientPaint((float) gx, (float) gy, (float) gr, new float[]{0, 0.5f, 1}, new Color[]{new Color(255, 170, 80), flesh, Util.shade(flesh, 0.6)}));
                    Util.oval(g, gx, gy, gr, gr);
                }
                g.setColor(bone);
                for (int k = 0; k < 3; k++) {
                    double sy = -12 + k * 12;
                    g.fillPolygon(new int[]{-20, -40 - k % 2 * 4, -20}, new int[]{(int) sy - 4, (int) sy, (int) sy + 4}, 3);
                }
                g.setColor(new Color(20, 22, 18)); g.setStroke(new BasicStroke(1.8f)); Util.ring(g, 0, 0, 25, 31);
                // officer cap behind a cracked gas mask with burning red lenses
                g.setColor(new Color(40, 46, 32)); g.fill(new Arc2D.Double(-1, -14, 28, 28, 100, 160, Arc2D.CHORD));
                g.setColor(new Color(210, 170, 50)); g.setStroke(new BasicStroke(2f)); g.draw(new Arc2D.Double(1, -12, 24, 24, 110, 140, Arc2D.OPEN));
                g.setColor(new Color(58, 60, 64)); Util.oval(g, 15, 0, 10, 11);
                g.setColor(new Color(34, 36, 40)); Util.oval(g, 24, 0, 4, 4);
                for (int sgn = -1; sgn <= 1; sgn += 2) {
                    g.setPaint(new RadialGradientPaint(20, sgn * 4.5f, 5, new float[]{0, 1}, new Color[]{new Color(255, 230, 160), new Color(230, 30, 20)}));
                    Util.oval(g, 20, sgn * 4.5, 3.3, 3.3);
                }
                g.setColor(new Color(180, 180, 180, 160)); g.setStroke(new BasicStroke(1f)); Util.line(g, 18, -9, 22, -3);
                break;
            }
            case BT.SHEPHERD: {
                for (int i = 0; i < 9; i++) {
                    double a = i * Math.PI * 2 / 9 + Math.sin(ph + i) * 0.22;
                    double L = 56 + Math.sin(ph * 2 + i * 1.3) * 12;
                    double bx = Math.cos(a) * 26, by = Math.sin(a) * 26;
                    double ex = Math.cos(a) * L, ey = Math.sin(a) * L;
                    double mx = Math.cos(a + 0.5 * Math.sin(ph + i)) * L * 0.6, my = Math.sin(a + 0.5 * Math.sin(ph + i)) * L * 0.6;
                    for (int k = 0; k < 3; k++) {
                        g.setColor(Util.mix(new Color(110, 40, 120), new Color(40, 10, 50), k / 3.0));
                        g.setStroke(Util.st(7 - k * 2));
                        g.draw(new QuadCurve2D.Double(bx, by, mx, my, ex, ey));
                    }
                    g.setColor(new Color(230, 140, 255)); Util.oval(g, ex, ey, 2.4, 2.4);
                }
                double ax = atk ? 34 : 12, ay = atk ? 22 : 46;
                limb(g, 0, -28, ax, -ay, new Color(50, 18, 60), new Color(200, 196, 210), 5f);
                limb(g, 0, 28, ax, ay, new Color(50, 18, 60), new Color(200, 196, 210), 5f);
                if (atk) for (int sgn = -1; sgn <= 1; sgn += 2) {
                    g.setPaint(new RadialGradientPaint((float) ax + 4, (float) (sgn * ay), 12, new float[]{0, 1}, new Color[]{new Color(255, 200, 255, 255), new Color(200, 60, 255, 0)}));
                    Util.oval(g, ax + 4, sgn * ay, 12, 12);
                }
                Path2D robe = new Path2D.Double();
                for (int k = 0; k <= 24; k++) {
                    double a = k / 24.0 * Math.PI * 2, rr = 34 + (k % 2 == 0 ? 3 : -2) + r.nextInt(3);
                    if (k == 0) robe.moveTo(Math.cos(a) * rr, Math.sin(a) * rr); else robe.lineTo(Math.cos(a) * rr, Math.sin(a) * rr);
                }
                robe.closePath();
                g.setPaint(new RadialGradientPaint(0, 0, 38, new float[]{0, 1}, new Color[]{new Color(80, 26, 92), new Color(28, 8, 36)})); g.fill(robe);
                g.setColor(new Color(16, 4, 20)); g.setStroke(new BasicStroke(2f)); g.draw(robe);
                double pul = 13 + Math.sin(ph * 2) * 3;
                g.setPaint(new RadialGradientPaint(-6, 0, (float) pul, new float[]{0, 0.5f, 1}, new Color[]{new Color(255, 230, 255), new Color(240, 80, 230), new Color(140, 20, 160, 0)}));
                Util.oval(g, -6, 0, pul, pul);
                g.setColor(new Color(186, 176, 200)); Util.oval(g, 16, 0, 12, 12);
                g.setColor(new Color(240, 236, 220));
                for (int k = 0; k < 7; k++) {
                    double a = Math.PI * 0.55 + k * (Math.PI * 0.9 / 6);
                    double cx2 = 16 + Math.cos(a) * 11, cy2 = Math.sin(a) * 11;
                    Path2D sp = new Path2D.Double();
                    sp.moveTo(cx2 + Math.cos(a + 1.4) * 3, cy2 + Math.sin(a + 1.4) * 3);
                    sp.lineTo(16 + Math.cos(a) * 21, Math.sin(a) * 21);
                    sp.lineTo(cx2 + Math.cos(a - 1.4) * 3, cy2 + Math.sin(a - 1.4) * 3); sp.closePath();
                    g.fill(sp);
                }
                g.setColor(new Color(250, 200, 255)); Util.oval(g, 23, -4, 2.2, 2.2); Util.oval(g, 23, 4, 2.2, 2.2);
                break;
            }
        }
    }

    private BufferedImage makeCanopy(int kind, int v) {
        BufferedImage im = Util.img(80, 80); Graphics2D g = Util.g(im); g.translate(40, 40);
        Random r = new Random(kind * 10 + v);
        if (kind == 2) {
            g.setColor(new Color(34, 28, 24)); g.setStroke(Util.st(3));
            for (int k = 0; k < 7; k++) {
                double a = r.nextDouble() * 6.28, L = 18 + r.nextInt(14);
                Util.line(g, 0, 0, Math.cos(a) * L, Math.sin(a) * L);
                g.setStroke(Util.st(1.5f)); Util.line(g, Math.cos(a) * L * 0.6, Math.sin(a) * L * 0.6, Math.cos(a + 0.5) * L * 0.9, Math.sin(a + 0.5) * L * 0.9); g.setStroke(Util.st(3));
            }
            g.setColor(new Color(255, 120, 30, 200)); for (int k = 0; k < 4; k++) Util.oval(g, r.nextInt(30) - 15, r.nextInt(30) - 15, 1.5, 1.5);
        } else {
            Color a = kind == 0 ? new Color(46, 88, 40) : new Color(98, 88, 46), b = kind == 0 ? new Color(76, 124, 56) : new Color(130, 110, 56);
            g.setColor(new Color(0, 0, 0, 60)); Util.oval(g, 6, 8, 30, 26);
            for (int k = 0; k < 9; k++) {
                double cx = r.nextGaussian() * 9, cy = r.nextGaussian() * 9, rr = 11 + r.nextInt(8);
                g.setPaint(new RadialGradientPaint((float) (cx - 4), (float) (cy - 4), (float) rr * 1.4f, new float[]{0, 1}, new Color[]{b, a}));
                Util.oval(g, cx, cy, rr, rr);
            }
            for (int k = 0; k < 20; k++) { g.setColor(Util.alpha(Util.shade(b, 1.2), 140)); Util.oval(g, r.nextGaussian() * 14, r.nextGaussian() * 14, 1.6, 1.6); }
        }
        g.dispose();
        return im;
    }

    private BufferedImage makeItem(int k) {
        BufferedImage im = Util.img(40, 40); Graphics2D g = Util.g(im); g.translate(20, 20);
        switch (k) {
            case IK.RADIO:
                g.setColor(new Color(50, 54, 58)); g.fillRoundRect(-12, -7, 24, 15, 4, 4);
                g.setColor(new Color(30, 30, 32)); for (int i = 0; i < 4; i++) g.fillRect(-9, -4 + i * 3, 10, 1);
                g.setColor(new Color(200, 40, 40)); Util.oval(g, 7, -2, 2, 2);
                g.setColor(new Color(150, 150, 150)); g.setStroke(Util.st(1.5f)); Util.line(g, 9, -7, 15, -16);
                break;
            case IK.FUEL:
                g.setColor(new Color(190, 30, 30)); g.fillRoundRect(-9, -11, 18, 22, 5, 5);
                g.setColor(new Color(140, 20, 20)); g.setStroke(new BasicStroke(2f)); Util.line(g, -6, -7, 6, 7); Util.line(g, 6, -7, -6, 7);
                g.setColor(new Color(60, 60, 60)); g.fillRect(-4, -15, 8, 4);
                break;
            case IK.GENERATOR:
                g.setColor(new Color(210, 170, 30)); g.fillRoundRect(-14, -11, 28, 22, 4, 4);
                g.setColor(new Color(50, 46, 30)); for (int i = 0; i < 5; i++) g.fillRect(-10 + i * 5, -7, 3, 10);
                g.setColor(new Color(40, 40, 40)); Util.oval(g, 9, 6, 3, 3);
                break;
            case IK.DRIVE:
                g.setColor(new Color(26, 28, 32)); g.fillRoundRect(-9, -7, 18, 14, 3, 3);
                g.setColor(new Color(60, 200, 255)); g.fillRect(-6, 2, 12, 2);
                g.setColor(new Color(120, 120, 130)); g.fillRect(-4, -9, 8, 3);
                break;
            case IK.TRANSMITTER:
                g.setColor(new Color(70, 76, 66)); g.fillRect(-10, -4, 20, 14);
                g.setColor(new Color(160, 165, 170)); g.setStroke(Util.st(2)); g.draw(new Arc2D.Double(-12, -18, 24, 20, 200, 140, Arc2D.OPEN));
                Util.line(g, 0, -4, 0, -12);
                g.setColor(new Color(255, 60, 40)); Util.oval(g, 0, -13, 2.5, 2.5);
                break;
            case IK.SAMPLE:
                g.setColor(new Color(200, 230, 240, 180)); g.fillRoundRect(-4, -13, 8, 26, 6, 6);
                g.setColor(new Color(220, 20, 60)); g.fillRoundRect(-3, -3, 6, 15, 5, 5);
                g.setColor(new Color(120, 120, 130)); g.fillRect(-5, -15, 10, 4);
                break;
            default: break;
        }
        g.dispose();
        return im;
    }

    private BufferedImage makePickup(int k) {
        BufferedImage im = Util.img(24, 24); Graphics2D g = Util.g(im); g.translate(12, 12);
        switch (k) {
            case PK.CASH:
                for (int i = 2; i >= 0; i--) {
                    g.setColor(new Color(60, 140, 70)); g.fillRoundRect(-8 + i, -5 + i, 16, 9, 2, 2);
                    g.setColor(new Color(30, 90, 40)); g.drawRoundRect(-8 + i, -5 + i, 16, 9, 2, 2);
                }
                g.setColor(new Color(200, 240, 180)); g.setFont(new Font("SansSerif", Font.BOLD, 9)); g.drawString("$", -3, 5);
                break;
            case PK.AMMO:
                g.setColor(new Color(80, 86, 50)); g.fillRect(-8, -5, 16, 11);
                g.setColor(new Color(220, 190, 70)); for (int i = 0; i < 4; i++) g.fillRect(-6 + i * 4, -9, 2, 5);
                g.setColor(new Color(50, 54, 30)); g.drawRect(-8, -5, 16, 11);
                break;
            case PK.HEALTH:
                g.setColor(new Color(235, 235, 230)); g.fillRoundRect(-8, -7, 16, 14, 3, 3);
                g.setColor(new Color(210, 30, 30)); g.fillRect(-2, -5, 4, 10); g.fillRect(-5, -2, 10, 4);
                break;
            case PK.MAGNET:
                g.setColor(new Color(120, 200, 255, 90)); Util.oval(g, 0, 0, 11, 11);
                g.setStroke(new BasicStroke(5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
                g.setColor(new Color(210, 40, 40)); g.draw(new Arc2D.Double(-7, -8, 14, 14, 0, 180, Arc2D.OPEN));
                g.drawLine(-7, -1, -7, 5); g.drawLine(7, -1, 7, 5);
                g.setColor(new Color(220, 220, 225)); g.fillRect(-9, 4, 5, 4); g.fillRect(4, 4, 5, 4);
                break;
            case PK.GRENADE:
                g.setColor(new Color(60, 80, 46)); Util.oval(g, 0, 1, 6, 7);
                g.setColor(new Color(40, 54, 30)); g.drawLine(-5, 1, 5, 1);
                g.setColor(new Color(160, 160, 160)); g.fillRect(-2, -8, 4, 3); Util.ring(g, 4, -7, 2, 2);
                break;
        }
        g.dispose();
        return im;
    }

    // ------------------------------------------------------------------ public API
    public BufferedImage ground(int tile, int variant) { return ground[tile][variant & 3]; }
    public Color groundAverage(int tile) { return groundAvg[tile]; }
    public BufferedImage playerLegs(int frame) { return legs[frame & 7]; }
    public BufferedImage playerTorso(boolean flash) { return flash ? torsoFlash : torso; }
    public BufferedImage zombie(ZombieSprite s, int variant, int frame, boolean flash) { return (flash ? zff : zf)[s.ordinal()][variant % 3][frame & 7]; }
    public BufferedImage boss(BossSprite s, int frame, boolean attack, boolean flash) {
        int i = s.ordinal(), f = frame & 7;
        return flash ? bf[i][f] : attack ? ba[i][f] : bm[i][f];
    }
    public BufferedImage survivor(int frame) { return survivor[frame & 3]; }
    /** Broodmother frame showing only the legs she still has (0..6). */
    public BufferedImage broodmother(int legs, int frame, boolean attack, boolean flash) {
        int l = Math.max(0, Math.min(6, legs));
        return broodFrames[flash ? 2 : attack ? 1 : 0][l][frame & 7];
    }
    public BufferedImage canopy(int kind, int variant) { return canopy[kind * 2 + variant]; }
    public BufferedImage item(ItemSprite s) {
        switch (s) {
            case RADIO: return itemImg[IK.RADIO]; case FUEL: return itemImg[IK.FUEL]; case GENERATOR: return itemImg[IK.GENERATOR];
            case DRIVE: return itemImg[IK.DRIVE]; case TRANSMITTER: return itemImg[IK.TRANSMITTER]; default: return itemImg[IK.SAMPLE];
        }
    }
    public BufferedImage pickup(PickupSprite s) { return pickImg[s.ordinal()]; }
    public BufferedImage vignette() { return vignette; }
    public BufferedImage redVignette() { return redVig; }
}
