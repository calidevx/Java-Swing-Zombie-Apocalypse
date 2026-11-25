package deadsignal.progress;

import deadsignal.core.Util;

import java.awt.*;

/** Permanently raises one {@link Stat} by a level. */
public final class StatUpgrade extends ShopItem {
    private final Stat stat;

    public StatUpgrade(Stat stat) { super(stat.getTitle(), stat.getDescription()); this.stat = stat; }

    @Override public int getCost(PlayerProfile p) { return stat.costForLevel(p.getLevel(stat)); }
    @Override public boolean isAvailable(PlayerProfile p) { return p.canUpgrade(stat); }
    @Override protected void applyTo(PlayerProfile p) { p.raiseLevel(stat); }
    @Override public int getLevel(PlayerProfile p) { return p.getLevel(stat); }

    @Override public void drawIcon(Graphics2D g, int x, int y) {
        switch (stat) {
            case VITALITY: g.setColor(new Color(220, 50, 60)); Util.oval(g, x - 5, y - 3, 7, 7); Util.oval(g, x + 5, y - 3, 7, 7);
                g.fillPolygon(new int[]{x - 12, x + 12, x}, new int[]{y, y, y + 13}, 3); break;
            case FIREPOWER: g.setColor(new Color(255, 170, 60)); g.fillPolygon(new int[]{x - 3, x + 7, x + 1, x + 9, x - 7, x}, new int[]{y - 14, y - 14, y - 2, y - 2, y + 14, y}, 6); break;
            case TRIGGER: g.setColor(new Color(200, 200, 210)); g.setStroke(new BasicStroke(3f)); Util.ring(g, x, y, 11, 11); Util.line(g, x, y, x + 6, y - 6); break;
            case AGILITY: g.setColor(new Color(120, 200, 255)); for (int k = 0; k < 3; k++) g.fillPolygon(new int[]{x - 12 + k * 8, x - 4 + k * 8, x - 12 + k * 8}, new int[]{y - 9, y, y + 9}, 3); break;
            case KEVLAR: g.setColor(new Color(150, 160, 180)); g.fillPolygon(new int[]{x - 11, x + 11, x + 9, x, x - 9}, new int[]{y - 12, y - 12, y + 3, y + 13, y + 3}, 5); break;
            case AMMO_BELT: g.setColor(new Color(220, 190, 70)); for (int k = 0; k < 4; k++) g.fillRoundRect(x - 12 + k * 6, y - 10, 4, 20, 3, 3); break;
        }
    }
}
