package deadsignal.entity.projectile;

import deadsignal.core.Util;
import deadsignal.entity.DamageType;
import deadsignal.entity.Damageable;
import deadsignal.entity.LightningArc;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;
import deadsignal.gfx.LightEmitter;
import deadsignal.gfx.ParticleType;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Tesla Rifle round: on impact, lightning chains to up to 3 more nearby enemies. */
public final class TeslaBolt extends Projectile implements LightEmitter {
    private static final int CHAINS = 3;
    private static final float CHAIN_RANGE = 170, CHAIN_FALLOFF = 0.75f;
    private static final Color CORE = new Color(200, 245, 255), GLOW = new Color(80, 200, 255);
    private final int shotId;

    public TeslaBolt(float x, float y, float angle, float damage, int shotId) {
        super(x, y, 4, angle, 1150, 0.7f, damage, true);
        this.shotId = shotId;
    }

    @Override
    protected boolean onHitTarget(Damageable first, GameWorld w) {
        Set<Damageable> hit = new HashSet<>();
        List<float[]> path = new ArrayList<>();
        path.add(new float[]{getX(), getY()});
        Damageable current = first;
        float dmg = getDamage();
        for (int jump = 0; jump <= CHAINS && current != null; jump++) {
            hit.add(current);
            path.add(new float[]{current.getX(), current.getY()});
            current.takeDamage(dmg, DamageType.ENERGY, w);
            w.onPlayerDamageDealt(dmg);
            for (int k = 0; k < 5; k++)
                w.particles().spawn(ParticleType.SPARK, current.getX(), current.getY(), Util.rf(-160, 160), Util.rf(-160, 160), 0.25f, 1.6f, CORE);
            dmg *= CHAIN_FALLOFF;
            current = nearestUnhit(w, current, hit);
        }
        w.addEffect(new LightningArc(path, GLOW, CORE));
        w.addFlash(first.getX(), first.getY(), 160, 0.12f, GLOW);
        w.stats().registerHit(shotId);
        w.audio().play("zap", 0.7f, 1.3f);
        return true;
    }

    private static Damageable nearestUnhit(GameWorld w, Damageable from, Set<Damageable> hit) {
        Damageable best = null;
        float bd = CHAIN_RANGE;
        for (Damageable d : w.getDamageables()) {
            if (hit.contains(d) || !d.isTargetable()) continue;
            float dist = Util.dist(from.getX(), from.getY(), d.getX(), d.getY());
            if (dist < bd) { bd = dist; best = d; }
        }
        return best;
    }

    @Override protected void onHitWall(GameWorld w) {
        for (int k = 0; k < 6; k++) w.particles().spawn(ParticleType.SPARK, getX(), getY(), Util.rf(-120, 120), Util.rf(-120, 120), 0.2f, 1.5f, CORE);
    }

    @Override
    public void render(Graphics2D g, Assets a) {
        float l = Math.max(1, getSpeed()), tl = Math.min(26, getAge() * l);
        float tx = getX() - getVx() / l * tl, ty = getY() - getVy() / l * tl;
        g.setColor(Util.alpha(GLOW, 130)); g.setStroke(new BasicStroke(6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Util.line(g, tx, ty, getX(), getY());
        g.setColor(CORE); g.setStroke(new BasicStroke(2f));
        Util.line(g, tx, ty, getX(), getY());
    }

    @Override public float getLightX() { return getX(); }
    @Override public float getLightY() { return getY(); }
    @Override public float getLightRadius() { return 60; }
    @Override public float getLightIntensity(float t) { return 0.7f; }
    @Override public Color getGlowColor() { return GLOW; }
    @Override public int getGlowAlpha() { return 40; }
}
