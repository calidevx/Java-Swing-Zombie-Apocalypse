package deadsignal.entity.item;

import deadsignal.core.Util;
import deadsignal.entity.Player;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;
import deadsignal.gfx.ItemSprite;

import java.awt.*;

/** Picked up automatically by walking over it. */
public final class FuelCanister extends ObjectiveItem {
    public FuelCanister(float x, float y) { super(x, y, 14); }

    @Override protected void updateActive(GameWorld w, float dt) {
        Player p = w.getPlayer();
        if (!p.isDead() && distanceTo(p) < p.getRadius() + 16) complete(w);
    }

    @Override protected void onCompleted(GameWorld w) {
        w.addText("FUEL SECURED", getX(), getY() - 20, new Color(255, 200, 100), 15);
        w.audio().play("pickup", 1f, 0.8f);
    }

    @Override protected void renderItem(Graphics2D g, Assets a, float pulse) {
        g.setColor(new Color(255, 220, 90, (int) (40 + 60 * pulse))); Util.oval(g, getX(), getY(), 24 + pulse * 4, 24 + pulse * 4);
        float bob = (float) Math.sin(getAnim() * 3) * 3;
        g.drawImage(a.item(ItemSprite.FUEL), (int) getX() - 20, (int) (getY() - 20 + bob), null);
    }
}
