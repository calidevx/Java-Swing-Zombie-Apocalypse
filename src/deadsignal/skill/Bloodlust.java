package deadsignal.skill;

import deadsignal.core.Util;
import deadsignal.entity.Player;
import deadsignal.game.GameWorld;
import deadsignal.gfx.ParticleType;

import java.awt.Color;

/** PASSIVE: lifesteal while the kill combo is x50 or higher. */
public final class Bloodlust extends Skill {
    public static final int COMBO_THRESHOLD = 50;

    public Bloodlust(int level) { super(SkillType.BLOODLUST, level); }

    @Override public float getCooldown() { return 0; }

    /** 6% of damage dealt at level 1, +3% per extra level. */
    public float getLifesteal() { return 0.06f + 0.03f * (getLevel() - 1); }

    @Override public boolean isActiveNow(GameWorld w) { return w.combo().getCount() >= COMBO_THRESHOLD; }

    @Override public void onDamageDealt(GameWorld w, Player p, float amount) {
        if (!isActiveNow(w) || p.getHealth() >= p.getMaxHealth()) return;
        p.heal(amount * getLifesteal());
        if (Util.rf() < 0.25f)
            w.particles().spawn(ParticleType.GLOW, p.getX() + Util.rf(-12, 12), p.getY() + Util.rf(-12, 12), 0, -50, 0.6f, 2.5f, new Color(255, 60, 80));
    }
}
