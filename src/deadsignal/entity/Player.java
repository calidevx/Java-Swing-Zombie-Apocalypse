package deadsignal.entity;

import deadsignal.core.Input;
import deadsignal.core.Util;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;
import deadsignal.gfx.ParticleType;
import deadsignal.progress.Medal;
import deadsignal.progress.PlayerProfile;
import deadsignal.skill.Skill;
import deadsignal.skill.SkillType;
import deadsignal.weapon.Weapon;
import deadsignal.world.Tile;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Kai Navarro, the hero. Reads input, moves, shoots, rolls, heals and interacts. */
public final class Player extends Creature {
    private static final float BASE_SPEED = 172, ROLL_TIME = 0.3f, ROLL_COOLDOWN = 1.05f, INTERACT_RANGE = 52;

    private final PlayerProfile profile;
    private float aim, moveAngle, anim, rollTimer, rollCooldown, rollDx, rollDy, invulnTimer, deathTimer, grenadeCooldown, hazardTimer;
    private boolean moving, godMode;
    // status effects applied by boss abilities (encapsulated: only changed through applySlow/jam/stun)
    private float slowTimer, slowFactor = 1f, jamTimer, stunTimer;
    private float rollDuration = ROLL_TIME, rollSpeedMul = 2.5f, moveX, moveY, magnetTimer;
    private final List<Skill> skills = new ArrayList<>();

    public Player(float x, float y, PlayerProfile profile) {
        super(x, y, 12, profile.maxHealth());
        this.profile = profile;
        for (SkillType t : SkillType.values()) if (profile.getSkillLevel(t) > 0) skills.add(t.create(profile.getSkillLevel(t)));
    }

    @Override
    public void update(GameWorld w, float dt) {
        if (isDead()) { deathTimer += dt; return; }
        tickFlash(dt);
        rollCooldown -= dt; invulnTimer -= dt; grenadeCooldown -= dt; hazardTimer -= dt;
        slowTimer -= dt; jamTimer -= dt; stunTimer -= dt;
        if (slowTimer <= 0) slowFactor = 1f;
        magnetTimer -= dt;
        for (Skill s : skills) s.update(dt);
        Input in = w.input();
        aim = (float) Math.atan2(w.mouseWorldY() - getY(), w.mouseWorldX() - getX());
        if (stunTimer > 0) { moving = false; profile.getCurrentWeapon().update(dt); return; }
        handleMovement(w, in, dt);
        handleWeapons(w, in, dt);
        handleItems(w, in);
        handleSkills(w, in);
        int under = w.getMap().tileAt(getX(), getY());
        if (Tile.isHazard(under) && hazardTimer <= 0 && rollTimer <= 0) { hazardTimer = 0.25f; takeDamage(5f, DamageType.FIRE, w); }
        Interactable target = w.findInteractable(this);
        if (target != null && in.isDown(KeyEvent.VK_E)) target.interact(w, dt);
    }

    private void handleMovement(GameWorld w, Input in, float dt) {
        float mx = 0, my = 0;
        if (in.isDown(KeyEvent.VK_W) || in.isDown(KeyEvent.VK_UP)) my -= 1;
        if (in.isDown(KeyEvent.VK_S) || in.isDown(KeyEvent.VK_DOWN)) my += 1;
        if (in.isDown(KeyEvent.VK_A) || in.isDown(KeyEvent.VK_LEFT)) mx -= 1;
        if (in.isDown(KeyEvent.VK_D) || in.isDown(KeyEvent.VK_RIGHT)) mx += 1;
        float ml = Util.len(mx, my);
        if (ml > 0) { mx /= ml; my /= ml; }
        float speed = BASE_SPEED * profile.moveSpeedMultiplier() * slowFactor;
        if (w.getMap().tileAt(getX(), getY()) == Tile.WATER) speed *= 0.7f;
        if (rollTimer > 0) {
            rollTimer -= dt;
            moveWithCollision(w.getMap(), rollDx * speed * rollSpeedMul * dt, rollDy * speed * rollSpeedMul * dt, getRadius());
            if (Util.rf() < 0.6f) w.particles().spawn(ParticleType.SMOKE, getX(), getY() + 6, Util.rf(-20, 20), Util.rf(-20, 20), 0.4f, 6, new Color(120, 110, 100, 120));
        } else {
            moveWithCollision(w.getMap(), mx * speed * dt, my * speed * dt, getRadius());
        }
        moving = ml > 0;
        moveX = mx; moveY = my;
        if (moving) { moveAngle = (float) Math.atan2(my, mx); anim += dt * speed / 22f; }
        if (in.wasPressed(KeyEvent.VK_SPACE) && rollCooldown <= 0 && moving) {
            rollTimer = ROLL_TIME; rollDuration = ROLL_TIME; rollSpeedMul = 2.5f; rollCooldown = ROLL_COOLDOWN; invulnTimer = 0.34f; rollDx = mx; rollDy = my;
            w.audio().play("dash", 0.6f, 1f);
        }
    }

