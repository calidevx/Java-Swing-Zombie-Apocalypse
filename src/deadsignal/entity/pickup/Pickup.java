package deadsignal.entity.pickup;

import deadsignal.core.Util;
import deadsignal.entity.Entity;
import deadsignal.entity.Player;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;
import deadsignal.gfx.PickupSprite;

import java.awt.*;

/**
 * ABSTRACT loot drop. Shared behaviour (pop-out, magnet, despawn, bobbing) lives here;
 * each subclass only says what icon it has and what it does when collected.
 */
public abstract class Pickup extends Entity {
    private static final float LIFETIME = 25f;
    private float vx, vy, age;

    protected Pickup(float x, float y) {
        super(x, y, 10);
        float a = Util.rf(0, 6.28f), s = Util.rf(40, 120);
        vx = (float) Math.cos(a) * s; vy = (float) Math.sin(a) * s;
    }

    protected abstract PickupSprite getSprite();
    protected abstract void applyTo(Player player, GameWorld world);

    @Override
    public final void update(GameWorld w, float dt) {
        age += dt;
        float drag = Math.min(1, dt * 5); vx -= vx * drag; vy -= vy * drag;
        Player p = w.getPlayer();
        float d = distanceTo(p);
        float nx = getX() + vx * dt, ny = getY() + vy * dt;
        float radius = p.getMagnetRadius();
        if (!p.isDead() && d < radius && age > 0.25f) {
            float pull = radius > 200 ? 520 + 400 * (d / radius) : 420 * (1 - d / 130);
            nx += (p.getX() - getX()) / Math.max(1, d) * pull * dt;
            ny += (p.getY() - getY()) / Math.max(1, d) * pull * dt;
        }
        setPosition(nx, ny);
        if (!p.isDead() && d < p.getRadius() + 12 && age > 0.25f) { applyTo(p, w); remove(); return; }
        if (age > LIFETIME) remove();
    }

    @Override
    public void render(Graphics2D g, Assets a) {
        if (age > LIFETIME - 5 && ((int) (age * 8)) % 2 == 0) return;
        float bob = (float) Math.sin(age * 5 + getX()) * 2;
        g.setColor(new Color(0, 0, 0, 70)); Util.oval(g, getX(), getY() + 7, 7, 3);
        g.drawImage(a.pickup(getSprite()), (int) getX() - 12, (int) (getY() - 12 + bob), null);
    }
}
