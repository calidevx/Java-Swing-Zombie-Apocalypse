package deadsignal.skill;

import deadsignal.entity.Player;
import deadsignal.game.GameWorld;

/**
 * ABSTRACT special skill bought in the shop after the Bonus Stage.
 * Cooldown handling is encapsulated here; each subclass decides what the skill does by
 * overriding {@link #activate} (active skills) or {@link #onDamageDealt} (passive skills).
 */
public abstract class Skill {
    private final SkillType type;
    private final int level;
    private float cooldownLeft;

    protected Skill(SkillType type, int level) { this.type = type; this.level = level; }

    /** Seconds between uses; 0 means this is a passive skill. */
    public abstract float getCooldown();

    /** Hook for active skills. @return true if the skill actually fired (starts the cooldown). */
    protected boolean activate(GameWorld w, Player p) { return false; }
    /** Hook for passive skills that react to the player's hits. */
    public void onDamageDealt(GameWorld w, Player p, float amount) { }
    /** Whether a passive effect is currently running (used by the HUD). */
    public boolean isActiveNow(GameWorld w) { return false; }

    /** Template method: checks cooldown, fires, starts cooldown. */
    public final boolean tryActivate(GameWorld w, Player p) {
        if (isPassive() || cooldownLeft > 0 || p.isDead()) return false;
        if (!activate(w, p)) return false;
        cooldownLeft = getCooldown();
        return true;
    }

    public void update(float dt) { if (cooldownLeft > 0) cooldownLeft -= dt; }

    public boolean isPassive() { return getCooldown() <= 0; }
    public boolean isReady() { return cooldownLeft <= 0; }
    public float getCooldownLeft() { return Math.max(0, cooldownLeft); }
    public float getCooldownFraction() { return isPassive() ? 0 : getCooldownLeft() / getCooldown(); }
    public SkillType getType() { return type; }
    public int getLevel() { return level; }
}
