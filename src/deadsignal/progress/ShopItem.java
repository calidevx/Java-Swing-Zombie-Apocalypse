package deadsignal.progress;

import java.awt.Graphics2D;

/**
 * ABSTRACT shop entry. {@link #purchase} is the same for every item; the subclasses decide
 * price, availability, effect and icon (POLYMORPHISM: the shop screen never needs to know which kind it is).
 */
public abstract class ShopItem {
    public enum Result { PURCHASED, MAXED_OUT, NOT_ENOUGH_CASH, LOCKED }

    private final String name, description;

    protected ShopItem(String name, String description) { this.name = name; this.description = description; }

    public abstract int getCost(PlayerProfile p);
    /** Hook: items that are not for sale yet (skills before the Bonus Stage). */
    public boolean isLocked(PlayerProfile p) { return false; }
    public abstract boolean isAvailable(PlayerProfile p);
    protected abstract void applyTo(PlayerProfile p);
    public abstract void drawIcon(Graphics2D g, int x, int y);
    /** Text under the description (level pips are drawn separately when {@link #getLevel} >= 0). */
    public String getStatusText(PlayerProfile p) { return ""; }
    /** Current level for upgrade items, or -1 for consumables. */
    public int getLevel(PlayerProfile p) { return -1; }

    public final Result purchase(PlayerProfile p) {
        if (isLocked(p)) return Result.LOCKED;
        if (!isAvailable(p)) return Result.MAXED_OUT;
        if (!p.spend(getCost(p))) return Result.NOT_ENOUGH_CASH;
        applyTo(p);
        return Result.PURCHASED;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
}
