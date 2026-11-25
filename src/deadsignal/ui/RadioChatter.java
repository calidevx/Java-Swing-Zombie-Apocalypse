package deadsignal.ui;

import deadsignal.audio.Audio;
import deadsignal.core.Const;
import deadsignal.core.Util;
import deadsignal.gfx.Fonts;
import deadsignal.story.DialogueLine;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

/** Non-blocking radio messages shown in the corner while you keep playing. */
public final class RadioChatter {
    private final Queue<DialogueLine> queue = new ArrayDeque<>();
    private DialogueLine current;
    private float timer;

    public void queue(DialogueLine line) { queue.add(line); }
    public void queue(List<DialogueLine> lines) { queue.addAll(lines); }

    private float duration() { return 2.6f + current.getText().length() * 0.045f; }

    public void update(float dt, Audio audio) {
        if (current == null) {
            current = queue.poll();
            if (current != null) { timer = 0; audio.play("radio", 0.35f, 1f); }
        } else {
            timer += dt;
            if (timer > duration()) current = null;
        }
    }

    public void render(Graphics2D g) {
        if (current == null) return;
        float a = Util.clamp(Math.min(timer * 4, (duration() - timer) * 3), 0, 1);
        Composite oc = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
        int x = 12, y = Const.HEIGHT - 104, w = 470;
        Color c = current.getSpeaker().getColor();
        g.setColor(new Color(0, 0, 0, 170)); g.fillRoundRect(x, y, w, 92, 12, 12);
        g.setColor(c); g.setStroke(new BasicStroke(1.5f)); g.drawRoundRect(x, y, w, 92, 12, 12);
        Portraits.draw(g, current.getSpeaker(), x + 10, y + 10, 72);
        g.setFont(Fonts.SMALL); Util.text(g, current.getSpeaker().getDisplayName(), x + 94, y + 24, c);
        g.setFont(Fonts.BODY);
        String shown = current.getText().substring(0, (int) Math.min(current.getText().length(), timer * 70));
        List<String> lines = Util.wrap(g.getFontMetrics(), shown, w - 106);
        for (int i = 0; i < lines.size() && i < 3; i++) Util.text(g, lines.get(i), x + 94, y + 44 + i * 18, new Color(235, 235, 235));
        g.setComposite(oc);
    }
}
