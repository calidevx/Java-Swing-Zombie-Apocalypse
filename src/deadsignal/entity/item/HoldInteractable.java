package deadsignal.entity.item;

import deadsignal.core.Util;
import deadsignal.entity.Interactable;
import deadsignal.entity.Player;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;
import deadsignal.gfx.Fonts;
import deadsignal.gfx.ParticleType;

import java.awt.*;
import java.awt.geom.Arc2D;

/** An objective the player activates by holding E for a few seconds. */
public abstract class HoldInteractable extends ObjectiveItem implements Interactable {
    private final float holdTime;
    private final String verb;
    private float progress;
    private boolean heldThisFrame, playerNear;

    protected HoldInteractable(float x, float y, float holdTime, String verb) {
        super(x, y, 16);
        this.holdTime = holdTime; this.verb = verb;
    }

    @Override public boolean canInteract(Player p) { return !isCompleted() && distanceTo(p) < p.getInteractRange(); }

    @Override public void interact(GameWorld w, float dt) {
        heldThisFrame = true;
        progress += dt / holdTime;
        if (Util.rf() < 0.3f)
            w.particles().spawn(ParticleType.SPARK, getX() + Util.rf(-8, 8), getY() + Util.rf(-8, 8), Util.rf(-60, 60), Util.rf(-60, 60), 0.25f, 2, new Color(255, 230, 120));
        if (progress >= 1) { progress = 1; complete(w); }
    }

    @Override protected void updateActive(GameWorld w, float dt) {
        if (!heldThisFrame) progress = Math.max(0, progress - dt);
        heldThisFrame = false;
        playerNear = canInteract(w.getPlayer());
    }

    /** Subclasses draw the object; this class adds the progress ring and prompt. */
    protected abstract void renderBody(Graphics2D g, Assets a, float pulse);

    @Override
    protected final void renderItem(Graphics2D g, Assets a, float pulse) {
        renderBody(g, a, pulse);
        if (!playerNear || isCompleted()) return;
        g.setColor(new Color(0, 0, 0, 160)); g.setStroke(new BasicStroke(5f)); Util.ring(g, getX(), getY(), 28, 28);
        g.setColor(new Color(120, 255, 150)); g.setStroke(new BasicStroke(4f));
        g.draw(new Arc2D.Double(getX() - 28, getY() - 28, 56, 56, 90, -360 * progress, Arc2D.OPEN));
        g.setFont(Fonts.SMALL);
        Util.ctext(g, "Hold [E] to " + verb, (int) getX(), (int) getY() + 46, Color.WHITE);
    }
}