    private void handleWeapons(GameWorld w, Input in, float dt) {
        for (int k = 0; k < PlayerProfile.WEAPON_SLOTS; k++)
            if (in.wasPressed(KeyEvent.VK_1 + k) && profile.ownsWeapon(k) && profile.getCurrentSlot() != k) switchTo(k, w);
        if (in.getWheel() != 0) {
            int s = profile.nextOwnedSlot(in.getWheel() > 0 ? 1 : -1);
            if (s != profile.getCurrentSlot()) switchTo(s, w);
        }
        Weapon wpn = profile.getCurrentWeapon();
        wpn.update(dt);
        if (in.wasPressed(KeyEvent.VK_R)) wpn.requestReload(profile.reloadSpeedMultiplier(), w.audio());
        if (in.isLeftDown() && rollTimer <= 0) {
            if (jamTimer > 0) { if (in.isLeftDown() && Util.rf() < dt * 3) w.addText("JAMMED!", getX(), getY() - 28, new Color(255, 80, 80), 13); }
            else wpn.trigger(this, w, profile.damageMultiplier(), profile.fireRateMultiplier(), profile.reloadSpeedMultiplier());
        }
    }

    private void handleSkills(GameWorld w, Input in) {
        for (Skill s : skills) {
            if (s.isPassive() || !in.wasPressed(s.getType().getKey())) continue;
            if (!s.tryActivate(w, this))
                w.addText(s.getType().getTitle() + " " + (int) Math.ceil(s.getCooldownLeft()) + "s", getX(), getY() - 30, new Color(180, 180, 200), 13);
        }
    }

    /** Called by the world whenever one of the player's attacks lands (passive skills react here). */
    public void onDamageDealt(GameWorld w, float amount) { for (Skill s : skills) s.onDamageDealt(w, this, amount); }

    /** A forced invulnerable dash (used by the Decoy Dash skill). */
    public void dash(float dx, float dy, float seconds, float speedMultiplier) {
        rollTimer = seconds; rollDuration = seconds; rollSpeedMul = speedMultiplier; rollDx = dx; rollDy = dy;
        invulnTimer = Math.max(invulnTimer, seconds + 0.1f);
    }

    public void activateMagnet(float seconds) { magnetTimer = Math.max(magnetTimer, seconds); }
    public float getMagnetTime() { return Math.max(0, magnetTimer); }
    /** Pickup attraction radius: huge while a magnet power-up is active. */
    public float getMagnetRadius() { return magnetTimer > 0 ? 720 : 110; }

    private void handleItems(GameWorld w, Input in) {
        if ((in.wasPressed(KeyEvent.VK_G) || in.rightClicked()) && grenadeCooldown <= 0) {
            if (profile.useGrenade()) throwGrenade(w);
            else w.addText("NO GRENADES", getX(), getY() - 26, new Color(255, 120, 100), 12);
        }
        if (in.wasPressed(KeyEvent.VK_Q)) {
            if (getHealth() >= getMaxHealth()) return;
            if (profile.useMedkit()) {
                heal(60);
                w.audio().play("heal");
                for (int i = 0; i < 16; i++) w.particles().spawn(ParticleType.GLOW, getX() + Util.rf(-14, 14), getY() + Util.rf(-14, 14), 0, -40, 0.8f, 3, new Color(120, 255, 140));
                w.addText("+60 HP", getX(), getY() - 30, new Color(120, 255, 140), 15);
                if (profile.getMedkitsUsed() >= 3) w.award(Medal.FIELD_MEDIC);
            } else w.addText("NO MEDKITS", getX(), getY() - 26, new Color(255, 120, 100), 12);
        }
    }

