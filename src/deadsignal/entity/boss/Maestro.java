package deadsignal.entity.boss;

import deadsignal.core.Util;
import deadsignal.entity.pickup.CashPickup;
import deadsignal.entity.pickup.GrenadePickup;
import deadsignal.entity.pickup.HealthPickup;
import deadsignal.entity.projectile.MusicNote;
import deadsignal.entity.zombie.Zombie;
import deadsignal.entity.zombie.ZombieType;
import deadsignal.game.GameWorld;
import deadsignal.gfx.BossSprite;
import deadsignal.gfx.LightEmitter;
import deadsignal.gfx.ParticleType;
import deadsignal.progress.Medal;
import deadsignal.story.DialogueLine;
import deadsignal.story.Speaker;

import java.awt.*;

/**
 * BONUS STAGE BOSS - a zombie bandleader.
 * Signature skill DEAD MAN'S GROOVE: every zombie freezes into a synchronized dance (free shots!),
 * then the music drops into a CRESCENDO and they all lunge at double speed.
 * He backslides away from the player and conducts waves of music-note projectiles.
 */
public final class Maestro extends Boss implements LightEmitter {
    private enum Mode { CONDUCT, GROOVE, CRESCENDO }
    private static final float GROOVE_TIME = 4f, CRESCENDO_TIME = 3f;

    private Mode mode = Mode.CONDUCT;
    private float modeTimer, noteCooldown = 2f, grooveCooldown = 5f, clock, strafeDir = 1;
    private boolean encorePlayed;

    public Maestro(float x, float y, float hpMul) {
        super("THE MAESTRO", "Conductor of the dead  -  The Majestic Theater", BossSprite.MAESTRO, x, y, 28, 2600 * hpMul, 14);
    }

    @Override
    protected void think(GameWorld w, float dt, float dx, float dy, float d) {
        clock += dt; modeTimer -= dt; noteCooldown -= dt; grooveCooldown -= dt;
        float frac = getHealthFraction();
        faceToward((float) Math.atan2(dy, dx), 5 * dt);
        if (!encorePlayed && frac < 0.5f) {
            encorePlayed = true;
            summon(w, 5, ZombieType.RUNNER);
            w.radio().queue(new DialogueLine(Speaker.LENA, "He's calling in an encore! More of them are coming down the aisles!"));
            w.announcer().showBanner("ENCORE!", new Color(255, 120, 220));
        }
        switch (mode) {
            case CONDUCT:
            case CRESCENDO:
                move(w, dt, dx, dy, d);
                float rate = mode == Mode.CRESCENDO ? 2.2f : 1f;
                if (noteCooldown <= 0) {
                    fireNotes(w, frac);
                    noteCooldown = (frac < 0.5f ? 1.5f : 2f) / rate;
                }
                if (mode == Mode.CRESCENDO && modeTimer <= 0) { mode = Mode.CONDUCT; grooveCooldown = frac < 0.5f ? 9f : 12f; }
                if (mode == Mode.CONDUCT && grooveCooldown <= 0) startGroove(w);
                break;
            case GROOVE:
                attackPose(0.1f);
                advanceAnim(dt * 3);
                if (Util.rf() < dt * 6) w.particles().spawn(ParticleType.GLOW, getX() + Util.rf(-30, 30), getY() + Util.rf(-30, 30), 0, -40, 0.8f, 3, discoColor(clock * 3));
                if (modeTimer <= 0) startCrescendo(w);
                break;
        }
    }

    /** Keeps a stage-performer's distance: slides backwards when you get close, strafes otherwise. */
    private void move(GameWorld w, float dt, float dx, float dy, float d) {
        if (d > 300) { chase(w, 85, dt, dx, dy, d); return; }
        if (d < 190) { moveBody(w, -dx / d * 125 * dt, -dy / d * 125 * dt); advanceAnim(dt * 8); return; }
        if (Util.rf() < dt * 0.4f) strafeDir = -strafeDir;
        if (moveBody(w, -dy / d * 70 * strafeDir * dt, dx / d * 70 * strafeDir * dt) != 0) strafeDir = -strafeDir;
        advanceAnim(dt * 6);
    }

