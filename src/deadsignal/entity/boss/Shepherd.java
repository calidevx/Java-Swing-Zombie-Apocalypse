package deadsignal.entity.boss;

import deadsignal.core.Util;
import deadsignal.entity.DamageType;
import deadsignal.entity.Player;
import deadsignal.entity.projectile.EnergyOrb;
import deadsignal.entity.projectile.HomingSpore;
import deadsignal.entity.zombie.ZombieType;
import deadsignal.game.GameWorld;
import deadsignal.gfx.BossSprite;
import deadsignal.gfx.LightEmitter;
import deadsignal.gfx.ParticleType;
import deadsignal.story.DialogueLine;
import deadsignal.story.Speaker;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Final boss, Patient Zero. The hardest fight in the game:
 *  - TWO LIVES (two health bars). When the first is emptied he SINKS INTO THE VOID and a horde of
 *    runners and spitters pours in; once the horde thins he rises again with a full second bar.
 *  - BLINK: teleports around the arena, leaving afterimages, then fires immediately.
 *  - VEIL WALK: bullets pass through him; only fire and explosives hurt.
 *  - At 1/3 of his second life: "THE FLOCK DIVIDES!" - he splits into BONE, BLIGHT and SHADE.
 *    Kill all three to end him.
 */
public final class Shepherd extends Boss implements LightEmitter {
    private enum Mode { DRIFT, WINDUP, CHARGE, SINKING, VOID, RISING, SPLIT }
    private static final float VEIL_DURATION = 5f, SINK_TIME = 1.6f, RISE_TIME = 1.4f, VOID_MAX = 16f;
    private Mode mode = Mode.DRIFT;
    private int life = 1, phase;
    private float modeTimer, ringCooldown = 2f, chargeCooldown = 4f, homingCooldown = 3f, blinkCooldown = 3f, spiralAcc, spin, chargeX, chargeY, clock;
    private float veilTimer, veilCooldown, ghostTimer, voidX, voidY, hordeTimer;
    private boolean veilUnlocked, veilExplained;
    private final List<ShepherdAspect> aspects = new ArrayList<>();
    private int aspectsTotal;

    public Shepherd(float x, float y, float hpMul) {
        super("THE SHEPHERD", "Patient Zero  -  Dr. Adrian Voss", BossSprite.SHEPHERD, x, y, 46, 3400 * hpMul, 18);
    }

    @Override
    protected void think(GameWorld w, float dt, float dx, float dy, float d) {
        clock += dt; modeTimer -= dt;
        switch (mode) {
            case SINKING: updateSinking(w); return;
            case VOID: updateVoid(w, dt); return;
            case RISING: updateRising(w); return;
            case SPLIT: return;                               // the aspects are fighting
            default: break;
        }
        ringCooldown -= dt; chargeCooldown -= dt; homingCooldown -= dt; blinkCooldown -= dt;
        updatePhase(w);
        updateVeil(w, dt);
        if (life == 2 && getHealthFraction() <= 1f / 3f) { split(w); return; }
        faceToward((float) Math.atan2(dy, dx), 4 * dt);
        Player p = w.getPlayer();
        if (mode == Mode.CHARGE) {
            floatTo(w, getX() + chargeX * 480 * dt, getY() + chargeY * 480 * dt);
            advanceAnim(dt * 14);
            if (Util.rf() < 0.6f) w.particles().spawn(ParticleType.GLOW, getX(), getY(), Util.rf(-40, 40), Util.rf(-40, 40), 0.5f, 4, new Color(200, 80, 255));
            if (d < getRadius() + p.getRadius() + 4) p.hurt(24, getX(), getY(), DamageType.MELEE, w);
            if (modeTimer <= 0) mode = Mode.DRIFT;
            return;
        }
        if (mode == Mode.WINDUP) {
            attackPose(0.1f);
            if (modeTimer <= 0) { mode = Mode.CHARGE; modeTimer = 0.65f; chargeX = (float) Math.cos(getAngle()); chargeY = (float) Math.sin(getAngle()); w.audio().play("dash", 1f, 0.5f); }
            return;
        }
        float speed = new float[]{70, 92, 118}[phase];
        float mvx = d > 170 ? dx / d : -dy / d * 0.7f, mvy = d > 170 ? dy / d : dx / d * 0.7f;
        floatTo(w, getX() + mvx * speed * dt, getY() + mvy * speed * dt);
        advanceAnim(dt * 4);
        if (blinkCooldown <= 0) { blink(w, p); return; }
        if (veilTimer > 0) ringCooldown -= dt;   // attacks faster while veiled
        if (ringCooldown <= 0) fireRing(w);
        if (phase >= 1) {
            spiralAcc += dt;
            float rate = phase == 2 ? 0.08f : 0.1f;
            int arms = phase == 2 ? 3 : 2;
            boolean active = ((int) (clock / 3f)) % 2 == 0 || phase == 2 && ((int) (clock / 2f)) % 3 != 2;
            while (spiralAcc > rate) {
                spiralAcc -= rate;
                if (!active) continue;
                spin += 0.13f;
                for (int k = 0; k < arms; k++) w.addProjectile(new EnergyOrb(getX(), getY(), spin + k * 6.28f / arms, 170, 8, 6, new Color(255, 90, 200)));
            }
            if (chargeCooldown <= 0) { mode = Mode.WINDUP; modeTimer = 0.55f; chargeCooldown = phase == 2 ? 4f : 5.5f; w.audio().play("roar", 0.6f, 1.2f); }
        }
        if (phase == 2 && homingCooldown <= 0) {
            homingCooldown = 3.5f;
            float face = (float) Math.atan2(dy, dx);
            for (int k = 0; k < 3; k++) w.addProjectile(new HomingSpore(getX(), getY(), face + (k - 1) * 0.8f));
            if (w.zombieCount() < 18) summon(w, 3, ZombieType.RUNNER);
        }
        if (d < 120 && ringCooldown > 0.2f && Util.rf() < dt * 1.2f) {
            p.hurt(16, getX(), getY(), DamageType.MELEE, w);
            for (int i = 0; i < 8; i++) w.particles().spawn(ParticleType.SPARK, p.getX(), p.getY(), Util.rf(-120, 120), Util.rf(-120, 120), 0.25f, 2, new Color(220, 120, 255));
        }
    }

