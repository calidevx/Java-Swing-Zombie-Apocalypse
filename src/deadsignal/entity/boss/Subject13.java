package deadsignal.entity.boss;

import deadsignal.core.Util;
import deadsignal.entity.DamageType;
import deadsignal.entity.Player;
import deadsignal.entity.ability.FaultLine;
import deadsignal.entity.projectile.EnergyOrb;
import deadsignal.game.GameWorld;
import deadsignal.gfx.BossSprite;
import deadsignal.gfx.ParticleType;

import java.awt.*;

/** Blinks and dashes through the player, and rips the floor open with FAULT LINE (stun + rock wall). */
public final class Subject13 extends Boss {
    private enum State { HUNT, FADE_OUT, FADE_IN, DASH, FAULT_WINDUP }
    private State state = State.HUNT;
    private float stateTimer, blinkCooldown = 3.5f, clawCooldown, clawDelay, dashX, dashY, faultCooldown = 2f;

    public Subject13(float x, float y, float hpMul) {
        super("SUBJECT 13", "BioGen's perfect soldier", BossSprite.SUBJECT13, x, y, 22, 2200 * hpMul, 15);
    }

    @Override
    protected void think(GameWorld w, float dt, float dx, float dy, float d) {
        stateTimer -= dt; blinkCooldown -= dt; clawCooldown -= dt; faultCooldown -= dt;
        float frac = getHealthFraction();
        Player p = w.getPlayer();
        switch (state) {
            case HUNT:
                chase(w, 165, dt, dx, dy, d);
                setAlpha(1);
                if (d < getRadius() + 28 && clawCooldown <= 0) { clawCooldown = 0.7f; attackPose(0.3f); clawDelay = 0.12f; }
                if (clawDelay > 0) { clawDelay -= dt; if (clawDelay <= 0 && d < getRadius() + 38) { p.hurt(15, getX(), getY(), DamageType.MELEE, w); w.audio().play("hit", 1f, 1.6f); } }
                if (faultCooldown <= 0 && d < 460 && canSeePlayer()) {
                    state = State.FAULT_WINDUP; stateTimer = 0.6f; attackPose(0.6f);
                    announce(w, "FAULT LINE", new Color(255, 150, 60));
                    break;
                }
                if (blinkCooldown <= 0 && d < 600) { state = State.FADE_OUT; stateTimer = 0.35f; w.audio().play("dash", 1f, 0.7f); }
                break;
            case FADE_OUT:
                setAlpha(Math.max(0, stateTimer / 0.35f));
                if (stateTimer <= 0) {
                    for (int t = 0; t < 16; t++) {
                        float a = Util.rf(0, 6.28f), nx = p.getX() + (float) Math.cos(a) * 180, ny = p.getY() + (float) Math.sin(a) * 180;
                        if (!w.getMap().collides(nx, ny, getCollisionRadius() + 2) && w.getMap().isReachableNow(nx, ny) && w.getMap().hasLineOfSight(nx, ny, p.getX(), p.getY())) {
                            teleport(nx, ny); break;
                        }
                    }
                    for (int i = 0; i < 14; i++) w.particles().spawn(ParticleType.GLOW, getX() + Util.rf(-20, 20), getY() + Util.rf(-20, 20), Util.rf(-60, 60), Util.rf(-60, 60), 0.4f, 3, new Color(100, 230, 255));
                    state = State.FADE_IN; stateTimer = 0.32f;
                }
                break;
            case FADE_IN:
                setAlpha(1 - Math.max(0, stateTimer / 0.32f));
                float face = angleTo(p);
                setAngle(face); dashX = (float) Math.cos(face); dashY = (float) Math.sin(face);
                if (stateTimer <= 0) {
                    state = State.DASH; stateTimer = 0.34f; attackPose(0.34f);
                    if (frac < 0.5f) for (int k = 0; k < 10; k++) w.addProjectile(new EnergyOrb(getX(), getY(), k / 10f * 6.28f, 300, 10, 5, new Color(110, 230, 255)));
                }
                break;
            case FAULT_WINDUP:
                faceToward(angleTo(p), 6 * dt);
                if (stateTimer <= 0) {
                    w.addEffect(new FaultLine(getX(), getY(), getAngle()));
                    state = State.HUNT;
                    faultCooldown = frac < 0.5f ? 5.5f : 7.5f;
                }
                break;
            case DASH:
                int blocked = moveBody(w, dashX * 680 * dt, dashY * 680 * dt);
                leaveAfterimage();
                advanceAnim(dt * 20);
                if (blocked != 0 || stateTimer <= 0) { state = State.HUNT; blinkCooldown = frac < 0.5f ? 2.3f : 3.3f; }
                break;
        }
    }

    @Override protected void renderTelegraph(Graphics2D g) {
        if (state != State.FADE_IN) return;
        g.setColor(new Color(100, 220, 255, 90)); g.setStroke(new BasicStroke(16f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Util.line(g, getX(), getY(), getX() + dashX * 240, getY() + dashY * 240);
    }
}
