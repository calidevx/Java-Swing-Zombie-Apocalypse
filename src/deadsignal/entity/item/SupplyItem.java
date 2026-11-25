package deadsignal.entity.item;

import deadsignal.core.Util;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;
import deadsignal.gfx.ItemSprite;
import deadsignal.gfx.ParticleType;

import java.awt.*;

/** A hold-to-use object drawn from an icon (radio, generator, drive...). */
public abstract class SupplyItem extends HoldInteractable {
    private final ItemSprite sprite;
    private final String doneLabel;

    protected SupplyItem(float x, float y, ItemSprite sprite, float holdTime, String verb, String doneLabel) {
        super(x, y, holdTime, verb);
        this.sprite = sprite; this.doneLabel = doneLabel;
    }

    @Override protected void onCompleted(GameWorld w) {
        w.audio().play("objective", 0.8f, 1.2f);
        w.addText(doneLabel, getX(), getY() - 24, new Color(255, 230, 120), 14);
        for (int i = 0; i < 20; i++) w.particles().spawn(ParticleType.GLOW, getX(), getY(), Util.rf(-90, 90), Util.rf(-90, 90), 0.7f, 3, new Color(255, 230, 140));
    }

    @Override protected void renderBody(Graphics2D g, Assets a, float pulse) {
        g.setColor(new Color(255, 220, 90, (int) (40 + 60 * pulse))); Util.oval(g, getX(), getY(), 24 + pulse * 4, 24 + pulse * 4);
        g.setColor(new Color(255, 230, 120, 200)); g.setStroke(new BasicStroke(1.5f)); Util.ring(g, getX(), getY(), 20 + pulse * 3, 20 + pulse * 3);
        g.drawImage(a.item(sprite), (int) getX() - 20, (int) getY() - 20, null);
    }
}
