package deadsignal.entity.boss;

import java.awt.Color;

/** Immutable description of one boss health/status bar for the HUD. */
public final class BossBar {
    private final String label;
    private final float fraction;
    private final Color top, bottom;

    public BossBar(String label, float fraction, Color top, Color bottom) {
        this.label = label; this.fraction = Math.max(0, Math.min(1, fraction)); this.top = top; this.bottom = bottom;
    }

    public String getLabel() { return label; }
    public float getFraction() { return fraction; }
    public Color getTop() { return top; }
    public Color getBottom() { return bottom; }
}