    private void fireRing(GameWorld w) {
        int n = phase == 0 ? 16 : 14;
        spin += 0.35f;
        for (int k = 0; k < n; k++) w.addProjectile(new EnergyOrb(getX(), getY(), spin + k / (float) n * 6.28f, phase == 2 ? 215 : 185, 9, 7, new Color(220, 110, 255)));
        ringCooldown = phase == 0 ? 1.9f : 2.7f; attackPose(0.4f); w.audio().play("zap", 0.7f, 0.6f);
    }

    // ------------------------------------------------------------------ blink
    private void blink(GameWorld w, Player p) {
        float maxX = w.getMap().getWidth() * 32 - 60, maxY = w.getMap().getHeight() * 32 - 60;
        float a = Util.rf(0, 6.283f), r = Util.rf(180, 280);
        float nx = Util.clamp(p.getX() + (float) Math.cos(a) * r, 60, maxX), ny = Util.clamp(p.getY() + (float) Math.sin(a) * r, 60, maxY);
        for (int k = 0; k < 3; k++) leaveAfterimage();
        burst(w, getX(), getY());
        teleport(nx, ny);
        burst(w, nx, ny);
        setAngle((float) Math.atan2(p.getY() - ny, p.getX() - nx));
        w.audio().play("zap", 1f, 1.5f);
        w.addFlash(nx, ny, 160, 0.15f, new Color(200, 120, 255));
        blinkCooldown = life == 2 ? Util.rf(2.6f, 3.6f) : Util.rf(3.8f, 5f);
        ringCooldown = Math.min(ringCooldown, 0.35f);         // punish right after arriving
    }

    private void burst(GameWorld w, float x, float y) {
        for (int i = 0; i < 18; i++) w.particles().spawn(ParticleType.GLOW, x, y, Util.rf(-180, 180), Util.rf(-180, 180), 0.45f, 4, new Color(210, 120, 255));
        w.particles().spawn(ParticleType.RING, x, y, 0, 0, 0.3f, 70, new Color(220, 150, 255));
    }

    // ------------------------------------------------------------------ first life ends -> the void
    /** POLYMORPHISM: the Shepherd does not die the first time - he sinks into the void instead. */
    @Override
    protected void onDeath(GameWorld w, DamageType type) {
        if (life == 1 && type != DamageType.PURGE) { enterVoid(w); return; }
        for (ShepherdAspect a : aspects) if (!a.isDead()) a.takeDamage(1e9f, DamageType.PURGE, w);
        super.onDeath(w, type);
    }

