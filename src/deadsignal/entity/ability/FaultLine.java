package deadsignal.entity.ability;

import deadsignal.core.Util;
import deadsignal.entity.DamageType;
import deadsignal.entity.Player;
import deadsignal.entity.zombie.Zombie;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;
import deadsignal.gfx.ParticleType;
import deadsignal.world.GameMap;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Subject 13 ability: tears the floor open in a straight line. Anyone on the line is hurt and
 * stunned, and a temporary wall of rock rises that blocks movement AND bullets for a few seconds.
 */
public final class FaultLine extends GroundEffect {
    private static final float LENGTH = 330, HALF_WIDTH = 26, WALL_TIME = 6f;
    private final float dirX, dirY;
    private final float[] jagged = new float[24];
    private final List<int[]> raised = new ArrayList<>();
    private GameMap map;

    public FaultLine(float x, float y, float angle) {
        super(x, y, 20, 0.55f, WALL_TIME);
        dirX = (float) Math.cos(angle); dirY = (float) Math.sin(angle);
        Random r = new Random();
        for (int i = 0; i < jagged.length; i++) jagged[i] = (float) r.nextGaussian() * 5;
    }

    private float distanceToLine(float px, float py) {
        float t = Util.clamp((px - getX()) * dirX + (py - getY()) * dirY, 0, LENGTH);
        return Util.dist(px, py, getX() + dirX * t, getY() + dirY * t);
    }

    @Override protected void onTrigger(GameWorld w) {
        map = w.getMap();
        Player p = w.getPlayer();
        if (!p.isDead() && distanceToLine(p.getX(), p.getY()) < HALF_WIDTH) { p.hurt(30, getX(), getY(), DamageType.EXPLOSION, w); p.stun(1f); }
        for (Zombie z : w.getZombies()) if (distanceToLine(z.getX(), z.getY()) < HALF_WIDTH) z.takeDamage(40, DamageType.EXPLOSION, w);
        for (float t = 48; t <= LENGTH; t += 16) {
            float px = getX() + dirX * t, py = getY() + dirY * t;
            if (map.isSolidAt(px, py)) break;                     // the crack stops at walls
            int tx = GameMap.toTile(px), ty = GameMap.toTile(py);
            float cx = tx * 32 + 16, cy = ty * 32 + 16;
            if (Util.dist(cx, cy, p.getX(), p.getY()) < 34) continue;   // never trap anyone inside a rock
            boolean occupied = w.getBoss() != null && Util.dist(cx, cy, w.getBoss().getX(), w.getBoss().getY()) < 40;
            for (Zombie z : w.getZombies()) if (Util.dist(cx, cy, z.getX(), z.getY()) < 30) { occupied = true; break; }
            if (!occupied && map.raiseRock(tx, ty)) raised.add(new int[]{tx, ty});
            for (int k = 0; k < 3; k++) w.particles().spawn(ParticleType.CHUNK, px, py, Util.rf(-120, 120), Util.rf(-120, 120), 0.6f, 3, new Color(120, 110, 100));
        }
        w.shake(14);
        w.audio().play("explode", 0.9f, 0.7f);
    }

    @Override protected void onEnd(GameWorld w) { lowerAll(); }

    @Override public void remove() { lowerAll(); super.remove(); }

    private void lowerAll() {
        if (map == null) return;
        for (int[] t : raised) map.lowerRock(t[0], t[1]);
        raised.clear();
    }

    @Override
    public void render(Graphics2D g, Assets a) {
        float len = isTriggered() ? LENGTH : LENGTH * windupProgress();
        Path2D crack = new Path2D.Float();
        crack.moveTo(getX(), getY());
        for (int i = 1; i < jagged.length; i++) {
            float t = len * i / (jagged.length - 1);
            crack.lineTo(getX() + dirX * t - dirY * jagged[i], getY() + dirY * t + dirX * jagged[i]);
        }
        if (!isTriggered()) {
            g.setColor(new Color(255, 120, 30, 70)); g.setStroke(new BasicStroke(HALF_WIDTH * 2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Util.line(g, getX(), getY(), getX() + dirX * LENGTH, getY() + dirY * LENGTH);
            g.setColor(new Color(255, 190, 80, 230)); g.setStroke(new BasicStroke(3f)); g.draw(crack);
            return;
        }
        float fade = Math.min(1, (1 - activeFraction()) * 6);
        g.setColor(new Color(30, 20, 14, (int) (200 * fade))); g.setStroke(new BasicStroke(5f)); g.draw(crack);
        g.setColor(new Color(255, 110, 30, (int) (120 * fade))); g.setStroke(new BasicStroke(1.5f)); g.draw(crack);
        for (int[] t : raised) drawRock(g, t[0] * 32 + 16, t[1] * 32 + 16, t[0] * 31 + t[1] * 17, fade);
    }

    private static void drawRock(Graphics2D g, float cx, float cy, int seed, float fade) {
        Random r = new Random(seed);
        Path2D p = new Path2D.Float();
        for (int k = 0; k < 8; k++) {
            double a = k / 8.0 * Math.PI * 2, rr = 12 + r.nextInt(5);
            if (k == 0) p.moveTo(cx + Math.cos(a) * rr, cy + Math.sin(a) * rr); else p.lineTo(cx + Math.cos(a) * rr, cy + Math.sin(a) * rr);
        }
        p.closePath();
        int al = (int) (255 * fade);
        g.setColor(new Color(0, 0, 0, al / 3)); g.translate(3, 5); g.fill(p); g.translate(-3, -5);
        g.setPaint(new GradientPaint(cx - 12, cy - 12, new Color(150, 140, 128, al), cx + 12, cy + 12, new Color(80, 74, 68, al)));
        g.fill(p);
        g.setColor(new Color(50, 46, 42, al)); g.setStroke(new BasicStroke(1.5f)); g.draw(p);
        g.setColor(new Color(255, 140, 50, al / 2)); g.setStroke(new BasicStroke(1f)); Util.line(g, cx - 5, cy + 2, cx + 4, cy - 3);
    }
}
