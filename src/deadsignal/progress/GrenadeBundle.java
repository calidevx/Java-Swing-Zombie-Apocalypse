package deadsignal.progress;

import deadsignal.core.Util;

import java.awt.*;

/** Consumable: two frag grenades. */
public final class GrenadeBundle extends ShopItem {
    public GrenadeBundle() { super("GRENADES x2", "Two frag grenades (G)"); }

    @Override public int getCost(PlayerProfile p) { return 90; }
    @Override public boolean isAvailable(PlayerProfile p) { return p.getGrenades() < PlayerProfile.MAX_GRENADES; }
    @Override protected void applyTo(PlayerProfile p) { p.addGrenades(2); }
    @Override public String getStatusText(PlayerProfile p) { return "Owned: " + p.getGrenades() + " / " + PlayerProfile.MAX_GRENADES; }

    @Override public void drawIcon(Graphics2D g, int x, int y) {
        g.setColor(new Color(60, 80, 46)); Util.oval(g, x, y + 2, 8, 10);
        g.setColor(new Color(160, 160, 160)); g.fillRect(x - 3, y - 11, 6, 4); Util.ring(g, x + 5, y - 10, 3, 3);
    }
}