    private void enterVoid(GameWorld w) {
        life = 2;
        setMaxHealth(getMaxHealth(), true);                    // the second bar starts full
        mode = Mode.SINKING; modeTimer = SINK_TIME;
        veilTimer = 0;
        voidX = getX(); voidY = getY();
        w.announcer().showBanner("THE SHEPHERD SINKS INTO THE VOID", new Color(200, 120, 255));
        w.radio().queue(new DialogueLine(Speaker.VOSS, "Death is only a door. My flock will hold it open."));
        w.audio().play("roar", 1f, 0.5f);
        w.shake(18); w.whiteFlash(0.3f);
    }

    private void updateSinking(GameWorld w) {
        float t = 1 - Math.max(0, modeTimer) / SINK_TIME;
        setAlpha(1 - t);
        if (Util.rf() < 0.6f) w.particles().spawn(ParticleType.GLOW, voidX + Util.rf(-50, 50), voidY + Util.rf(-50, 50), 0, Util.rf(-60, -20), 0.8f, 4, new Color(150, 60, 220));
        if (modeTimer <= 0) {
            mode = Mode.VOID; hordeTimer = VOID_MAX; setAlpha(0);
            unleashHorde(w);
        }
    }

    private void unleashHorde(GameWorld w) {
        int runners = 14, spitters = 6;
        for (int i = 0; i < runners + spitters; i++) {
            float[] pt = w.findSpawnPoint(260, 700, false);
            if (pt == null) continue;
            w.spawnZombie(i < runners ? ZombieType.RUNNER : ZombieType.SPITTER, pt[0], pt[1]);
            for (int k = 0; k < 6; k++) w.particles().spawn(ParticleType.SMOKE, pt[0], pt[1], Util.rf(-40, 40), Util.rf(-40, 40), 0.7f, 9, new Color(60, 20, 80, 160));
        }
        w.announcer().showBanner("THE HORDE RISES", new Color(255, 90, 90));
        w.radio().queue(new DialogueLine(Speaker.LENA, "He's gone - no, he's still there, under everything! Hold out, {name}!"));
    }

    private void updateVoid(GameWorld w, float dt) {
        hordeTimer -= dt;
        if (Util.rf() < dt * 20) w.particles().spawn(ParticleType.GLOW, voidX + Util.rf(-60, 60), voidY + Util.rf(-60, 60), 0, -30, 1f, 3, new Color(130, 40, 200));
        if (hordeTimer <= 0 || (hordeTimer < VOID_MAX - 4 && w.zombieCount() <= 4)) {
            Player p = w.getPlayer();
            float a = Util.rf(0, 6.283f);
            float[] pt = w.reachableNear(p.getX() + (float) Math.cos(a) * 240, p.getY() + (float) Math.sin(a) * 240);
            voidX = pt[0]; voidY = pt[1];
            teleport(voidX, voidY);
            mode = Mode.RISING; modeTimer = RISE_TIME;
            w.announcer().showBanner("THE SHEPHERD RETURNS", new Color(255, 90, 200));
            w.audio().play("roar", 1f, 0.7f);
            w.shake(14);
        }
    }

    private void updateRising(GameWorld w) {
        setAlpha(1 - Math.max(0, modeTimer) / RISE_TIME);
        if (modeTimer <= 0) {
            setAlpha(1); mode = Mode.DRIFT; blinkCooldown = 1.5f; ringCooldown = 0.2f;
            phase = 2;
            w.radio().queue(new DialogueLine(Speaker.VOSS, "Again. As many times as it takes."));
        }
    }

    // ------------------------------------------------------------------ the split
    private void split(GameWorld w) {
        mode = Mode.SPLIT; veilTimer = 0; setAlpha(0);
        float hp = getMaxHealth() * 0.24f;
        aspects.add(new BoneAspect(this, getX() - 60, getY(), hp));
        aspects.add(new BlightAspect(this, getX() + 60, getY() - 40, hp));
        aspects.add(new ShadeAspect(this, getX() + 20, getY() + 60, hp));
        aspectsTotal = aspects.size();
        for (ShepherdAspect a : aspects) w.addStructure(a);
        w.addText("THE FLOCK DIVIDES!", getX(), getY() - 80, new Color(255, 120, 230), 30);
        announce(w, "THE FLOCK DIVIDES!", new Color(255, 120, 230));
        w.announcer().showBanner("THE FLOCK DIVIDES!", new Color(255, 120, 230));
        w.radio().queue(new DialogueLine(Speaker.VOSS, "THE FLOCK DIVIDES!"));
        w.radio().queue(new DialogueLine(Speaker.LENA, "He split into three! Bone, Blight and Shade - take them all down!"));
        burst(w, getX(), getY());
        w.particles().spawn(ParticleType.RING, getX(), getY(), 0, 0, 0.6f, 220, new Color(255, 120, 230));
        w.whiteFlash(0.4f); w.shake(22);
        w.audio().play("roar", 1f, 0.4f);
    }

