package deadsignal.entity.ability;

import deadsignal.entity.Entity;
import deadsignal.game.GameWorld;

/**
 * ABSTRACT telegraphed boss ability placed on the ground.
 * Life-cycle (template method): wind-up (warning shown) -> trigger -> active period -> end.
 * Subclasses (BloodRite, ThumperPack, FaultLine) override only the hooks they need.
 */
public abstract class GroundEffect extends Entity {
    private final float windup, duration;
    private float timer;
    private boolean triggered;

    protected GroundEffect(float x, float y, float radius, float windup, float duration) {
        super(x, y, radius);
        this.windup = windup;
        this.duration = duration;
    }

    @Override
    public final void update(GameWorld w, float dt) {
        timer += dt;
        if (!triggered) {
            if (timer < windup) { whileWindingUp(w, dt); return; }
            triggered = true;
            onTrigger(w);
        }
        whileActive(w, dt);
        if (timer >= windup + duration) { onEnd(w); remove(); }
    }

    /** The moment the warning ends and the ability actually hits. */
    protected abstract void onTrigger(GameWorld w);
    protected void whileWindingUp(GameWorld w, float dt) { }
    protected void whileActive(GameWorld w, float dt) { }
    protected void onEnd(GameWorld w) { }

    protected final boolean isTriggered() { return triggered; }
    protected final float windupProgress() { return Math.min(1, timer / windup); }
    protected final float activeTime() { return Math.max(0, timer - windup); }
    protected final float activeFraction() { return duration <= 0 ? 1 : Math.min(1, activeTime() / duration); }
}
