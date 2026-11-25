package deadsignal.entity.boss;

import deadsignal.core.Util;
import deadsignal.entity.DamageType;
import deadsignal.entity.EnemyGrenade;
import deadsignal.entity.Player;
import deadsignal.entity.ability.ToxinSentry;
import deadsignal.entity.projectile.EnergyOrb;
import deadsignal.entity.zombie.ZombieType;
import deadsignal.game.GameWorld;
import deadsignal.gfx.BossSprite;
import deadsignal.gfx.ParticleType;
import deadsignal.story.DialogueLine;
import deadsignal.story.Speaker;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Chapter 5 boss: the Abomination. Minigun sweeps, grenades, sentries - and a RAGE meter.
 * Rage fills as he takes damage (and slowly on its own). When it is full he ENRAGES: faster, harder hits,
 * a faster minigun, and repeated LEAPS that crash down on the player, stunning them for 2 seconds.
 */
public final class Graves extends Boss {
    private enum State { MANEUVER, SPIN_UP, FIRING, LEAP_WINDUP, LEAP_AIR }
    private static final float ENRAGE_TIME = 9f, LEAP_RADIUS = 120, PLAYER_STUN = 2f;
    private State state = State.MANEUVER;
    private float stateTimer, gunCooldown = 2.5f, grenadeCooldown = 3f, sentryCooldown = 1.5f, fireAccumulator, strafeDir = 1;
    private float rage, enrageTimer, leapCooldown, leapFromX, leapFromY, leapToX, leapToY, rageFlicker;
    private int enrageCount;
    private boolean squadCalled, reinforcementsCalled;

    public Graves(float x, float y, float hpMul) {
        super("THE ABOMINATION", "Colonel Graves, Meridian Defense Command", BossSprite.GRAVES, x, y, 36, 2700 * hpMul, 15);
    }

    public boolean isEnraged() { return enrageTimer > 0; }
    public float getRage() { return rage; }
    private float speedMul() { return isEnraged() ? 1.6f : 1f; }
    private float hitMul() { return isEnraged() ? 1.5f : 1f; }

    @Override
    protected void think(GameWorld w, float dt, float dx, float dy, float d) {
        stateTimer -= dt; gunCooldown -= dt; grenadeCooldown -= dt; sentryCooldown -= dt; leapCooldown -= dt; rageFlicker += dt;
        float frac = getHealthFraction();
        updateRage(w, dt);
        if (state == State.LEAP_WINDUP) { updateLeapWindup(w); return; }
        if (state == State.LEAP_AIR) { updateLeapAir(w); return; }
        faceToward((float) Math.atan2(dy, dx), 4 * speedMul() * dt);
        if (state == State.MANEUVER) {
            float sm = speedMul();
            if (isEnraged() || d > 270) chase(w, 78 * sm, dt, dx, dy, d);
            else if (d < 170) { moveBody(w, -dx / d * 62 * dt, -dy / d * 62 * dt); advanceAnim(dt * 6); }
            else {
                if (Util.rf() < dt * 0.5f) strafeDir = -strafeDir;
                if (moveBody(w, -dy / d * 55 * strafeDir * dt, dx / d * 55 * strafeDir * dt) != 0) strafeDir = -strafeDir;
                advanceAnim(dt * 5);
            }
            if (isEnraged() && leapCooldown <= 0 && d < 620) { startLeap(w); return; }
            if (gunCooldown <= 0 && canSeePlayer()) { state = State.SPIN_UP; stateTimer = isEnraged() ? 0.3f : 0.55f; attackPose(0.55f); w.audio().play("reload", 1f, 0.6f); }
            if (grenadeCooldown <= 0) lobGrenades(w, frac);
            if (sentryCooldown <= 0 && w.structureCount() < 2 && !isEnraged()) deploySentries(w, frac);
        } else if (state == State.SPIN_UP) {
            if (stateTimer <= 0) { state = State.FIRING; stateTimer = isEnraged() ? 1.3f : 1.6f; fireAccumulator = 0; }
        } else {
            attackPose(0.1f);
            fireAccumulator += dt;
            float interval = isEnraged() ? 0.045f : 0.07f;
            while (fireAccumulator > interval) {
                fireAccumulator -= interval;
                float a = getAngle() + (float) Math.sin(stateTimer * 5) * 0.4f + Util.gauss() * 0.04f;
                float gx = getX() + (float) Math.cos(getAngle()) * 58 - (float) Math.sin(getAngle()) * 30;
                float gy = getY() + (float) Math.sin(getAngle()) * 58 + (float) Math.cos(getAngle()) * 30;
                w.addProjectile(new EnergyOrb(gx, gy, a, 560, 8 * hitMul(), 4, isEnraged() ? new Color(255, 90, 60) : new Color(255, 210, 90)));
                w.addFlash(gx, gy, 90, 0.05f, new Color(255, 200, 100));
            }
            w.audio().play("smg", 0.6f, 0.8f);
            if (stateTimer <= 0) { state = State.MANEUVER; gunCooldown = isEnraged() ? 1.6f : 2.8f; }
        }
        if (!squadCalled && frac < 0.66f) {
            squadCalled = true; summon(w, 5, ZombieType.SOLDIER);
            w.radio().queue(new DialogueLine(Speaker.GRAVES, "Squad! To me! Protect your Colonel!"));
        }
        if (!reinforcementsCalled && frac < 0.33f) {
            reinforcementsCalled = true; summon(w, 6, ZombieType.RUNNER);
            w.radio().queue(new DialogueLine(Speaker.GRAVES, "The Shepherd will rebuild everything you destroy!"));
        }
    }

