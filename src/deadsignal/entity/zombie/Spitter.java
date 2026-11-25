package deadsignal.entity.zombie;

import deadsignal.core.Util;
import deadsignal.entity.Player;
import deadsignal.entity.projectile.AcidGlob;
import deadsignal.game.GameWorld;
import deadsignal.gfx.ZombieSprite;

/** Keeps its distance and lobs acid that leaves burning puddles. */
public final class Spitter extends Zombie {
    private float spitCooldown = Util.rf(1, 3);

    public Spitter(float x, float y, float hpMul, float speedMul) {
        super(ZombieSprite.SPITTER, x, y, 13, 50 * hpMul, 58 * speedMul, 10, 12);
    }

    @Override
    protected float behave(GameWorld w, float dt, float dist, float speed, float[] dir) {
        spitCooldown -= dt;
        Player p = w.getPlayer();
        if (!hasLineOfSight() || dist > 330 || p.isDead()) return speed;
        if (dist < 170) { dir[0] = -dir[0]; dir[1] = -dir[1]; speed *= 0.8f; } else speed *= 0.15f;
        if (spitCooldown <= 0) {
            spitCooldown = 2.4f + Util.rf();
            float a = angleTo(p) + Util.gauss() * 0.06f;
            w.addProjectile(new AcidGlob(getX(), getY(), a, 270, 12, 6, Math.min(1.6f, dist / 270 + 0.35f)));
            w.audio().play("splat", 0.4f, 1.4f);
        }
        return speed;
    }
}
