package deadsignal.ui;

import deadsignal.audio.Audio;
import deadsignal.core.Const;
import deadsignal.core.Input;
import deadsignal.core.Util;
import deadsignal.gfx.Fonts;

import java.awt.*;
import java.awt.event.KeyEvent;

/** Reusable vertical menu (keyboard + mouse). Selection state is encapsulated. */
public final class MenuList {
    private final String[] labels;
    private final int top, spacing, width;
    private int selected, lastMx = -1, lastMy = -1;

    public MenuList(int top, int spacing, int width, String... labels) {
        this.labels = labels.clone(); this.top = top; this.spacing = spacing; this.width = width;
    }

    private Rectangle bounds(int i) { return new Rectangle(Const.WIDTH / 2 - width / 2, top + i * spacing - 28, width, 40); }

    /** @return the index the user activated this frame, or -1. */
    public int handle(Input in, Audio audio) {
        int n = labels.length;
        if (in.wasPressed(KeyEvent.VK_UP, KeyEvent.VK_W)) { selected = (selected + n - 1) % n; audio.play("select"); }
        if (in.wasPressed(KeyEvent.VK_DOWN, KeyEvent.VK_S)) { selected = (selected + 1) % n; audio.play("select"); }
        boolean moved = in.getMouseX() != lastMx || in.getMouseY() != lastMy;
        lastMx = in.getMouseX(); lastMy = in.getMouseY();
        for (int i = 0; i < n; i++) {
            Rectangle r = bounds(i);
            if (moved && r.contains(lastMx, lastMy) && selected != i) { selected = i; audio.play("select"); }
            if (in.leftClickedIn(r)) { selected = i; return i; }
        }
        if (in.wasPressed(KeyEvent.VK_ENTER, KeyEvent.VK_SPACE)) return selected;
        return -1;
    }

    public void render(Graphics2D g) {
        for (int i = 0; i < labels.length; i++) {
            Rectangle r = bounds(i);
            boolean sel = i == selected;
            g.setColor(sel ? new Color(150, 20, 20, 210) : new Color(0, 0, 0, 160)); g.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
            g.setColor(sel ? new Color(255, 120, 100) : new Color(120, 120, 120)); g.setStroke(new BasicStroke(1.5f)); g.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);
            g.setFont(Fonts.LARGE); Util.ctext(g, labels[i], Const.WIDTH / 2, top + i * spacing, sel ? Color.WHITE : new Color(200, 200, 200));
        }
    }

    public void setLabel(int i, String text) { labels[i] = text; }
    public int getSelected() { return selected; }

    /** Draws the arrow mouse pointer used on menu screens. */
    public static void drawPointer(Graphics2D g, Input in) {
        int cx = in.getMouseX(), cy = in.getMouseY();
        g.setColor(Color.BLACK); g.fillPolygon(new int[]{cx + 1, cx + 1, cx + 13}, new int[]{cy + 1, cy + 17, cy + 11}, 3);
        g.setColor(Color.WHITE); g.fillPolygon(new int[]{cx, cx, cx + 12}, new int[]{cy, cy + 16, cy + 10}, 3);
    }
}
