package deadsignal.entity.boss;

import deadsignal.core.Util;
import deadsignal.entity.DamageType;
import deadsignal.entity.Player;
import deadsignal.entity.ability.ThumperPack;
import deadsignal.entity.projectile.Boulder;
import deadsignal.game.GameWorld;
import deadsignal.gfx.BossSprite;
import deadsignal.gfx.ParticleType;

import java.awt.*;

/** Hurls boulders, slams the ground, and throws THUMPER PACKS - stone imps that stomp an area. */
public final class Tank extends Boss {
    private enum Attack { NONE, SLAM, THROW, THUMPERS }
    private Attack pending = Attack.NONE;
    private float windup, throwCooldown = 2.5f, slamCooldown = 3f, thumperCooldown = 1.5f, slamX, slamY;
    private static final float SLAM_RADIUS = 175;

    public Tank(float x, float y, float hpMul) {
        super("THE TANK", "Seismic-class infected", BossSprite.TANK, x, y, 40, 2300 * hpMul, 15);
    }

    @Override
    protected void think(GameWorld w, float dt, float dx, float dy, float d) {
        throwCooldown -= dt; slamCooldown -= dt; thumperCooldown -= dt;
        float frac = getHealthFraction();
        if (pending == Attack.NONE) {
            chase(w, frac < 0.5f ? 64 : 54, dt, dx, dy, d);
            if (slamCooldown <= 0 && d < 160) { pending = Attack.SLAM; windup = 0.9f; slamX = getX(); slamY = getY(); attackPose(0.9f); w.audio().play("roar", 0.6f, 0.8f); }
            else if (thumperCooldown <= 0 && d < 480) { pending = Attack.THUMPERS; windup = 0.5f; attackPose(0.5f); }
            else if (throwCooldown <= 0 && canSeePlayer() && d > 140) { pending = Attack.THROW; windup = 0.6f; attackPose(0.6f); }
            return;
        }
        faceToward((float) Math.atan2(dy, dx), 3 * dt);
        windup -= dt;
        if (windup > 0) return;
        if (pending == Attack.SLAM) {
            w.particles().spawn(ParticleType.RING, slamX, slamY, 0, 0, 0.45f, SLAM_RADIUS, new Color(255, 200, 150));
            for (int i = 0; i < 24; i++) { float a = i / 24f * 6.28f; w.particles().spawn(ParticleType.SMOKE, slamX, slamY, (float) Math.cos(a) * 300, (float) Math.sin(a) * 300, 0.6f, 12, new Color(120, 110, 100, 150)); }
            if (w.getPlayer().distanceTo(slamX, slamY) < SLAM_RADIUS) w.getPlayer().hurt(30, slamX, slamY, DamageType.EXPLOSION, w);
            w.shake(18); w.audio().play("explode", 1f, 0.6f);
            slamCooldown = 4.5f;
        } else if (pending == Attack.THUMPERS) {
            Player p = w.getPlayer();
            w.addEffect(new ThumperPack(p.getX(), p.getY()));
            announce(w, "THUMPER PACK", new Color(230, 180, 110));
            w.audio().play("throw", 1f, 0.6f);
            thumperCooldown = frac < 0.5f ? 5.5f : 7.5f;
        } else {
            int n = frac < 0.5f ? 3 : 1;
            float pa = (float) Math.atan2(dy, dx);
            for (int k = 0; k < n; k++)
                w.addProjectile(new Boulder(getX() + (float) Math.cos(getAngle()) * 30, getY() + (float) Math.sin(getAngle()) * 30, pa + (k - (n - 1) / 2f) * 0.3f, Math.min(2.4f, d / 390 + 0.4f)));
            w.audio().play("throw", 1f, 0.5f);
            throwCooldown = 2.6f;
        }
        pending = Attack.NONE;
    }

    @Override protected void renderTelegraph(Graphics2D g) {
        if (pending != Attack.SLAM) return;
        float k = 1 - windup / 0.9f;
        g.setColor(new Color(255, 40, 30, (int) (50 + 70 * k))); Util.oval(g, slamX, slamY, SLAM_RADIUS, SLAM_RADIUS);
        g.setColor(new Color(255, 70, 40, 220)); g.setStroke(new BasicStroke(3f)); Util.ring(g, slamX, slamY, SLAM_RADIUS * k, SLAM_RADIUS * k);
    }
}