    private void switchTo(int slot, GameWorld w) {
        profile.selectWeapon(slot);
        profile.getCurrentWeapon().onEquip();
        w.audio().play("reload", 0.5f, 1.4f);
        w.addText(profile.getCurrentWeapon().getName(), getX(), getY() - 30, new Color(220, 220, 255), 13);
    }

    private void throwGrenade(GameWorld w) {
        grenadeCooldown = 0.6f;
        float dx = w.mouseWorldX() - getX(), dy = w.mouseWorldY() - getY(), d = Util.len(dx, dy);
        if (d > 340) { dx *= 340 / d; dy *= 340 / d; d = 340; }
        float tx = getX(), ty = getY();
        int steps = Math.max(1, (int) (d / 8));
        for (int i = 1; i <= steps; i++) {
            float nx = getX() + dx * i / steps, ny = getY() + dy * i / steps;
            if (w.getMap().isSolidAt(nx, ny)) break;
            tx = nx; ty = ny;
        }
        float flight = 0.45f + d / 1100f;
        w.addGrenade(new Grenade(getX(), getY(), tx, ty, flight, flight + 0.45f));
        w.audio().play("throw");
    }

    // ------------------------------------------------------------------ status effects (boss abilities)
    /** Slows movement to {@code factor} (0..1) for a while. The strongest slow wins. */
    public void applySlow(float factor, float seconds) {
        if (godMode || isDead()) return;
        if (slowTimer <= 0 || factor < slowFactor) slowFactor = factor;
        slowTimer = Math.max(slowTimer, seconds);
    }
    /** Jams every weapon: the player cannot shoot for a while. */
    public void jamWeapons(float seconds) { if (!godMode && !isDead() && rollTimer <= 0) jamTimer = Math.max(jamTimer, seconds); }
    /** Stuns: no moving, shooting or rolling. Dodge-rolling avoids it. */
    public void stun(float seconds) { if (!godMode && !isDead() && rollTimer <= 0) stunTimer = Math.max(stunTimer, seconds); }
    public boolean isSlowed() { return slowTimer > 0; }
    public boolean isJammed() { return jamTimer > 0; }
    public boolean isStunned() { return stunTimer > 0; }

    /** Damage from a source at (sx, sy): applies the hit, then a small knock-back. */
    public void hurt(float amount, float sx, float sy, DamageType type, GameWorld w) {
        float before = getHealth();
        takeDamage(amount, type, w);
        if (getHealth() < before && !isDead()) {
            float dx = getX() - sx, dy = getY() - sy, l = Math.max(1, Util.len(dx, dy));
            moveWithCollision(w.getMap(), dx / l * 10, dy / l * 10, getRadius());
        }
    }

    // ------------------------------------------------------------------ Creature hooks (polymorphism)
    @Override protected boolean canBeDamaged(DamageType type) {
        if (godMode || rollTimer > 0) return false;
        return type.isDamageOverTime() || invulnTimer <= 0;
    }
    @Override protected float modifyIncomingDamage(float amount, DamageType type, GameWorld w) {
        return amount * w.getDifficulty().getDamageTaken() * profile.damageTakenMultiplier();
    }
    @Override protected void onHurt(float dmg, DamageType type, GameWorld w) {
        if (!type.isDamageOverTime()) invulnTimer = 0.3f;
        w.onPlayerDamaged(dmg);
        w.audio().play("hurt", type.isDamageOverTime() ? 0.3f : 0.7f, 1f);
        for (int i = 0; i < 5; i++) w.particles().spawn(ParticleType.BLOOD, getX(), getY(), Util.rf(-90, 90), Util.rf(-90, 90), 0.5f, 2.5f, new Color(170, 20, 20));
    }
    @Override protected void onDeath(GameWorld w, DamageType type) {
        deathTimer = 0;
        profile.recordDeath();
        w.audio().play("zdie", 1f, 0.6f);
        Util.splat(w.getMap().getDecals(), getX(), getY(), 22, new Random(), new Color(120, 10, 10));
        w.onPlayerDeath();
    }

