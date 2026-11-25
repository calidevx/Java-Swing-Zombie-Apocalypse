package deadsignal.world;

import deadsignal.gfx.LightEmitter;

import java.awt.Color;

/** A static light placed in the map (street lamp, fire, toxic glow...). Implements the LightEmitter interface. */
public final class MapLight implements LightEmitter {
    public enum Style { STEADY, FLICKER, PULSE }
    private static final Color[] COLORS = {new Color(255, 220, 150), new Color(255, 120, 40), new Color(120, 255, 120), new Color(150, 200, 255),
            new Color(255, 90, 210), new Color(90, 220, 255), new Color(255, 210, 80)};

    private final float x, y, radius;
    private final Style style;
    private final int colorIndex;
    private final boolean glows;

    MapLight(float x, float y, float radius, Style style, int colorIndex, boolean glows) {
        this.x = x; this.y = y; this.radius = radius; this.style = style; this.colorIndex = colorIndex; this.glows = glows;
    }

    @Override public float getLightX() { return x; }
    @Override public float getLightY() { return y; }
    @Override public float getLightRadius() { return radius; }

    @Override public float getLightIntensity(float t) {
        switch (style) {
            case FLICKER: return Math.sin(t * 13 + x) * Math.sin(t * 7.3 + y) > 0.6 ? 0.15f : 0.85f;
            case PULSE: return 0.75f + 0.2f * (float) Math.sin(t * 9 + x);
            default: return 0.9f;
        }
    }

    @Override public Color getGlowColor() { return glows ? COLORS[colorIndex] : null; }
    @Override public int getGlowAlpha() { return colorIndex == 1 ? 50 : colorIndex >= 4 ? 55 : 26; }
}