    /** Called by an aspect when it dies. When the last one falls, the Shepherd is finished. */
    void onAspectDown(GameWorld w, ShepherdAspect a) {
        aspects.remove(a);
        if (aspects.isEmpty() && !isDead()) {
            ShepherdAspect last = a;
            teleport(last.getX(), last.getY());
            setAlpha(1);
            kill(w, DamageType.EXPLOSION);
        } else w.addText(aspects.size() + " ASPECT" + (aspects.size() == 1 ? "" : "S") + " REMAIN", a.getX(), a.getY() - 72, new Color(255, 180, 230), 15);
    }

    private boolean isHidden() { return mode == Mode.SINKING || mode == Mode.VOID || mode == Mode.RISING || mode == Mode.SPLIT; }

    // ------------------------------------------------------------------ veil walk
    private void updateVeil(GameWorld w, float dt) {
        if (!veilUnlocked && (life == 2 || getHealthFraction() < 0.75f)) { veilUnlocked = true; veilCooldown = 0; }
        if (!veilUnlocked) return;
        if (veilTimer > 0) {
            veilTimer -= dt; ghostTimer -= dt;
            setAlpha(0.5f);
            if (ghostTimer <= 0) { ghostTimer = 0.12f; leaveAfterimage(); }
            if (veilTimer <= 0) { setAlpha(1f); w.addText("VEIL FADES", getX(), getY() - 60, new Color(200, 240, 255), 15); }
            return;
        }
        veilCooldown -= dt;
        if (veilCooldown <= 0 && mode == Mode.DRIFT) {
            veilTimer = VEIL_DURATION;
            veilCooldown = 17f;
            announce(w, "VEIL WALK", new Color(170, 235, 255));
            w.whiteFlash(0.25f);
            w.audio().play("roar", 0.8f, 1.4f);
            if (!veilExplained) {
                veilExplained = true;
                w.radio().queue(new DialogueLine(Speaker.LENA, "He's slipped beyond the veil - bullets pass right through him! Use fire and explosives, {name}!"));
            }
        }
    }

    public boolean isVeiled() { return veilTimer > 0; }
    public int getLife() { return life; }

    @Override public boolean isImmuneTo(DamageType type) {
        if (isHidden()) return type != DamageType.PURGE;
        return veilTimer > 0 && type == DamageType.BULLET;
    }
    @Override public boolean isTargetable() { return !isHidden() && super.isTargetable(); }
    @Override protected float getContactDamage() { return isHidden() ? 0 : super.getContactDamage(); }

    @Override protected float modifyIncomingDamage(float amount, DamageType type, GameWorld w) {
        float dmg = super.modifyIncomingDamage(amount, type, w) * (veilTimer > 0 ? 1.5f : 1f);
        // the second life can never be skipped: one huge hit still leaves him alive to split
        if (life == 2 && aspectsTotal == 0 && type != DamageType.PURGE) dmg = Math.min(dmg, getHealth() - 1);
        return dmg;
    }

    private void updatePhase(GameWorld w) {
        int ph = life == 2 ? 2 : getHealthFraction() > 0.5f ? 0 : 1;
        if (ph == phase) return;
        phase = ph;
        w.shake(16); w.audio().play("roar", 1f, 0.6f); w.whiteFlash(0.3f);
        if (ph == 1) {
            summon(w, 8, ZombieType.WALKER);
            w.radio().queue(new DialogueLine(Speaker.VOSS, "You cannot kill what has already died!"));
            w.announcer().showBanner("THE SHEPHERD GROWS STRONGER", new Color(220, 120, 255));
        }
    }

