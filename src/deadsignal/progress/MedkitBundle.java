package deadsignal.progress;

import java.awt.*;

/** Consumable: one medkit. */
public final class MedkitBundle extends ShopItem {
    public MedkitBundle() { super("MEDKIT", "Heals 60 HP (Q)"); }

    @Override public int getCost(PlayerProfile p) { return 110; }
    @Override public boolean isAvailable(PlayerProfile p) { return p.getMedkits() < PlayerProfile.MAX_MEDKITS; }
    @Override protected void applyTo(PlayerProfile p) { p.addMedkits(1); }
    @Override public String getStatusText(PlayerProfile p) { return "Owned: " + p.getMedkits() + " / " + PlayerProfile.MAX_MEDKITS; }

    @Override public void drawIcon(Graphics2D g, int x, int y) {
        g.setColor(new Color(235, 235, 230)); g.fillRoundRect(x - 11, y - 9, 22, 18, 4, 4);
        g.setColor(new Color(210, 30, 30)); g.fillRect(x - 3, y - 7, 6, 14); g.fillRect(x - 7, y - 3, 14, 6);
    }
}
