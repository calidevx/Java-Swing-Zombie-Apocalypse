package deadsignal.gfx;

import deadsignal.core.Util;

import java.awt.*;

/** Rising, fading combat text such as "+$12" or "STUNNED!". */
public final class FloatingText {
    private final String text;
    private final Color color;
    private final Font font;
    private float x, y, timeLeft;

    public FloatingText(String text, float x, float y, Color color, int size) {
        this.text = text; this.x = x; this.y = y; this.color = color; this.timeLeft = 1.2f;
        this.font = new Font("SansSerif", Font.BOLD, size);
    }

    public boolean update(float dt) { timeLeft -= dt; y -= 28 * dt; return timeLeft > 0; }

    public void render(Graphics2D g) {
        g.setFont(font);
        Util.ctext(g, text, (int) x, (int) y, Util.alpha(color, (int) (255 * Math.min(1, timeLeft / 0.4f))));
    }
}