    /** POLYMORPHISM: two health bars, and during the split one bar per aspect. */
    @Override
    public List<BossBar> getHudBars() {
        List<BossBar> bars = new ArrayList<>();
        Color top = new Color(220, 90, 255), bottom = new Color(90, 10, 130);
        if (mode == Mode.SPLIT) {
            float sum = 0, max = 0;
            for (ShepherdAspect a : aspects) { sum += a.getHealth(); max += a.getMaxHealth(); }
            bars.add(new BossBar("THE FLOCK  -  " + aspects.size() + "/" + aspectsTotal + " REMAIN", max <= 0 ? 0 : sum / max, new Color(255, 120, 230), new Color(130, 20, 100)));
            for (ShepherdAspect a : aspects) bars.add(new BossBar(a.getTitle(), a.getHealthFraction(), a.getTint(), a.getTint().darker().darker()));
            return bars;
        }
        if (life == 1) {
            bars.add(new BossBar(getName() + "  -  LIFE I", getHealthFraction(), top, bottom));
            bars.add(new BossBar("LIFE II", 1f, new Color(150, 70, 200), new Color(60, 10, 90)));
        } else {
            String label = mode == Mode.VOID || mode == Mode.SINKING ? getName() + "  -  IN THE VOID" : getName() + "  -  LIFE II";
            bars.add(new BossBar(label, getHealthFraction(), new Color(255, 80, 200), new Color(120, 10, 80)));
            bars.add(new BossBar("SPLIT AT 1/3", Math.max(0, (getHealthFraction() - 1f / 3f) / (2f / 3f)), new Color(255, 170, 230), new Color(120, 60, 100)));
        }
        return bars;
    }

    /** When Patient Zero dies, every infected on the map collapses. */
    @Override protected void onDefeated(GameWorld w) { w.purgeAllZombies(); }

    @Override protected void renderAura(Graphics2D g) {
        if (mode == Mode.SINKING || mode == Mode.VOID || mode == Mode.RISING) {
            float r = 90 + 10 * (float) Math.sin(clock * 4);
            g.setPaint(new RadialGradientPaint(voidX, voidY, r, new float[]{0, 0.55f, 1}, new Color[]{new Color(10, 0, 20, 240), new Color(70, 10, 120, 160), new Color(120, 40, 200, 0)}));
            Util.oval(g, voidX, voidY, r, r);
            g.setColor(new Color(190, 110, 255, 170)); g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{5, 8}, 13 - (clock * 50) % 13));
            Util.ring(g, voidX, voidY, r * 0.7f, r * 0.7f);
            return;
        }
        if (mode == Mode.SPLIT) return;
        if (veilTimer > 0) {
            g.setPaint(new RadialGradientPaint(getX(), getY(), 130, new float[]{0, 1}, new Color[]{new Color(170, 235, 255, 110), new Color(80, 160, 220, 0)}));
            Util.oval(g, getX(), getY(), 130, 130);
            g.setColor(new Color(200, 245, 255, 160)); g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f, new float[]{6, 9}, clock * 40));
            Util.ring(g, getX(), getY(), 70, 70);
            return;
        }
        Color c = life == 2 ? new Color(255, 60, 200, 100) : new Color(200, 60, 255, 90);
        g.setPaint(new RadialGradientPaint(getX(), getY(), 110, new float[]{0, 1}, new Color[]{c, new Color(120, 20, 160, 0)}));
        Util.oval(g, getX(), getY(), 110, 110);
    }
    @Override protected float bodyScale() {
        if (mode == Mode.SINKING) return 1 - 0.5f * (1 - Math.max(0, modeTimer) / SINK_TIME);
        if (mode == Mode.RISING) return 0.5f + 0.5f * (1 - Math.max(0, modeTimer) / RISE_TIME);
        return 1 + 0.03f * (float) Math.sin(clock * 3);
    }

    @Override protected void renderTelegraph(Graphics2D g) {
        if (mode != Mode.WINDUP) return;
        g.setColor(new Color(220, 80, 255, 90)); g.setStroke(new BasicStroke(70f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Util.line(g, getX(), getY(), getX() + (float) Math.cos(getAngle()) * 320, getY() + (float) Math.sin(getAngle()) * 320);
    }

    // The Shepherd glows: it lights up the darkness around it (LightEmitter interface).
    @Override public float getLightX() { return isHidden() ? voidX : getX(); }
    @Override public float getLightY() { return isHidden() ? voidY : getY(); }
    @Override public float getLightRadius() { return mode == Mode.SPLIT ? 0 : 170; }
    @Override public float getLightIntensity(float t) { return isDead() ? 0.4f : 0.8f; }
}