    // ------------------------------------------------------------------ rendering
    @Override
    public void render(Graphics2D g, Assets a) {
        if (isDead()) {
            float k = Math.min(1, deathTimer / 0.8f);
            g.setColor(new Color(110, 10, 10, (int) (180 * k))); Util.oval(g, getX(), getY(), 10 + 14 * k, 8 + 10 * k);
            AffineTransform at = g.getTransform();
            g.translate(getX(), getY()); g.rotate(aim + k * 1.2f); g.scale(1, 1 - 0.25f * k);
            g.drawImage(Util.darken(a.playerTorso(false), 1 - 0.4f * k, 1), -30, -30, null);
            g.setTransform(at);
            return;
        }
        g.setColor(new Color(0, 0, 0, 80)); Util.oval(g, getX() + 3, getY() + 5, 13, 10);
        Composite oc = g.getComposite();
        if (invulnTimer > 0 && rollTimer <= 0 && ((int) (System.nanoTime() / 40_000_000L)) % 2 == 0)
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        int lf = moving ? ((int) anim) & 7 : 0;
        float spin = rollTimer > 0 ? (1 - rollTimer / rollDuration) * 6.283f : 0;
        float sc = rollTimer > 0 ? 0.85f : 1f;
        Util.drawRotated(g, a.playerLegs(lf), getX(), getY(), (moving ? moveAngle : aim) + spin, sc);
        Weapon wpn = profile.getCurrentWeapon();
        AffineTransform at = g.getTransform();
        g.translate(getX(), getY()); g.rotate(aim + spin); g.scale(sc, sc); g.translate(-wpn.getRecoilOffset(), 0);
        wpn.drawModel(g);
        g.drawImage(a.playerTorso(isFlashing()), -30, -30, null);
        if (wpn.showMuzzleFlash()) {
            float tip = wpn.getTipLength();
            g.setPaint(new RadialGradientPaint(tip + 4, 0, 12, new float[]{0, 1}, new Color[]{new Color(255, 250, 200, 255), new Color(255, 160, 40, 0)}));
            Util.oval(g, tip + 4, 0, 12, 7);
        }
        g.setTransform(at);
        g.setComposite(oc);
        renderStatus(g);
        if (wpn.isReloading()) {
            g.setColor(new Color(0, 0, 0, 150)); g.setStroke(new BasicStroke(4f)); Util.ring(g, getX(), getY(), 22, 22);
            g.setColor(new Color(255, 220, 120)); g.setStroke(new BasicStroke(3f));
            g.draw(new Arc2D.Double(getX() - 22, getY() - 22, 44, 44, 90, -360 * wpn.getReloadProgress(), Arc2D.OPEN));
        }
    }

    private void renderStatus(Graphics2D g) {
        float x = getX(), y = getY(), t = System.nanoTime() / 1e9f;
        if (magnetTimer > 0) {
            float pr = 22 + (t * 40) % 20;
            g.setColor(new Color(110, 190, 255, (int) (120 * (1 - (pr - 22) / 20)))); g.setStroke(new BasicStroke(2f)); Util.ring(g, x, y, pr, pr);
        }
        if (slowTimer > 0) { g.setColor(new Color(120, 220, 90, 120)); g.setStroke(new BasicStroke(2f)); Util.ring(g, x, y + 4, 16, 9); }
        if (stunTimer > 0) for (int k = 0; k < 3; k++) {
            float a = t * 6 + k * 2.09f;
            Util.drawStar(g, x + (float) Math.cos(a) * 13, y - 22 + (float) Math.sin(a) * 4, 4, new Color(255, 235, 110));
        }
        if (jamTimer > 0) {
            g.setColor(new Color(200, 20, 30, 220)); g.fillRoundRect((int) x - 22, (int) y - 40, 44, 14, 6, 6);
            g.setColor(Color.WHITE); g.setFont(deadsignal.gfx.Fonts.TINY); Util.ctext(g, "JAMMED", (int) x, (int) y - 30, Color.WHITE);
        }
    }

    // ------------------------------------------------------------------ getters
    public float getAim() { return aim; }
    public float getMoveX() { return moveX; }
    public float getMoveY() { return moveY; }
    public List<Skill> getSkills() { return Collections.unmodifiableList(skills); }
    public boolean isRolling() { return rollTimer > 0; }
    public float getRollReadiness() { return rollCooldown > 0 ? 1 - rollCooldown / ROLL_COOLDOWN : 1; }
    public float getDeathTimer() { return deathTimer; }
    public PlayerProfile getProfile() { return profile; }
    public float getInteractRange() { return INTERACT_RANGE; }
    public void setGodMode(boolean god) { godMode = god; }
}
