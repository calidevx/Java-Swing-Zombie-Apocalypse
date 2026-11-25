package deadsignal.entity.ability;

import deadsignal.core.Util;
import deadsignal.entity.DamageType;
import deadsignal.entity.Player;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;
import deadsignal.gfx.ParticleType;

import java.awt.*;

/** Tank ability: three stone imps land in an area and stomp every second - damage, mini-stun and slow. */
public final class ThumperPack extends GroundEffect {
    private static final float RADIUS = 110, STOMP_INTERVAL = 1f;
    private final float[][] imps = new float[3][2];
    private float stompTimer = 0.3f, squash;

    public ThumperPack(float x, float y) {
        super(x, y, RADIUS, 0.7f, 5f);
        for (int i = 0; i < imps.length; i++) {
            double a = i * Math.PI * 2 / 3 + Math.random();
            imps[i][0] = (float) Math.cos(a) * RADIUS * 0.5f;
            imps[i][1] = (float) Math.sin(a) * RADIUS * 0.5f;
        }
    }

    @Override protected void onTrigger(GameWorld w) {
        w.audio().play("explode", 0.4f, 1.5f);
        for (float[] imp : imps) for (int k = 0; k < 6; k++)
            w.particles().spawn(ParticleType.SMOKE, getX() + imp[0], getY() + imp[1], Util.rf(-50, 50), Util.rf(-50, 50), 0.6f, 8, new Color(130, 115, 95, 150));
    }

    @Override protected void whileActive(GameWorld w, float dt) {
        Player p = w.getPlayer();
        boolean inside = !p.isDead() && distanceTo(p) < RADIUS;
        if (inside) p.applySlow(0.6f, 0.2f);
        squash = Math.max(0, squash - dt * 4);
        stompTimer -= dt;
        if (stompTimer > 0) return;
        stompTimer = STOMP_INTERVAL;
        squash = 1;
        w.particles().spawn(ParticleType.RING, getX(), getY(), 0, 0, 0.35f, RADIUS, new Color(200, 170, 120));
        w.audio().play("hit", 0.7f, 0.5f);
        w.shake(3);
        if (inside) { p.hurt(7, getX(), getY(), DamageType.MELEE, w); p.stun(0.2f); }
    }

    @Override
    public void render(Graphics2D g, Assets a) {
        float x = getX(), y = getY();
        float fade = isTriggered() ? Math.min(1, (1 - activeFraction()) * 5) : windupProgress();
        g.setColor(new Color(110, 80, 50, (int) (70 * fade))); Util.oval(g, x, y, RADIUS, RADIUS);
        g.setColor(new Color(220, 170, 100, (int) (200 * fade)));
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{10, 8}, activeTime() * 20));
        Util.ring(g, x, y, RADIUS, RADIUS);
        for (int i = 0; i < imps.length; i++) {
            float ix = x + imps[i][0], iy = y + imps[i][1];
            float hop;
            if (!isTriggered()) hop = (1 - windupProgress()) * 120;           // falling in from above
            else hop = (float) Math.abs(Math.sin((1 - stompTimer / STOMP_INTERVAL) * Math.PI)) * 9 * (1 - squash);
            float sx = 1 + squash * 0.35f, sy = 1 - squash * 0.3f;
            g.setColor(new Color(0, 0, 0, 90)); Util.oval(g, ix, iy + 6, 10, 5);
            drawImp(g, ix, iy - hop, sx, sy, i);
        }
    }

    private static void drawImp(Graphics2D g, float x, float y, float sx, float sy, int seed) {
        Color body = new Color(112 + seed * 8, 104, 92);
        g.setColor(Util.shade(body, 0.7)); g.setStroke(Util.st(4));
        Util.line(g, x - 8 * sx, y, x - 13 * sx, y + 4); Util.line(g, x + 8 * sx, y, x + 13 * sx, y + 4);
        g.setPaint(new GradientPaint(x - 9, y - 9, Util.shade(body, 1.2), x + 9, y + 9, Util.shade(body, 0.7)));
        Util.oval(g, x, y, 10 * sx, 9 * sy);
        g.setColor(Util.shade(body, 0.5)); g.setStroke(new BasicStroke(1.3f)); Util.ring(g, x, y, 10 * sx, 9 * sy);
        g.setColor(new Color(60, 56, 50)); Util.oval(g, x - 3, y - 5 * sy, 3, 2); Util.oval(g, x + 4, y + 3 * sy, 2, 2);
        g.setColor(new Color(255, 150, 40)); Util.oval(g, x - 3.5, y - 1, 1.8, 1.8); Util.oval(g, x + 3.5, y - 1, 1.8, 1.8);
    }
}
