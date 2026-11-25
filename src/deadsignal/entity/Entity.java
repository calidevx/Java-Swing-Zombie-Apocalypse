package deadsignal.entity;

import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;
import deadsignal.world.GameMap;

import java.awt.Graphics2D;

/**
 * ROOT OF THE ENTITY HIERARCHY (abstraction + inheritance).
 * Every object that lives in the world has a position, a size, and knows how to
 * update and draw itself. Subclasses MUST provide {@link #update} and {@link #render}.
 * Position is private: subclasses move through {@link #setPosition} / {@link #moveWithCollision}.
 */
public abstract class Entity {
    private float x, y, radius;
    private boolean removed;

    protected Entity(float x, float y, float radius) {
        this.x = x; this.y = y; this.radius = radius;
    }

    public abstract void update(GameWorld world, float dt);
    public abstract void render(Graphics2D g, Assets assets);

    public final float getX() { return x; }
    public final float getY() { return y; }
    public final float getRadius() { return radius; }
    protected void setRadius(float r) { radius = r; }
    protected void setPosition(float nx, float ny) { x = nx; y = ny; }

    public boolean isRemoved() { return removed; }
    public void remove() { removed = true; }

    public float distanceTo(Entity o) { return distanceTo(o.x, o.y); }
    public float distanceTo(float px, float py) { float dx = px - x, dy = py - y; return (float) Math.sqrt(dx * dx + dy * dy); }
    public float angleTo(Entity o) { return (float) Math.atan2(o.y - y, o.x - x); }

    /** Moves with axis-separated wall sliding. @return bit 1 = blocked on X, bit 2 = blocked on Y */
    protected final int moveWithCollision(GameMap map, float dx, float dy, float r) {
        int blocked = 0;
        if (dx != 0) { float nx = x + dx; if (!map.collides(nx, y, r)) x = nx; else blocked |= 1; }
        if (dy != 0) { float ny = y + dy; if (!map.collides(x, ny, r)) y = ny; else blocked |= 2; }
        return blocked;
    }
}