    private void fireNotes(GameWorld w, float frac) {
        int n = frac < 0.5f ? 7 : 5;
        float base = getAngle();
        for (int k = 0; k < n; k++) {
            float a = base + (k - (n - 1) / 2f) * 0.22f;
            w.addProjectile(new MusicNote(getX() + (float) Math.cos(a) * 30, getY() + (float) Math.sin(a) * 30, a, discoColor(k * 0.7f + clock)));
        }
        attackPose(0.35f);
        w.audio().play("zap", 0.5f, 1.8f);
    }

    private void startGroove(GameWorld w) {
        mode = Mode.GROOVE;
        modeTimer = GROOVE_TIME;
        if (w.zombieCount() < 10) summon(w, 6, ZombieType.WALKER);
        for (Zombie z : w.getZombies()) if (z.distanceTo(this) < 950) z.dance(GROOVE_TIME);
        announce(w, "DEAD MAN'S GROOVE", new Color(255, 140, 230));
        w.whiteFlash(0.15f);
    }

    private void startCrescendo(GameWorld w) {
        mode = Mode.CRESCENDO;
        modeTimer = CRESCENDO_TIME;
        for (Zombie z : w.getZombies()) if (z.isDancing() || z.distanceTo(this) < 950) z.frenzy(CRESCENDO_TIME);
        announce(w, "CRESCENDO", new Color(255, 80, 80));
        w.shake(12);
        w.audio().play("roar", 0.9f, 1.1f);
    }

    /** POLYMORPHISM: the world asks every boss for its music; the Maestro switches tracks mid-fight. */
    @Override public String getMusicTrack() { return mode == Mode.GROOVE ? "groove" : "boss"; }

    /** He keeps conducting while the dance floor is full - and is harder to hurt while he does. */
    @Override protected float damageMultiplier() { return mode == Mode.GROOVE ? 0.5f : 1f; }

    @Override protected void onDefeated(GameWorld w) {
        for (int k = 0; k < 20; k++) w.addPickup(new CashPickup(getX() + Util.rf(-40, 40), getY() + Util.rf(-40, 40), 15));
        w.addPickup(new GrenadePickup(getX(), getY()));
        w.addPickup(new GrenadePickup(getX(), getY()));
        w.addPickup(new HealthPickup(getX(), getY()));
        w.award(Medal.ENCORE);
    }

    static Color discoColor(float t) {
        Color[] c = {new Color(255, 90, 200), new Color(90, 220, 255), new Color(255, 220, 80), new Color(150, 255, 120)};
        return c[((int) Math.abs(t)) % c.length];
    }

    @Override protected void renderTelegraph(Graphics2D g) {
        if (mode != Mode.GROOVE) return;
        float k = 1 - Math.max(0, modeTimer) / GROOVE_TIME;
        for (int i = 0; i < 4; i++) {
            double a = clock * 1.6 + i * Math.PI / 2;
            Color c = discoColor(i + clock);
            g.setPaint(new RadialGradientPaint((float) (getX() + Math.cos(a) * 110), (float) (getY() + Math.sin(a) * 110), 90,
                    new float[]{0, 1}, new Color[]{Util.alpha(c, 70), Util.alpha(c, 0)}));
            Util.oval(g, getX() + Math.cos(a) * 110, getY() + Math.sin(a) * 110, 90, 90);
        }
        g.setColor(new Color(255, 80, 80, (int) (60 + 150 * k)));
        g.setStroke(new BasicStroke(3f));
        Util.ring(g, getX(), getY(), 40 + 30 * k, 40 + 30 * k);
    }

    // A performer always has a spotlight on him (LightEmitter interface).
    @Override public float getLightX() { return getX(); }
    @Override public float getLightY() { return getY(); }
    @Override public float getLightRadius() { return 150; }
    @Override public float getLightIntensity(float t) { return 0.9f; }
    @Override public Color getGlowColor() { return mode == Mode.GROOVE ? discoColor(clock * 3) : new Color(255, 236, 190); }
    @Override public int getGlowAlpha() { return 45; }
}