    // ------------------------------------------------------------------ rage
    private void updateRage(GameWorld w, float dt) {
        if (isEnraged()) {
            enrageTimer -= dt;
            if (Util.rf() < dt * 30) w.particles().spawn(ParticleType.SPARK, getX() + Util.rf(-30, 30), getY() + Util.rf(-30, 30), Util.rf(-30, 30), Util.rf(-90, -30), 0.5f, 2.5f, new Color(255, Util.ri(40, 120), 30));
            if (enrageTimer <= 0) { rage = 0; w.addText("RAGE SPENT", getX(), getY() - 60, new Color(255, 180, 120), 15); }
            return;
        }
        rage += dt * (getHealthFraction() < 0.5f ? 0.055f : 0.035f);        // fills on its own, faster when hurt
        if (rage >= 1) enrage(w);
    }

    @Override
    protected void onHurt(float dmg, DamageType type, GameWorld w) {
        super.onHurt(dmg, type, w);
        if (!isEnraged()) rage = Math.min(1, rage + dmg / (getMaxHealth() * 0.10f));   // 10% of his HP in damage fills the bar
    }

    private void enrage(GameWorld w) {
        rage = 1; enrageTimer = ENRAGE_TIME; leapCooldown = 0.4f; enrageCount++;
        announce(w, "ENRAGED!", new Color(255, 60, 40));
        w.addText("ENRAGED!", getX(), getY() - 70, new Color(255, 70, 40), 24);
        w.audio().play("roar", 1f, 0.6f);
        w.shake(16);
        w.addFlash(getX(), getY(), 300, 0.4f, new Color(255, 60, 30));
        if (enrageCount == 1) w.radio().queue(new DialogueLine(Speaker.GRAVES, "YOU WANT TO SEE WHAT THEY MADE OF ME?!"));
        if (state == State.SPIN_UP || state == State.FIRING) state = State.MANEUVER;
    }

    // ------------------------------------------------------------------ leap
    private void startLeap(GameWorld w) {
        Player p = w.getPlayer();
        state = State.LEAP_WINDUP; stateTimer = 0.65f;
        leapFromX = getX(); leapFromY = getY();
        leapToX = p.getX(); leapToY = p.getY();
        // never land inside a wall: walk the target back toward Graves until it is clear
        for (int k = 0; k < 12 && w.getMap().collides(leapToX, leapToY, getCollisionRadius()); k++) {
            leapToX += (leapFromX - leapToX) * 0.15f; leapToY += (leapFromY - leapToY) * 0.15f;
        }
        attackPose(0.65f);
        announce(w, "CRUSHING LEAP", new Color(255, 90, 50));
        w.audio().play("roar", 0.7f, 1.2f);
    }

    private void updateLeapWindup(GameWorld w) {
        faceToward((float) Math.atan2(leapToY - getY(), leapToX - getX()), 0.4f);
        if (stateTimer <= 0) { state = State.LEAP_AIR; stateTimer = 0.55f; w.audio().play("throw", 1f, 0.5f); }
    }

    private void updateLeapAir(GameWorld w) {
        float t = 1 - Math.max(0, stateTimer) / 0.55f;
        float e = t * t * (3 - 2 * t);
        teleport(leapFromX + (leapToX - leapFromX) * e, leapFromY + (leapToY - leapFromY) * e);
        if (Util.rf() < 0.5f) leaveAfterimage();
        if (stateTimer <= 0) land(w);
    }

