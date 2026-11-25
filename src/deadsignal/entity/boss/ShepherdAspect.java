package deadsignal.entity.boss;

import deadsignal.core.Util;
import deadsignal.entity.Creature;
import deadsignal.entity.DamageType;
import deadsignal.entity.Player;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;
import deadsignal.gfx.BossSprite;
import deadsignal.gfx.Fonts;
import deadsignal.gfx.ParticleType;

import java.awt.*;

/**
 * ABSTRACTION + INHERITANCE: one of the three bodies the Shepherd splits into ("THE FLOCK DIVIDES!").
 * The base class owns the shared life cycle (movement, contact damage, drawing, reporting its death to
 * the Shepherd); each subclass supplies its own attack through {@link #attack} - polymorphism.
 */
public abstract class ShepherdAspect extends Creature {
    private final Shepherd parent;
    private final String title;
    private final Color tint;
    private float anim, angle, contactCooldown, spawnTimer = 0.8f;

    protected ShepherdAspect(Shepherd parent, String title, Color tint, float x, float y, float radius, float hp) {
        super(x, y, radius, hp);
        this.parent = parent; this.title = title; this.tint = tint;
    }

    /** Each aspect fights differently. */
    protected abstract void attack(GameWorld w, Player p, float dt, float dx, float dy, float d);
    /** Preferred movement speed and fighting distance. */
    protected abstract float getSpeed();
    protected abstract float getPreferredRange();
    protected float getContactDamage() { return 14; }

    @Override
    public final void update(GameWorld w, float dt) {
        tickFlash(dt);
        anim += dt * 6; contactCooldown -= dt;
        if (spawnTimer > 0) { spawnTimer -= dt; return; }
        Player p = w.getPlayer();
        if (p.isDead()) return;
        float dx = p.getX() - getX(), dy = p.getY() - getY(), d = Math.max(1, Util.len(dx, dy));
        angle = Util.turn(angle, (float) Math.atan2(dy, dx), 5 * dt);
        float range = getPreferredRange(), dir = d > range + 30 ? 1 : d < range - 30 ? -1 : 0;
        float sx = -dy / d * 0.5f, sy = dx / d * 0.5f;              // slight strafe so they circle
        moveTo(w, getX() + (dx / d * dir + sx) * getSpeed() * dt, getY() + (dy / d * dir + sy) * getSpeed() * dt);
        attack(w, p, dt, dx, dy, d);
        if (d < getRadius() + p.getRadius() && contactCooldown <= 0) { p.hurt(getContactDamage(), getX(), getY(), DamageType.MELEE, w); contactCooldown = 0.8f; }
    }

    /** Aspects float over walls like the Shepherd, but stay inside the map. */
    protected final void moveTo(GameWorld w, float nx, float ny) {
        float maxX = w.getMap().getWidth() * 32 - 40, maxY = w.getMap().getHeight() * 32 - 40;
        setPosition(Util.clamp(nx, 40, maxX), Util.clamp(ny, 40, maxY));
    }

    @Override public boolean isTargetable() { return !isDead() && spawnTimer <= 0; }

    @Override
    protected void onDeath(GameWorld w, DamageType type) {
        remove();
        for (int i = 0; i < 30; i++) w.particles().spawn(ParticleType.GLOW, getX(), getY(), Util.rf(-220, 220), Util.rf(-220, 220), 0.7f, 4, tint);
        w.particles().spawn(ParticleType.RING, getX(), getY(), 0, 0, 0.4f, 90, tint);
        w.audio().play("roar", 0.7f, 1.5f);
        w.shake(10);
        if (type != DamageType.PURGE) {
            w.addText(title + " FALLS", getX(), getY() - 50, tint, 18);
            parent.onAspectDown(w, this);
        }
    }

    @Override
    public void render(Graphics2D g, Assets a) {
        float x = getX(), y = getY(), grow = spawnTimer > 0 ? 1 - spawnTimer / 0.8f : 1;
        float r = getRadius() * 2.2f;
        g.setPaint(new RadialGradientPaint(x, y, r, new float[]{0, 1}, new Color[]{new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), 120), new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), 0)}));
        Util.oval(g, x, y, r, r);
        Util.drawRotated(g, a.boss(BossSprite.SHEPHERD, (int) anim, false, isFlashing()), x, y, angle, 0.62f * grow);
        g.setColor(tint); g.setStroke(new BasicStroke(2f)); Util.ring(g, x, y, getRadius() + 4, getRadius() + 4);
        drawMarks(g, x, y);
        g.setFont(Fonts.TINY); Util.ctext(g, title, (int) x, (int) (y - getRadius() - 14), tint);
        float hf = getHealthFraction();
        g.setColor(new Color(0, 0, 0, 160)); g.fillRect((int) x - 30, (int) (y - getRadius() - 10), 60, 5);
        g.setColor(tint); g.fillRect((int) x - 30, (int) (y - getRadius() - 10), (int) (60 * hf), 5);
    }

    /** Hook: subclass decorations (telegraphs, orbiting bones...). */
    protected void drawMarks(Graphics2D g, float x, float y) { }

    protected final float getAngle() { return angle; }
    protected final float getAnim() { return anim; }
    public final String getTitle() { return title; }
    public final Color getTint() { return tint; }
}
