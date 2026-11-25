package deadsignal.weapon;

import deadsignal.audio.Audio;
import deadsignal.core.Util;
import deadsignal.entity.Player;
import deadsignal.entity.projectile.Bullet;
import deadsignal.game.GameWorld;
import deadsignal.gfx.ParticleType;

import java.awt.*;

/**
 * ABSTRACT weapon. Ammo, fire-rate, reloading and recoil are ENCAPSULATED here (all private).
 * {@link #trigger} is a template method: subclasses only decide WHAT comes out of the barrel
 * ({@link #fireProjectiles}) and what the gun LOOKS like ({@link #drawModel}).
 */
public abstract class Weapon {
    private final String name, sound;
    private final int slot, magSize, baseReserve;
    private final float fireRate, reloadTime, recoilKick, shake, tipLength;
    private int mag, reserve;
    private float cooldown, reloadTimer, reloadDuration = 1, recoil, flashTimer;

    protected Weapon(String name, int slot, int magSize, int baseReserve, float fireRate, float reloadTime,
                     float recoilKick, float shake, float tipLength, String sound) {
        this.name = name; this.slot = slot; this.magSize = magSize; this.baseReserve = baseReserve; this.fireRate = fireRate;
        this.reloadTime = reloadTime; this.recoilKick = recoilKick; this.shake = shake; this.tipLength = tipLength; this.sound = sound;
        this.mag = magSize; this.reserve = baseReserve;
    }

    // ------------------------------------------------------------------ abstract / overridable behaviour
    /** What leaves the barrel. Every weapon is different (POLYMORPHISM). */
    protected abstract void fireProjectiles(GameWorld w, float x, float y, float aim, float damageMul, int shotId);
    /** Draws the gun in the player's hands (local coordinates, barrel pointing to +X). */
    public abstract void drawModel(Graphics2D g);
    /** Creates a fresh instance of the same weapon type (used by {@link #copy()}). */
    protected abstract Weapon newInstance();

    public boolean hasInfiniteAmmo() { return false; }
    protected boolean countsForAccuracy() { return true; }
    protected float soundVolume() { return 0.8f; }

    protected void muzzleEffects(GameWorld w, Player shooter, float x, float y, float aim) {
        float ca = (float) Math.cos(aim), sa = (float) Math.sin(aim);
        for (int i = 0; i < 4; i++)
            w.particles().spawn(ParticleType.FIRE, x, y, ca * Util.rf(60, 220) + Util.rf(-40, 40), sa * Util.rf(60, 220) + Util.rf(-40, 40), 0.09f, 4, new Color(255, 220, 120));
        w.particles().spawn(ParticleType.SMOKE, x, y, ca * 30, sa * 30, 0.5f, 4, new Color(160, 160, 160, 90));
        deadsignal.gfx.Particle shell = w.particles().spawn(ParticleType.SHELL, shooter.getX() + sa * 6, shooter.getY() - ca * 6,
                -sa * Util.rf(60, 120) + Util.rf(-20, 20), ca * Util.rf(60, 120), 0.6f, 2, new Color(210, 170, 70));
        if (shell != null) shell.withSpin(Util.rf(-20, 20));
        w.addFlash(x, y, 150, 0.06f, new Color(255, 200, 100));
    }

    /** Helper for bullet weapons. */
    protected final void fireBullet(GameWorld w, float x, float y, float angle, float speed, float life, float dmg, float knockback, int pierce, int shotId) {
        w.addProjectile(new Bullet(x, y, angle, speed * Util.rf(0.9f, 1.1f), life * Util.rf(0.85f, 1.1f), dmg, knockback, pierce, shotId));
    }

    // ------------------------------------------------------------------ template method
    public final boolean trigger(Player shooter, GameWorld w, float damageMul, float rateMul, float reloadMul) {
        if (cooldown > 0 || isReloading()) return false;
        if (mag <= 0) {
            if (canReload()) startReload(reloadMul, w.audio());
            else { w.audio().play("empty"); cooldown = 0.3f; w.addText("NO AMMO", shooter.getX(), shooter.getY() - 26, new Color(255, 120, 100), 12); }
            return false;
        }
        float aim = shooter.getAim(), ca = (float) Math.cos(aim), sa = (float) Math.sin(aim);
        float mx = shooter.getX() + ca * tipLength, my = shooter.getY() + sa * tipLength;
        if (w.getMap().isSolidAt(mx, my)) { mx = shooter.getX() + ca * 8; my = shooter.getY() + sa * 8; }
        fireProjectiles(w, mx, my, aim, damageMul, w.nextShotId());
        mag--;
        cooldown = 1f / (fireRate * rateMul);
        recoil = recoilKick;
        flashTimer = 0.05f;
        w.shake(shake);
        if (countsForAccuracy()) w.stats().addShot();
        muzzleEffects(w, shooter, mx, my, aim);
        w.audio().play(sound, soundVolume(), 1f);
        return true;
    }

    public void update(float dt) {
        cooldown -= dt; flashTimer -= dt;
        recoil = Math.max(0, recoil - dt * 40);
        if (reloadTimer > 0) { reloadTimer -= dt; if (reloadTimer <= 0) finishReload(); }
    }

    public void requestReload(float speedMul, Audio audio) {
        if (!isReloading() && mag < magSize && canReload()) startReload(speedMul, audio);
    }
    private boolean canReload() { return hasInfiniteAmmo() || reserve > 0; }
    private void startReload(float speedMul, Audio audio) {
        reloadDuration = reloadTime / speedMul;
        reloadTimer = reloadDuration;
        audio.play("reload");
    }
    private void finishReload() {
        if (hasInfiniteAmmo()) { mag = magSize; return; }
        int take = Math.min(magSize - mag, reserve);
        mag += take; reserve -= take;
    }

    public void onEquip() { reloadTimer = 0; cooldown = 0.25f; }
    public int maxReserve(float capacityMul) { return (int) (baseReserve * capacityMul); }
    public void refill(float capacityMul) { mag = magSize; reserve = maxReserve(capacityMul); }
    /** @return false if this weapon does not use reserve ammo. */
    public boolean addReserve(float magazines, float capacityMul) {
        if (hasInfiniteAmmo()) return false;
        reserve = Math.min(maxReserve(capacityMul), reserve + (int) (magSize * magazines));
        return true;
    }
    public final Weapon copy() { Weapon c = newInstance(); c.mag = mag; c.reserve = reserve; return c; }

    // ------------------------------------------------------------------ read-only state
    public String getName() { return name; }
    public int getSlot() { return slot; }
    public int getMag() { return mag; }
    public int getMagSize() { return magSize; }
    public int getReserve() { return reserve; }
    public boolean isReloading() { return reloadTimer > 0; }
    public float getReloadProgress() { return isReloading() ? 1 - reloadTimer / reloadDuration : 0; }
    public float getRecoilOffset() { return recoil; }
    public float getTipLength() { return tipLength; }
    public boolean showMuzzleFlash() { return flashTimer > 0; }
    public float getCrosshairSpread() { return 6 + recoil * 2; }
}
