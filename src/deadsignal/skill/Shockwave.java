package deadsignal.skill;

import deadsignal.core.Util;
import deadsignal.entity.DamageType;
import deadsignal.entity.Damageable;
import deadsignal.entity.Player;
import deadsignal.entity.boss.Boss;
import deadsignal.entity.zombie.Zombie;
import deadsignal.game.GameWorld;
import deadsignal.gfx.ParticleType;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/** ACTIVE [F]: a burst that stuns every enemy around the player. 30 s cooldown. */
public final class Shockwave extends Skill {
    public Shockwave(int level) { super(SkillType.SHOCKWAVE, level); }

    @Override public float getCooldown() { return 30f - 2f * (getLevel() - 1); }
    public float getRadius() { return 210 + 20 * (getLevel() - 1); }
    public float getStunTime() { return 2.5f + 0.25f * (getLevel() - 1); }
    public float getDamage() { return 40 + 15 * (getLevel() - 1); }

    @Override
    protected boolean activate(GameWorld w, Player p) {
        float r = getRadius();
        List<Damageable> targets = new ArrayList<>(w.getDamageables());
        for (Damageable d : targets) {
            if (!d.isTargetable() || Util.dist(p.getX(), p.getY(), d.getX(), d.getY()) > r + d.getRadius()) continue;
            if (d instanceof Zombie) ((Zombie) d).stun(getStunTime());
            if (d instanceof Boss) ((Boss) d).stagger(0.9f);
            d.takeDamage(d instanceof Boss ? getDamage() * 2 : getDamage(), DamageType.ENERGY, w);
        }
        w.particles().spawn(ParticleType.RING, p.getX(), p.getY(), 0, 0, 0.45f, r, new Color(140, 220, 255));
        w.particles().spawn(ParticleType.RING, p.getX(), p.getY(), 0, 0, 0.3f, r * 0.6f, new Color(255, 250, 200));
        for (int i = 0; i < 36; i++) {
            float a = i / 36f * 6.283f;
            w.particles().spawn(ParticleType.SPARK, p.getX(), p.getY(), (float) Math.cos(a) * r * 2.2f, (float) Math.sin(a) * r * 2.2f, 0.35f, 2f, new Color(170, 230, 255));
        }
        w.addFlash(p.getX(), p.getY(), r * 1.6f, 0.35f, new Color(120, 200, 255));
        w.shake(14);
        w.audio().play("explode", 0.8f, 1.4f);
        w.audio().play("zap", 1f, 0.7f);
        w.addText("SHOCKWAVE!", p.getX(), p.getY() - 34, new Color(150, 220, 255), 18);
        return true;
    }
}
