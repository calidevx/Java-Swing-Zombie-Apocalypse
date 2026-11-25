package deadsignal.entity.ability;

import deadsignal.core.Util;
import deadsignal.entity.Creature;
import deadsignal.entity.DamageType;
import deadsignal.entity.Player;
import deadsignal.entity.projectile.PoisonDart;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;
import deadsignal.gfx.ParticleType;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * Graves's deployable poison turret. INHERITANCE: it is a Creature (has health, can be shot)
 * but it never moves - it overrides update/render/onDeath with turret behaviour.
 */
public final class ToxinSentry extends Creature {
    private static final float LIFETIME = 18f, RANGE = 560;
    private float age, fireTimer = 1.2f, losTimer, headAngle;
    private boolean seesPlayer;

    public ToxinSentry(float x, float y, float hpMultiplier) { super(x, y, 14, 240 * hpMultiplier); }

    @Override
    public void update(GameWorld w, float dt) {
        tickFlash(dt);
        age += dt; fireTimer -= dt; losTimer -= dt;
        if (age > LIFETIME) { kill(w, DamageType.PURGE); return; }
        Player p = w.getPlayer();
        float d = distanceTo(p);
        if (losTimer <= 0) { losTimer = 0.3f; seesPlayer = d < RANGE && w.getMap().hasLineOfSight(getX(), getY(), p.getX(), p.getY()); }
        if (!seesPlayer || p.isDead() || age < 0.5f) return;
        headAngle = Util.turn(headAngle, angleTo(p), 5 * dt);
        if (fireTimer <= 0) {
            fireTimer = 1.3f;
            float mx = getX() + (float) Math.cos(headAngle) * 16, my = getY() + (float) Math.sin(headAngle) * 16;
            w.addProjectile(new PoisonDart(mx, my, headAngle + Util.gauss() * 0.05f));
            w.audio().play("zap", 0.35f, 1.6f);
        }
    }

    @Override protected void onDeath(GameWorld w, DamageType type) {
        remove();
        for (int i = 0; i < 16; i++) w.particles().spawn(ParticleType.DOT, getX(), getY(), Util.rf(-160, 160), Util.rf(-160, 160), 0.6f, 3, new Color(140, 230, 70));
        for (int i = 0; i < 6; i++) w.particles().spawn(ParticleType.SMOKE, getX(), getY(), Util.rf(-40, 40), Util.rf(-40, 40), 0.8f, 10, new Color(70, 90, 60, 150));
        w.audio().play("explode", 0.4f, 1.8f);
        if (type != DamageType.PURGE) { w.getProfile().addCash(30); w.addText("SENTRY DOWN  +$30", getX(), getY() - 26, new Color(170, 240, 120), 14); }
    }

    @Override
    public void render(Graphics2D g, Assets a) {
        float x = getX(), y = getY(), grow = Math.min(1, age / 0.4f);
        AffineTransform at = g.getTransform();
        g.translate(x, y); g.scale(grow, grow);
        g.setColor(new Color(0, 0, 0, 90)); Util.oval(g, 3, 5, 16, 12);
        g.setColor(new Color(60, 64, 58)); g.setStroke(Util.st(3));
        for (int k = 0; k < 3; k++) { double an = k * 2.094 + 0.5; Util.line(g, 0, 0, Math.cos(an) * 17, Math.sin(an) * 17); }
        g.setColor(isFlashing() ? Color.WHITE : new Color(84, 90, 70)); Util.oval(g, 0, 0, 11, 11);
        g.rotate(headAngle);
        g.setColor(isFlashing() ? Color.WHITE : new Color(66, 72, 56)); g.fillRoundRect(-8, -7, 18, 14, 5, 5);
        g.setColor(new Color(40, 42, 38)); g.fillRect(8, -2, 11, 4);
        g.setPaint(new GradientPaint(-6, -10, new Color(170, 250, 90), -6, -4, new Color(60, 140, 30))); g.fillRoundRect(-7, -12, 10, 6, 3, 3);
        boolean blink = ((int) (age * 4)) % 2 == 0;
        g.setColor(blink ? new Color(255, 60, 40) : new Color(90, 20, 15)); Util.oval(g, -3, 3, 2, 2);
        g.setTransform(at);
        g.setColor(new Color(0, 0, 0, 150)); g.fillRect((int) x - 14, (int) y - 24, 28, 4);
        g.setColor(new Color(140, 230, 80)); g.fillRect((int) x - 14, (int) y - 24, (int) (28 * getHealthFraction()), 4);
        g.setColor(new Color(255, 255, 255, 120)); g.fillRect((int) x - 14, (int) y - 19, (int) (28 * (1 - age / LIFETIME)), 1);
    }
}
