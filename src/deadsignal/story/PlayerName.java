package deadsignal.story;

/** The hero's name, chosen on the name-entry screen. One place to read it, one place to set it. */
public final class PlayerName {
    public static final String DEFAULT = "Kai Navarro";
    public static final int MAX_LENGTH = 14;

    private static String name = DEFAULT;

    private PlayerName() { }

    /** Name exactly as typed (used inside dialogue text). */
    public static String get() { return name; }
    /** Upper-case version (used for the HUD and speaker labels). */
    public static String getUpper() { return name.toUpperCase(); }

    public static void set(String typed) {
        String t = typed == null ? "" : typed.trim();
        if (t.length() > MAX_LENGTH) t = t.substring(0, MAX_LENGTH).trim();
        name = t.isEmpty() ? DEFAULT : t;
    }
}
