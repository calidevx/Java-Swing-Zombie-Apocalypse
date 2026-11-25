package deadsignal.world;

import deadsignal.core.Util;
import deadsignal.gfx.Assets;
import deadsignal.gfx.ZombieSprite;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/** Pre-renders a generated map into one big ground image (single responsibility: drawing tiles). */
final class MapRenderer {
    private MapRenderer() { }

    private static boolean isWallAt(GameMap m, int x, int y) { return Tile.isWall(m.getTile(x, y)); }

    static void render(GameMap m, Assets A) {
        m.image = new BufferedImage(m.w * 32, m.h * 32, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = m.image.createGraphics();
        Util.hq(g);
        Random r = new Random(1234 + m.w);
        for (int y = 0; y < m.h; y++) for (int x = 0; x < m.w; x++) {
            int i = y * m.w + x, id = m.tiles[i];
            int gid = Tile.isSolid(id) ? m.under[i] : id;
            if (Tile.isSolid(gid)) gid = m.theme.getGround();
            g.drawImage(A.ground(gid, ((x * 7 + y * 13) ^ (x * y)) & 3), x * 32, y * 32, null);
        }
        // decals: cracks, trash, blood, manholes
        for (int k = 0; k < 140; k++) {
            int x = 1 + r.nextInt(m.w - 2), y = 1 + r.nextInt(m.h - 2), id = m.getTile(x, y);
            if (Tile.isSolid(id) || Tile.isAnimated(id)) continue;
            float px = x * 32 + r.nextInt(32), py = y * 32 + r.nextInt(32);
            int kind = r.nextInt(10);
            if (kind < 3) { Util.splat(g, px, py, 6 + r.nextInt(10), r, new Color(110, 10, 10)); }
            else if (kind < 6) {
                g.setColor(new Color(220, 215, 200, 170));
                AffineTransform at = g.getTransform(); g.translate(px, py); g.rotate(r.nextDouble() * 6);
                g.fillRect(-3, -2, 6, 4); g.setTransform(at);
            } else if (kind < 8) {
                g.setColor(new Color(0, 0, 0, 80)); g.setStroke(new BasicStroke(1f));
                Path2D p = new Path2D.Float(); p.moveTo(px, py);
                for (int s = 0; s < 4; s++) { px += r.nextInt(17) - 8; py += r.nextInt(17) - 8; p.lineTo(px, py); }
                g.draw(p);
            } else if (id == Tile.ROAD) {
                g.setColor(new Color(40, 40, 42)); Util.oval(g, x * 32 + 16, y * 32 + 16, 10, 10);
                g.setColor(new Color(70, 70, 72)); g.setStroke(new BasicStroke(1.5f)); Util.ring(g, x * 32 + 16, y * 32 + 16, 10, 10);
                Util.line(g, x * 32 + 8, y * 32 + 16, x * 32 + 24, y * 32 + 16);
            } else {
                g.setColor(new Color(0, 0, 0, 50)); Util.oval(g, px, py, 10 + r.nextInt(10), 6 + r.nextInt(6));
            }
        }
        // corpses lying around
        for (int k = 0; k < 18; k++) {
            int x = 2 + r.nextInt(m.w - 4), y = 2 + r.nextInt(m.h - 4);
            if (Tile.isSolid(m.getTile(x, y)) || Tile.isAnimated(m.getTile(x, y))) continue;
            float px = x * 32 + 16, py = y * 32 + 16;
            Util.splat(g, px, py, 14, r, new Color(95, 8, 8));
            BufferedImage im = Util.darken(A.zombie(r.nextInt(2) == 0 ? ZombieSprite.WALKER : ZombieSprite.RUNNER, r.nextInt(3), 0, false), 0.55f, 0.85f);
            AffineTransform at = g.getTransform(); g.translate(px, py); g.rotate(r.nextDouble() * 6.28); g.scale(1, 0.85);
            g.drawImage(im, -im.getWidth() / 2, -im.getHeight() / 2, null); g.setTransform(at);
        }
        // helipad for military
        if (m.theme == Theme.MILITARY) {
            for (int k = 0; k < 3; k++) {
                int x = 6 + r.nextInt(m.w - 12), y = 6 + r.nextInt(m.h - 12);
                boolean ok = true;
                for (int oy = -2; oy <= 2 && ok; oy++) for (int ox = -2; ox <= 2 && ok; ox++) if (m.isSolid(x + ox, y + oy)) ok = false;
                if (!ok) continue;
                float px = x * 32 + 16, py = y * 32 + 16;
                g.setColor(new Color(80, 80, 78)); Util.oval(g, px, py, 70, 70);
                g.setColor(new Color(220, 190, 60)); g.setStroke(new BasicStroke(4f)); Util.ring(g, px, py, 62, 62);
                g.setFont(new Font("SansSerif", Font.BOLD, 64)); g.drawString("H", px - 22, py + 23);
            }
        }
        // shadows cast by solids
        for (int y = 0; y < m.h; y++) for (int x = 0; x < m.w; x++) {
            if (m.isSolid(x, y)) continue;
            int px = x * 32, py = y * 32;
            if (m.isSolid(x, y - 1)) { g.setPaint(new GradientPaint(px, py, new Color(0, 0, 0, 110), px, py + 9, new Color(0, 0, 0, 0))); g.fillRect(px, py, 32, 9); }
            if (m.isSolid(x - 1, y)) { g.setPaint(new GradientPaint(px, py, new Color(0, 0, 0, 90), px + 7, py, new Color(0, 0, 0, 0))); g.fillRect(px, py, 7, 32); }
        }
        // walls & props
        for (int y = 0; y < m.h; y++) for (int x = 0; x < m.w; x++) {
            int id = m.getTile(x, y);
            if (!Tile.isSolid(id)) continue;
            Random rr = new Random(x * 92821L + y * 17L);
            if (Tile.isWall(id)) drawWall(m, g, id, x, y, rr);
            else drawProp(g, id, x * 32, y * 32, rr);
        }
        for (CarWreck c : m.cars) drawCar(g, c, m.theme);
        for (int[] l : m.lamps) {
            float px = l[0] * 32 + 16, py = l[1] * 32 + 16;
            g.setColor(new Color(0, 0, 0, 90)); Util.oval(g, px + 3, py + 3, 5, 5);
            g.setColor(new Color(50, 52, 56)); Util.oval(g, px, py, 5, 5);
            g.setColor(m.theme == Theme.SEWER ? new Color(140, 230, 160) : new Color(255, 230, 160)); Util.oval(g, px, py, 2.5, 2.5);
        }
        for (Tree tr : m.trees) {
            g.setColor(new Color(0, 0, 0, 80)); Util.oval(g, tr.getX() + 4, tr.getY() + 5, 9, 7);
            g.setColor(tr.getKind() == 2 ? new Color(40, 30, 24) : new Color(84, 60, 38)); Util.oval(g, tr.getX(), tr.getY(), 7, 7);
        }
        m.decals = m.image.createGraphics(); Util.hq(m.decals);
        // minimap
        m.minimap = new BufferedImage(m.w * 2, m.h * 2, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < m.h; y++) for (int x = 0; x < m.w; x++) {
            int id = m.getTile(x, y);
            Color c;
            if (Tile.isWall(id)) c = new Color(150, 150, 150);
            else if (Tile.isSolid(id)) c = new Color(90, 90, 90);
            else c = Util.shade(A.groundAverage(id), 0.65);
            int rgb = (220 << 24) | (c.getRGB() & 0xFFFFFF);
            m.minimap.setRGB(x * 2, y * 2, rgb); m.minimap.setRGB(x * 2 + 1, y * 2, rgb); m.minimap.setRGB(x * 2, y * 2 + 1, rgb); m.minimap.setRGB(x * 2 + 1, y * 2 + 1, rgb);
        }
        g.dispose();
    }

    static void drawWall(GameMap m, Graphics2D g, int id, int x, int y, Random r) {
        int px = x * 32, py = y * 32;
        boolean n = isWallAt(m, x, y - 1), s = isWallAt(m, x, y + 1), e = isWallAt(m, x + 1, y), w2 = isWallAt(m, x - 1, y);
        Color top, front;
        switch (id) {
            case Tile.WALL_WHITE: top = new Color(214, 216, 212); front = new Color(160, 166, 168); break;
            case Tile.WALL_STEEL: top = new Color(82, 90, 100); front = new Color(52, 58, 66); break;
            case Tile.WALL_SEWER: top = new Color(80, 74, 62); front = new Color(56, 50, 42); break;
            case Tile.WALL_BURNT: top = new Color(56, 48, 44); front = new Color(36, 30, 28); break;
            default: top = new Color(104, 74, 62); front = new Color(124, 60, 44);
        }
        int fh = s ? 0 : 12;
        g.setColor(top); g.fillRect(px, py, 32, 32 - fh);
        for (int i = 0; i < 12; i++) {
            g.setColor(Util.shade(top, r.nextBoolean() ? 0.9 : 1.08));
            g.fillRect(px + r.nextInt(30), py + r.nextInt(Math.max(1, 30 - fh)), 2, 2);
        }
        if (id == Tile.WALL_STEEL) {
            g.setColor(Util.shade(top, 0.8)); g.drawLine(px, py + 15, px + 31, py + 15);
            g.setColor(Util.shade(top, 1.3)); g.fillRect(px + 3, py + 3, 2, 2); g.fillRect(px + 27, py + 3, 2, 2);
        } else if (id == Tile.WALL_WHITE) {
            g.setColor(Util.shade(top, 0.93)); g.drawLine(px + 16, py, px + 16, py + 31 - fh);
        } else if (id == Tile.WALL_BURNT) {
            g.setColor(new Color(20, 16, 14, 120)); Util.oval(g, px + r.nextInt(32), py + r.nextInt(20), 8, 6);
        }
        if (fh > 0) {
            int fy = py + 32 - fh;
            g.setPaint(new GradientPaint(px, fy, front, px, fy + fh, Util.shade(front, 0.65)));
            g.fillRect(px, fy, 32, fh);
            g.setColor(Util.shade(front, 0.55));
            if (id == Tile.WALL_BRICK || id == Tile.WALL_SEWER || id == Tile.WALL_BURNT) {
                g.drawLine(px, fy + 6, px + 31, fy + 6);
                for (int bx = 0; bx < 32; bx += 10) { g.drawLine(px + bx, fy, px + bx, fy + 5); g.drawLine(px + bx + 5, fy + 6, px + bx + 5, fy + 11); }
            } else if (id == Tile.WALL_STEEL) {
                g.setColor(new Color(200, 170, 40)); for (int k = 0; k < 32; k += 8) g.fillRect(px + k, fy + 8, 4, 3);
            } else if (id == Tile.WALL_WHITE && (x % 3 == 0)) {
                g.setColor(new Color(80, 110, 140)); g.fillRect(px + 8, fy + 2, 16, 6);
                g.setColor(new Color(150, 190, 220, 120)); g.fillRect(px + 9, fy + 3, 6, 2);
            }
            g.setColor(Util.shade(top, 1.25)); g.fillRect(px, fy, 32, 1);
        }
        g.setColor(Util.shade(top, 0.6));
        if (!n) g.fillRect(px, py, 32, 2);
        if (!w2) g.fillRect(px, py, 2, 32 - fh);
        if (!e) g.fillRect(px + 30, py, 2, 32 - fh);
        if (!n) { g.setColor(Util.shade(top, 1.25)); g.fillRect(px + 2, py + 2, 28, 1); }
    }

    static void drawProp(Graphics2D g, int id, int px, int py, Random r) {
        float cx = px + 16, cy = py + 16;
        switch (id) {
            case Tile.CRATE: {
                g.setColor(new Color(0, 0, 0, 90)); g.fillRect(px + 6, py + 6, 26, 26);
                g.setColor(new Color(136, 98, 54)); g.fillRect(px + 3, py + 3, 26, 26);
                g.setColor(new Color(160, 118, 70)); for (int k = 0; k < 4; k++) g.fillRect(px + 4, py + 4 + k * 6, 24, 4);
                g.setColor(new Color(90, 62, 34)); g.setStroke(new BasicStroke(2.5f)); g.drawRect(px + 3, py + 3, 26, 26);
                Util.line(g, px + 4, py + 4, px + 28, py + 28);
                break;
            }
            case Tile.BARREL: {
                Color[] cs = {new Color(160, 40, 32), new Color(40, 80, 140), new Color(70, 110, 60), new Color(130, 90, 50)};
                Color c = cs[r.nextInt(cs.length)];
                g.setColor(new Color(0, 0, 0, 90)); Util.oval(g, cx + 3, cy + 3, 12, 12);
                g.setPaint(new RadialGradientPaint(cx - 4, cy - 4, 16, new float[]{0, 1}, new Color[]{Util.shade(c, 1.3), Util.shade(c, 0.7)}));
                Util.oval(g, cx, cy, 12, 12);
                g.setColor(Util.shade(c, 0.5)); g.setStroke(new BasicStroke(2f)); Util.ring(g, cx, cy, 12, 12); Util.ring(g, cx, cy, 7, 7);
                g.setColor(new Color(230, 200, 40)); Util.oval(g, cx + 4, cy - 3, 2, 2);
                break;
            }
            case Tile.TANK: {
                g.setColor(new Color(0, 0, 0, 100)); Util.oval(g, cx + 3, cy + 3, 15, 15);
                g.setColor(new Color(110, 118, 126)); Util.oval(g, cx, cy, 15, 15);
                g.setPaint(new RadialGradientPaint(cx - 3, cy - 3, 14, new float[]{0, 1}, new Color[]{new Color(90, 230, 170, 230), new Color(20, 90, 70, 240)}));
                Util.oval(g, cx, cy, 12, 12);
                g.setColor(new Color(30, 50, 40, 200)); Util.oval(g, cx + 1, cy + 1, 5, 4); Util.oval(g, cx + 5, cy + 1, 2, 3);
                g.setColor(new Color(255, 255, 255, 120)); g.setStroke(new BasicStroke(1.5f)); g.draw(new Arc2D.Double(cx - 9, cy - 9, 18, 18, 100, 70, Arc2D.OPEN));
                break;
            }
            case Tile.ROCK: {
                Path2D p = new Path2D.Double();
                for (int k = 0; k < 9; k++) {
                    double a = k / 9.0 * Math.PI * 2, rr = 10 + r.nextInt(5);
                    if (k == 0) p.moveTo(cx + Math.cos(a) * rr, cy + Math.sin(a) * rr); else p.lineTo(cx + Math.cos(a) * rr, cy + Math.sin(a) * rr);
                }
                p.closePath();
                AffineTransform at = g.getTransform();
                g.translate(3, 4); g.setColor(new Color(0, 0, 0, 90)); g.fill(p); g.setTransform(at);
                g.setPaint(new GradientPaint(cx - 10, cy - 10, new Color(130, 126, 120), cx + 10, cy + 10, new Color(70, 68, 64))); g.fill(p);
                g.setColor(new Color(50, 48, 46)); g.setStroke(new BasicStroke(1.2f)); g.draw(p);
                break;
            }
            case Tile.DESK: {
                g.setColor(new Color(0, 0, 0, 90)); g.fillRect(px + 5, py + 9, 28, 20);
                g.setColor(new Color(104, 74, 48)); g.fillRect(px + 2, py + 6, 28, 20);
                g.setColor(new Color(70, 48, 30)); g.drawRect(px + 2, py + 6, 28, 20);
                g.setColor(new Color(30, 32, 36)); g.fillRect(px + 6, py + 9, 11, 8);
                g.setColor(new Color(60, 120, 150)); g.fillRect(px + 7, py + 10, 9, 5);
                g.setColor(new Color(220, 218, 205)); g.fillRect(px + 19, py + 14, 8, 9);
                break;
            }
            case Tile.FENCE: {
                g.setColor(new Color(150, 150, 150, 160)); g.setStroke(new BasicStroke(1f));
                for (int k = -32; k < 32; k += 6) { Util.line(g, px + k, py, px + k + 32, py + 32); Util.line(g, px + k + 32, py, px + k, py + 32); }
                g.setColor(new Color(90, 90, 92)); g.fillRect(px, py + 14, 32, 4);
                break;
            }
            case Tile.SANDBAG: {
                g.setColor(new Color(0, 0, 0, 90)); g.fillRoundRect(px + 3, py + 5, 30, 28, 8, 8);
                for (int row = 0; row < 3; row++) for (int k = 0; k < 2; k++) {
                    int bx = px + 1 + k * 15 + (row % 2) * 4, by = py + 2 + row * 10;
                    g.setColor(new Color(156, 136, 94)); g.fillRoundRect(bx, by, 15, 10, 7, 7);
                    g.setColor(new Color(110, 94, 62)); g.drawRoundRect(bx, by, 15, 10, 7, 7);
                }
                break;
            }
            case Tile.TREE: break;
            default: break;
        }
    }

    static void drawCar(Graphics2D g, CarWreck c, Theme th) {
        boolean horiz = c.horizontal, burnt = c.burnt;
        float cx = c.tx * 32 + (horiz ? 32 : 16), cy = c.ty * 32 + (horiz ? 16 : 32);
        AffineTransform at = g.getTransform();
        g.translate(cx, cy);
        g.rotate((horiz ? 0 : Math.PI / 2) + (c.flipped ? Math.PI : 0));
        Color body = burnt ? new Color(52, 44, 40) : th.carColor(c.colorIndex);
        g.setColor(new Color(0, 0, 0, 100)); g.fill(new RoundRectangle2D.Double(-27, -11, 60, 28, 12, 12));
        g.setColor(new Color(20, 20, 22)); g.fillRect(-21, -15, 10, 4); g.fillRect(12, -15, 10, 4); g.fillRect(-21, 11, 10, 4); g.fillRect(12, 11, 10, 4);
        g.setColor(Util.shade(body, 0.6)); g.fill(new RoundRectangle2D.Double(-30, -14, 60, 28, 12, 12));
        g.setPaint(new GradientPaint(0, -13, Util.shade(body, 1.15), 0, 13, Util.shade(body, 0.8)));
        g.fill(new RoundRectangle2D.Double(-29, -13, 58, 26, 10, 10));
        g.setColor(burnt ? new Color(20, 18, 16) : new Color(36, 52, 68));
        g.fill(new RoundRectangle2D.Double(5, -11, 9, 22, 4, 4));
        g.fill(new RoundRectangle2D.Double(-21, -10, 7, 20, 4, 4));
        g.setColor(Util.shade(body, 1.1)); g.fill(new RoundRectangle2D.Double(-13, -11, 17, 22, 4, 4));
        if (!burnt) {
            g.setColor(new Color(255, 255, 255, 60)); g.fillRect(6, -10, 3, 9);
            g.setColor(new Color(230, 230, 190)); g.fillRect(27, -11, 3, 5); g.fillRect(27, 6, 3, 5);
            g.setColor(new Color(170, 30, 30)); g.fillRect(-30, -11, 2, 5); g.fillRect(-30, 6, 2, 5);
            if (body.getRed() > 200 && body.getGreen() > 200) { g.setColor(new Color(200, 30, 30)); g.fillRect(-8, -3, 8, 6); g.fillRect(-7, -6, 6, 12); }
        } else {
            g.setColor(new Color(90, 40, 20, 160)); Util.oval(g, -5, 2, 9, 5); Util.oval(g, 14, -4, 5, 4);
        }
        g.setColor(Util.shade(body, 0.45)); g.setStroke(new BasicStroke(1.2f)); g.draw(new RoundRectangle2D.Double(-30, -14, 60, 28, 12, 12));
        g.setTransform(at);
    }
}
