package deadsignal.entity.zombie;

import deadsignal.core.Util;
import deadsignal.entity.DamageType;
import deadsignal.entity.Faction;
import deadsignal.game.GameWorld;
import deadsignal.gfx.ParticleType;
import deadsignal.gfx.ZombieSprite;

import java.awt.*;

/** A bloated walking bomb. Lights a fuse when close and explodes on death - hurting other zombies too. */
public final class Exploder extends Zombie {
    private float fuse = -1;

    public Exploder(float x, float y, float hpMul, float speedMul) {
        super(ZombieSprite.EXPLODER, x, y, 15, 60 * hpMul, 68 * speedMul, 0, 12);
    }

    @Override protected boolean canMelee() { return false; }

    @Override
    protected float behave(GameWorld w, float dt, float dist, float speed, float[] dir) {
        if (fuse < 0 && dist < 48 && !w.getPlayer().isDead()) { fuse = 0.55f; w.audio().play("zap", 0.5f, 0.6f); }
        if (fuse >= 0) {
            fuse -= dt;
            if (fuse <= 0) { kill(w, DamageType.EXPLOSION); return 0; }
            return speed * 0.3f;
        }
        return speed;
    }

    @Override protected void onDeath(GameWorld w, DamageType type) {
        super.onDeath(w, type);
        for (int i = 0; i < 12; i++) w.particles().spawn(ParticleType.DOT, getX(), getY(), Util.rf(-200, 200), Util.rf(-200, 200), 0.6f, 3, new Color(230, 170, 50));
        w.explode(getX(), getY(), 95, 46, Faction.INFECTED);
    }

    @Override protected void renderOverlay(Graphics2D g) {
        if (fuse < 0) return;
        g.setColor(new Color(255, 60, 30, ((int) (fuse * 20)) % 2 == 0 ? 200 : 80));
        g.setStroke(new BasicStroke(2.5f));
        Util.ring(g, getX(), getY(), 30, 30);
    }
}
