package deadsignal.entity.item;

import deadsignal.entity.Entity;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;
import deadsignal.gfx.LightEmitter;

import java.awt.*;

/**
 * ABSTRACT mission object (radio, fuel, survivor, hive node, exit...).
 * Handles the shared "completed -> fade out -> notify the mission" life-cycle.
 */
public abstract class ObjectiveItem extends Entity implements LightEmitter {
    private boolean completed;
    private float fade = 1f, anim;

    protected ObjectiveItem(float x, float y, float radius) {
        super(x, y, radius);
        anim = (float) (Math.random() * 6);
    }

    @Override
    public final void update(GameWorld w, float dt) {
        anim += dt;
        if (completed) { fade -= dt * 1.5f; if (fade <= 0) remove(); return; }
        updateActive(w, dt);
    }

    /** Hook: per-frame logic while not yet completed. */
    protected void updateActive(GameWorld w, float dt) { }
    /** Hook: reward / effects when completed. */
    protected void onCompleted(GameWorld w) { }
    /** Draws the item itself (fade is applied by the caller). */
    protected abstract void renderItem(Graphics2D g, Assets a, float pulse);

    /** Marks this objective done exactly once and tells the world. */
    protected final void complete(GameWorld w) {
        if (completed) return;
        completed = true;
        onCompleted(w);
        w.onObjectiveItemCompleted(this);
    }

    @Override
    public final void render(Graphics2D g, Assets a) {
        if (fade <= 0) return;
        Composite oc = g.getComposite();
        if (fade < 1) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, fade)));
        renderItem(g, a, (float) Math.sin(anim * 4) * 0.5f + 0.5f);
        g.setComposite(oc);
    }

    public boolean isCompleted() { return completed; }
    protected float getAnim() { return anim; }
    /** Colour of this objective's blip on the minimap. */
    public Color getMinimapColor() { return new Color(255, 220, 80); }

    @Override public float getLightX() { return getX(); }
    @Override public float getLightY() { return getY(); }
    @Override public float getLightRadius() { return 80; }
    @Override public float getLightIntensity(float t) { return completed ? 0 : 0.7f; }
}