    private void land(GameWorld w) {
        teleport(leapToX, leapToY);
        Player p = w.getPlayer();
        if (Util.dist(p.getX(), p.getY(), getX(), getY()) < LEAP_RADIUS + p.getRadius()) {
            p.hurt(20 * hitMul(), getX(), getY(), DamageType.MELEE, w);
            p.stun(PLAYER_STUN);
            if (p.isStunned()) w.addText("STUNNED!", p.getX(), p.getY() - 30, new Color(255, 220, 80), 18);
        }
        w.particles().spawn(ParticleType.RING, getX(), getY(), 0, 0, 0.45f, LEAP_RADIUS, new Color(255, 120, 60));
        for (int i = 0; i < 30; i++) {
            float a = i / 30f * 6.283f;
            w.particles().spawn(ParticleType.SMOKE, getX(), getY(), (float) Math.cos(a) * 220, (float) Math.sin(a) * 220, 0.6f, Util.rf(6, 12), new Color(110, 95, 80));
        }
        w.addFlash(getX(), getY(), 220, 0.25f, new Color(255, 140, 60));
        w.shake(22);
        w.audio().play("explode", 1f, 0.6f);
        state = State.MANEUVER;
        leapCooldown = 2.4f;
        gunCooldown = Math.max(gunCooldown, 0.6f);
    }

    @Override public boolean isTargetable() { return super.isTargetable() && state != State.LEAP_AIR; }
    @Override protected float getContactDamage() { return super.getContactDamage() * hitMul(); }

    // ------------------------------------------------------------------ rendering hooks
    @Override
    protected void renderTelegraph(Graphics2D g) {
        if (state != State.LEAP_WINDUP && state != State.LEAP_AIR) return;
        float pulse = 0.5f + 0.5f * (float) Math.sin(rageFlicker * 18);
        g.setColor(new Color(255, 40, 20, 50 + (int) (40 * pulse))); Util.oval(g, leapToX, leapToY, LEAP_RADIUS, LEAP_RADIUS);
        g.setColor(new Color(255, 90, 40, 220)); g.setStroke(new BasicStroke(3f)); Util.ring(g, leapToX, leapToY, LEAP_RADIUS, LEAP_RADIUS);
        float prog = state == State.LEAP_AIR ? 1 - Math.max(0, stateTimer) / 0.55f : 0;
        g.setStroke(new BasicStroke(2f)); Util.ring(g, leapToX, leapToY, LEAP_RADIUS * prog, LEAP_RADIUS * prog);
    }

    @Override
    protected void renderAura(Graphics2D g) {
        super.renderAura(g);
        if (!isEnraged()) return;
        float r = getRadius() * (1.7f + 0.15f * (float) Math.sin(rageFlicker * 12));
        g.setPaint(new RadialGradientPaint(getX(), getY(), r, new float[]{0, 0.6f, 1}, new Color[]{new Color(255, 60, 20, 150), new Color(200, 20, 10, 70), new Color(120, 0, 0, 0)}));
        Util.oval(g, getX(), getY(), r, r);
    }

    @Override protected float bodyScale() {
        if (state == State.LEAP_AIR) { float t = 1 - Math.max(0, stateTimer) / 0.55f; return 1f + 0.4f * (float) Math.sin(t * Math.PI); }
        return isEnraged() ? 1.08f : 1f;
    }

    /** POLYMORPHISM: Graves shows his health and a RAGE meter under it. */
    @Override
    public List<BossBar> getHudBars() {
        List<BossBar> bars = new ArrayList<>(super.getHudBars());
        if (isEnraged()) bars.add(new BossBar("ENRAGED " + (int) Math.ceil(enrageTimer) + "s", enrageTimer / ENRAGE_TIME, new Color(255, 70, 30), new Color(150, 10, 0)));
        else bars.add(new BossBar("RAGE", rage, new Color(255, 170, 50), new Color(170, 70, 10)));
        return bars;
    }

    // ------------------------------------------------------------------ other attacks
    private void deploySentries(GameWorld w, float frac) {
        sentryCooldown = 11f;
        int count = frac < 0.5f ? 2 : 1;
        for (int k = 0; k < count; k++) for (int t = 0; t < 10; t++) {
            float a = Util.rf(0, 6.28f), dd = Util.rf(70, 150), x = getX() + (float) Math.cos(a) * dd, y = getY() + (float) Math.sin(a) * dd;
            if (!w.getMap().collides(x, y, 16) && w.getMap().isReachableNow(x, y)) { w.addStructure(new ToxinSentry(x, y, w.getDifficulty().getEnemyHealth())); break; }
        }
        announce(w, "TOXIN SENTRY", new Color(150, 240, 80));
        w.audio().play("reload", 1f, 0.5f);
    }

    private void lobGrenades(GameWorld w, float frac) {
        grenadeCooldown = frac < 0.5f ? 4f : 5.5f;
        Player p = w.getPlayer();
        for (int k = 0; k < 3; k++) {
            float tx = p.getX() + Util.rf(-70, 70), ty = p.getY() + Util.rf(-70, 70);
            if (w.getMap().isSolidAt(tx, ty)) { tx = p.getX(); ty = p.getY(); }
            w.addGrenade(new EnemyGrenade(getX(), getY(), tx, ty, 0.7f + k * 0.12f));
        }
        w.audio().play("throw", 1f, 0.8f);
    }
}
