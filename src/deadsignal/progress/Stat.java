package deadsignal.progress;

/** Upgradeable character stats sold in the shop. */
public enum Stat {
    VITALITY("VITALITY", "+20 max health", 120),
    FIREPOWER("FIREPOWER", "+12% weapon damage", 150),
    TRIGGER("TRIGGER", "+10% fire rate", 130),
    AGILITY("AGILITY", "+7% move speed", 110),
    KEVLAR("KEVLAR", "-8% damage taken", 140),
    AMMO_BELT("AMMO BELT", "+25% ammo capacity", 100);

    /** Number of level pips drawn on the shop card (levels themselves are uncapped). */
    public static final int PIPS = 5;
    private final String title, description;
    private final int baseCost;

    Stat(String title, String description, int baseCost) { this.title = title; this.description = description; this.baseCost = baseCost; }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    /** Prices keep climbing so cash always has a use: base x (level+1) x (1 + 12% per level). */
    public int costForLevel(int currentLevel) { return Math.round(baseCost * (currentLevel + 1) * (1 + 0.12f * currentLevel)); }
}
