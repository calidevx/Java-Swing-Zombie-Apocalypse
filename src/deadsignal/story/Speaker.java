package deadsignal.story;

import java.awt.Color;

/** The cast of DEAD SIGNAL. */
public enum Speaker {
    NARRATOR("", new Color(230, 230, 230)),
    KAI("KAI NAVARRO", new Color(110, 170, 255)),
    LENA("DR. LENA ORTIZ", new Color(120, 235, 170)),
    GRAVES("COL. GRAVES", new Color(225, 185, 90)),
    VOSS("THE SHEPHERD", new Color(210, 120, 255)),
    RADIO("RADIO", new Color(190, 190, 190)),
    SURVIVOR("SURVIVOR", new Color(245, 205, 150)),
    TIP("FIELD TIP", new Color(160, 200, 220)),
    SYSTEM("BIOGEN SYSTEM", new Color(255, 90, 80));

    private final String displayName;
    private final Color color;

    Speaker(String displayName, Color color) { this.displayName = displayName; this.color = color; }

    public String getDisplayName() { return this == KAI ? PlayerName.getUpper() : displayName; }
    public Color getColor() { return color; }
}
