package deadsignal.entity.boss;

import deadsignal.core.Util;
import deadsignal.entity.DamageType;
import deadsignal.entity.Player;
import deadsignal.game.GameWorld;
import deadsignal.gfx.BossSprite;
import deadsignal.gfx.ParticleType;

import java.awt.*;

/**
 * Chapter 1 boss: the infected hospital chaplain.
 * MIASMA - a toggled cloud of toxic censer smoke that burns and slows the player up close.
 * He also charges in straight lines and stuns himself if he slams into a wall.
 */
public final class Censer extends Boss {
    private enum State { CHASE, WINDUP, CHARGE, STUNNED }
    private static final float MIASMA_RADIUS = 125, MIASMA_DPS = 16;
    private static final Color MIASMA_COLOR = new Color(140, 230, 90);

    private State state = State.CHASE;
    private float stateTimer, chargeCooldown = 3f, swingCooldown, swingDelay, chargeX, chargeY;
    private boolean miasmaOn;
    private float miasmaTimer = 1.5f, miasmaTick, clock;

    public Censer(float x, float y, float hpMul) {
        super("THE CENSER", "Father Aldous, hospital chaplain", BossSprite.CENSER, x, y, 30, 1300 * hpMul, 15);
    }

    @Override protected float damageMultiplier() { return state == State.STUNNED ? 1.6f : 1f; }

    @Override
    protected void think(GameWorld w, float dt, float dx, float dy, float d) {
        clock += dt; stateTimer -= dt; chargeCooldown -= dt; swingCooldown -= dt;
        updateMiasma(w, dt);
        float frac = getHealthFraction();
        Player p = w.getPlayer();
        switch (state) {
            case CHASE:
                chase(w, frac < 0.5f ? 92 : 74, dt, dx, dy, d);
                if (d < getRadius() + 36 && swingCooldown <= 0) { swingCooldown = 1.1f; attackPose(0.45f); swingDelay = 0.22f; }
                if (swingDelay > 0) {
                    swingDelay -= dt;
                    if (swingDelay <= 0) {
                        w.audio().play("throw", 0.8f, 0.6f);
                        for (int i = 0; i < 8; i++) w.particles().spawn(ParticleType.SMOKE, getX() + (float) Math.cos(getAngle()) * 40, getY() + (float) Math.sin(getAngle()) * 40, Util.rf(-90, 90), Util.rf(-90, 90), 0.6f, 8, new Color(120, 200, 80, 140));
                        if (d < getRadius() + 46) p.hurt(22, getX(), getY(), DamageType.MELEE, w);
                    }
                }
                if (chargeCooldown <= 0 && canSeePlayer() && d < 520 && d > 110) { state = State.WINDUP; stateTimer = 0.75f; w.audio().play("roar", 0.6f, 1.3f); }
                break;
            case WINDUP:
                attackPose(0.1f);
                faceToward((float) Math.atan2(dy, dx), 2.5f * dt);
                chargeX = (float) Math.cos(getAngle()); chargeY = (float) Math.sin(getAngle());
                if (stateTimer <= 0) { state = State.CHARGE; stateTimer = 0.9f; }
                break;
            case CHARGE: {
                int blocked = moveBody(w, chargeX * 520 * dt, chargeY * 520 * dt);
                advanceAnim(dt * 18);
                if (d < getRadius() + p.getRadius() + 4) p.hurt(28, getX(), getY(), DamageType.MELEE, w);
                if (blocked != 0) {
                    state = State.STUNNED; stateTimer = 1.7f; w.shake(15); w.audio().play("explode", 0.6f, 0.8f);
                    for (int i = 0; i < 14; i++) w.particles().spawn(ParticleType.CHUNK, getX() + chargeX * 30, getY() + chargeY * 30, Util.rf(-160, 160), Util.rf(-160, 160), 0.7f, 3, new Color(120, 110, 100));
                    w.addText("STUNNED!", getX(), getY() - 46, new Color(255, 230, 90), 18);
                } else if (stateTimer <= 0) { state = State.CHASE; chargeCooldown = frac < 0.5f ? 2.8f : 3.8f; }
                break;
            }
            case STUNNED:
                if (stateTimer <= 0) { state = State.CHASE; chargeCooldown = 2.5f; }
                break;
        }
    }

    /** Toggles the toxic cloud on/off and applies damage + slow to a player standing inside it. */
    private void updateMiasma(GameWorld w, float dt) {
        miasmaTimer -= dt;
        if (miasmaTimer <= 0) {
            miasmaOn = !miasmaOn;
            miasmaTimer = miasmaOn ? 5.5f : (state == State.STUNNED ? 1.5f : 2.5f);
            if (miasmaOn) { announce(w, "MIASMA", MIASMA_COLOR); w.audio().play("flame", 0.6f, 0.5f); }
        }
        if (!miasmaOn) return;
        if (Util.rf() < dt * 30) {
            float a = Util.rf(0, 6.28f), r = Util.rf(20, MIASMA_RADIUS);
            w.particles().spawn(ParticleType.SMOKE, getX() + (float) Math.cos(a) * r, getY() + (float) Math.sin(a) * r, Util.rf(-15, 15), Util.rf(-25, 5), 1.1f, Util.rf(8, 14), new Color(110, 190, 70, 110));
        }
        miasmaTick -= dt;
        Player p = w.getPlayer();
        if (miasmaTick <= 0 && !p.isDead() && distanceTo(p) < MIASMA_RADIUS) {
            miasmaTick = 0.25f;
            p.takeDamage(MIASMA_DPS * 0.25f, DamageType.ACID, w);
            p.applySlow(0.7f, 0.35f);
        }
    }

    @Override protected void renderTelegraph(Graphics2D g) {
        if (miasmaOn) {
            float pulse = 1 + 0.04f * (float) Math.sin(clock * 5);
            g.setPaint(new RadialGradientPaint(getX(), getY(), MIASMA_RADIUS * pulse, new float[]{0, 0.7f, 1},
                    new Color[]{new Color(120, 220, 80, 90), new Color(90, 180, 60, 60), new Color(60, 140, 40, 0)}));
            Util.oval(g, getX(), getY(), MIASMA_RADIUS * pulse, MIASMA_RADIUS * pulse);
            g.setColor(new Color(160, 240, 110, 110));
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{14, 10}, clock * 30));
            Util.ring(g, getX(), getY(), MIASMA_RADIUS, MIASMA_RADIUS);
        }
        if (state != State.WINDUP) return;
        g.setColor(new Color(255, 40, 30, 90));
        g.setStroke(new BasicStroke(getRadius() * 1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Util.line(g, getX(), getY(), getX() + chargeX * 470, getY() + chargeY * 470);
    }

    @Override protected void renderOverlay(Graphics2D g) {
        if (state != State.STUNNED) return;
        for (int k = 0; k < 3; k++) {
            float a = clock * 4 + k * 2.09f;
            Util.drawStar(g, getX() + (float) Math.cos(a) * 18, getY() - 38 + (float) Math.sin(a) * 6, 5, new Color(255, 240, 120));
        }
    }
}
