package deadsignal.entity.boss;

import deadsignal.core.Util;
import deadsignal.entity.Creature;
import deadsignal.entity.DamageType;
import deadsignal.entity.Player;
import deadsignal.entity.zombie.ZombieType;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;
import deadsignal.gfx.BossSprite;
import deadsignal.gfx.ParticleType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ABSTRACT boss. Handles everything bosses share: dying sequence, contact damage, line-of-sight,
 * chasing, summoning, rendering. Each boss subclass only implements {@link #think} - its unique AI.
 */
public abstract class Boss extends Creature {
    private final String name, subtitle;
    private final BossSprite sprite;
    private final float collisionRadius, contactDamage;
    private float angle, anim, attackAnim, alpha = 1, contactCooldown, dyingTimer, losTimer;
    private boolean lineOfSight;
    private String activeAbility;
    private float abilityTimer, staggerTimer;
    private final List<float[]> afterimages = new ArrayList<>();

    protected Boss(String name, String subtitle, BossSprite sprite, float x, float y, float radius, float health, float contactDamage) {
        super(x, y, radius, health);
        this.name = name; this.subtitle = subtitle; this.sprite = sprite;
        this.collisionRadius = Math.min(radius, 25);
        this.contactDamage = contactDamage;
    }

    /** The unique brain of each boss (POLYMORPHISM: six different implementations). */
    protected abstract void think(GameWorld w, float dt, float dx, float dy, float dist);

    @Override
    public final void update(GameWorld w, float dt) {
        tickFlash(dt);
        attackAnim -= dt; contactCooldown -= dt; losTimer -= dt; abilityTimer -= dt;
        for (int i = afterimages.size() - 1; i >= 0; i--) { float[] gh = afterimages.get(i); gh[3] -= dt * 3; if (gh[3] <= 0) afterimages.remove(i); }
        if (isDying()) { updateDying(w, dt); return; }
        if (staggerTimer > 0) { staggerTimer -= dt; return; }
        Player p = w.getPlayer();
        float dx = p.getX() - getX(), dy = p.getY() - getY(), d = Math.max(1, Util.len(dx, dy));
        if (losTimer <= 0) { losTimer = 0.2f; lineOfSight = w.getMap().hasLineOfSight(getX(), getY(), p.getX(), p.getY()); }
        if (p.isDead()) { anim += dt * 2; return; }
        think(w, dt, dx, dy, d);
        if (d < getRadius() * 0.85f + p.getRadius() && contactCooldown <= 0 && alpha > 0.5f) {
            p.hurt(getContactDamage(), getX(), getY(), DamageType.MELEE, w);
            contactCooldown = 0.9f;
        }
    }

    private void updateDying(GameWorld w, float dt) {
        dyingTimer -= dt;
        anim += dt * 3;
        if (Util.rf() < dt * 12) w.explosionEffect(getX() + Util.rf(-getRadius(), getRadius()), getY() + Util.rf(-getRadius(), getRadius()));
        if (dyingTimer <= 0) { onDefeated(w); w.onBossDefeated(this); remove(); }
    }

    // ------------------------------------------------------------------ Creature hooks
    @Override protected float modifyIncomingDamage(float amount, DamageType type, GameWorld w) {
        return amount * (type == DamageType.FIRE ? 0.6f : 1f) * damageMultiplier();
    }
    /** Hook: e.g. a stunned Censer takes extra damage. */
    protected float damageMultiplier() { return 1f; }
    @Override protected void onHurt(float dmg, DamageType type, GameWorld w) {
        if (Util.rf() < 0.35f) w.addText(String.valueOf((int) dmg), getX() + Util.rf(-20, 20), getY() - getRadius(), new Color(255, 240, 200), 12);
    }
    @Override protected void onDeath(GameWorld w, DamageType type) {
        dyingTimer = 2.4f;
        w.onBossDying(this);
    }
    /** Hook: extra effect when the boss finally dies. */
    protected void onDefeated(GameWorld w) { }
    @Override public boolean isTargetable() { return !isDead() && alpha > 0.3f; }

    // ------------------------------------------------------------------ helpers for subclasses
    protected final int chase(GameWorld w, float speed, float dt, float dx, float dy, float d) {
        float tx, ty;
        float[] f = (lineOfSight || d < 120) ? null : w.getMap().flowDirection(getX(), getY());
        if (f != null) { tx = f[0]; ty = f[1]; } else { tx = dx / d; ty = dy / d; }
        int bl = moveWithCollision(w.getMap(), tx * speed * dt, ty * speed * dt, collisionRadius);
        if (bl == 3) { float[] g = w.getMap().flowDirection(getX(), getY()); if (g != null) moveWithCollision(w.getMap(), g[0] * speed * dt, g[1] * speed * dt, collisionRadius); }
        angle = Util.turn(angle, (float) Math.atan2(ty, tx), 4 * dt);
        anim += dt * speed / 9f;
        return bl;
    }
    protected final int moveBody(GameWorld w, float dx, float dy) { return moveWithCollision(w.getMap(), dx, dy, collisionRadius); }
    protected final void floatTo(GameWorld w, float nx, float ny) {
        setPosition(Util.clamp(nx, 64, w.getMap().getPixelWidth() - 64), Util.clamp(ny, 64, w.getMap().getPixelHeight() - 64));
    }
    protected final void teleport(float nx, float ny) { setPosition(nx, ny); }
    protected final void summon(GameWorld w, int count, ZombieType type) {
        for (int k = 0; k < count; k++) for (int t = 0; t < 8; t++) {
            float a = Util.rf(0, 6.28f), dd = Util.rf(60, 130), x = getX() + (float) Math.cos(a) * dd, y = getY() + (float) Math.sin(a) * dd;
            if (!w.getMap().collides(x, y, 12) && w.getMap().isReachableNow(x, y)) {
                w.spawnZombie(type, x, y);
                for (int i = 0; i < 6; i++) w.particles().spawn(ParticleType.SMOKE, x, y, Util.rf(-40, 40), Util.rf(-40, 40), 0.7f, 8, new Color(60, 30, 60, 150));
                break;
            }
        }
    }
    /** Shows the ability name over the boss and on the HUD (the "cast bar"). */
    protected final void announce(GameWorld w, String ability, Color color) {
        activeAbility = ability;
        abilityTimer = 2.2f;
        w.addText(ability + "!", getX(), getY() - getRadius() - 18, color, 16);
    }
    protected final void faceToward(float target, float rate) { angle = Util.turn(angle, target, rate); }
    protected final void setAngle(float a) { angle = a; }
    protected final float getAngle() { return angle; }
    protected final void advanceAnim(float amount) { anim += amount; }
    protected final void attackPose(float seconds) { attackAnim = seconds; }
    protected final void setAlpha(float a) { alpha = a; }
    protected final boolean canSeePlayer() { return lineOfSight; }
    protected final void leaveAfterimage() { afterimages.add(new float[]{getX(), getY(), angle, 1f}); }
    protected final float getCollisionRadius() { return collisionRadius; }

    // ------------------------------------------------------------------ rendering
    @Override
    public final void render(Graphics2D g, Assets a) {
        renderTelegraph(g);
        for (float[] gh : afterimages) {
            Composite oc = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, gh[3] * 0.35f)));
            Util.drawRotated(g, a.boss(sprite, 0, false, false), gh[0], gh[1], gh[2], 1f);
            g.setComposite(oc);
        }
        renderAura(g);
        Composite oc = g.getComposite();
        float al = isDying() ? Math.max(0.1f, dyingTimer / 2.4f) : alpha;
        if (al < 1) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Util.clamp(al, 0, 1)));
        Util.drawRotated(g, bodyFrame(a, (int) anim, attackAnim > 0, isFlashing()), getX(), getY(), angle, bodyScale());
        g.setComposite(oc);
        renderOverlay(g);
        if (staggerTimer > 0) {
            float t = System.nanoTime() / 1e9f;
            for (int k = 0; k < 3; k++) { float an = t * 6 + k * 2.09f; Util.drawStar(g, getX() + (float) Math.cos(an) * 22, getY() - getRadius() - 10 + (float) Math.sin(an) * 5, 5, new Color(170, 230, 255)); }
        }
    }
    /** Hook: warning shapes drawn under the boss (charge lanes, slam circles). */
    protected void renderTelegraph(Graphics2D g) { }
    /** Hook: shadow or glow under the body. */
    protected void renderAura(Graphics2D g) { g.setColor(new Color(0, 0, 0, 90)); Util.oval(g, getX() + 5, getY() + 7, getRadius() * 1.2, getRadius()); }
    /** Hook: effects above the body. */
    protected void renderOverlay(Graphics2D g) { }
    protected float bodyScale() { return 1f; }

    /** Developer/test shortcut: ends the fight immediately, skipping extra lives. */
    public void forceDefeat(GameWorld w) { kill(w, DamageType.PURGE); }
    /** Briefly interrupts the boss (Shockwave skill). */
    public void stagger(float seconds) { if (!isDead()) staggerTimer = Math.max(staggerTimer, seconds); }
    public boolean isStaggered() { return staggerTimer > 0; }
    /** Hook: damage dealt by touching the boss (Graves hits harder while enraged). */
    protected float getContactDamage() { return contactDamage; }
    /** Hook: health bars shown on the HUD. Default: one bar. Graves adds RAGE, the Shepherd shows two lives. */
    public java.util.List<BossBar> getHudBars() {
        return java.util.Collections.singletonList(new BossBar(name, getHealthFraction(), new Color(240, 60, 60), new Color(120, 10, 20)));
    }
    /** Hook: which sprite frame to draw (the Broodmother picks one matching her remaining legs). */
    protected java.awt.image.BufferedImage bodyFrame(Assets a, int frame, boolean attack, boolean flash) { return a.boss(sprite, frame, attack, flash); }
    /** Hook: which music loop plays during this fight (the Maestro overrides it). */
    public String getMusicTrack() { return "boss"; }
    /** Name of the ability being used right now, or null. */
    public String getActiveAbility() { return abilityTimer > 0 ? activeAbility : null; }
    public String getName() { return name; }
    public String getSubtitle() { return subtitle; }
    public boolean isDying() { return isDead() && dyingTimer > 0; }
}
