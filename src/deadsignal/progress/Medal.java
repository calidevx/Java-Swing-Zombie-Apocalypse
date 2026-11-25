package deadsignal.progress;

/** Achievements. Each one pays a cash bonus when first earned. */
public enum Medal {
    FIRST_BLOOD("FIRST BLOOD", "Kill your first infected"),
    CENTURION("CENTURION", "100 kills"),
    BODY_COUNT("BODY COUNT", "300 kills"),
    UNSTOPPABLE("UNSTOPPABLE", "Reach a x15 kill combo"),
    DEMOLITION("DEMOLITION", "5+ kills with one explosion"),
    UNTOUCHABLE("UNTOUCHABLE", "Defeat a boss without taking damage"),
    SHARPSHOOTER("SHARPSHOOTER", "Finish a chapter with 65%+ accuracy"),
    IRON_SKIN("IRON SKIN", "Finish a chapter taking under 40 damage"),
    FIELD_MEDIC("FIELD MEDIC", "Use 3 medkits"),
    PYROMANIAC("PYROMANIAC", "40 flamethrower kills"),
    ENCORE("ENCORE", "Defeat the Maestro in the bonus stage"),
    DEAD_SIGNAL("DEAD SIGNAL", "Finish the story");

    public static final int CASH_BONUS = 100;
    private final String title, description;

    Medal(String title, String description) { this.title = title; this.description = description; }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
}
