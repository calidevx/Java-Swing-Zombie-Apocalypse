package deadsignal.screen;

import deadsignal.core.Const;
import deadsignal.core.Input;
import deadsignal.core.Util;
import deadsignal.game.Game;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Fonts;
import deadsignal.story.DialogueLine;
import deadsignal.story.Speaker;
import deadsignal.ui.Portraits;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

/** Blocking cut-scene dialogue with a typewriter effect. */
public final class DialogueScreen extends Screen {
    private final List<DialogueLine> lines;
    private final Runnable then;
    private int index;
    private float chars, time;

    public DialogueScreen(Game game, List<DialogueLine> lines, Runnable then) {
        super(game); this.lines = lines; this.then = then;
    }

    @Override public void onEnter() { game.getAudio().play("radio", 0.5f, 1f); }

    @Override
    public void update(float dt) {
        time += dt;
        chars += dt * 62;
        Input in = game.getInput();
        if (in.wasPressed(KeyEvent.VK_ESCAPE)) { finish(); return; }
        if (in.confirmPressed() || in.wasPressed(KeyEvent.VK_E)) {
            String t = lines.get(index).getText();
            if (chars < t.length()) chars = t.length();
            else {
                index++; chars = 0;
                if (index >= lines.size()) finish(); else game.getAudio().play("select", 0.5f, 1.2f);
            }
        }
    }

    /** Skips the remaining lines (also used by the automated test). */
    public void finish() {
        game.resumePlay();
        if (then != null) then.run();
    }

    @Override
    public void render(Graphics2D g) {
        GameWorld w = game.getWorld();
        if (w != null) w.render(g, w.hasStarted()); else game.getBackdrop().render(g);
        DialogueLine line = lines.get(Math.min(index, lines.size() - 1));
        String txt = line.getText().substring(0, (int) Math.min(line.getText().length(), chars));
        int W = Const.WIDTH, H = Const.HEIGHT;
        if (line.getSpeaker() == Speaker.NARRATOR) {
            g.setColor(new Color(0, 0, 0, 225)); g.fillRect(0, 0, W, H);
            g.setFont(Fonts.CINEMATIC);
            List<String> ls = Util.wrap(g.getFontMetrics(), txt, 700);
            int y0 = H / 2 - ls.size() * 18;
            for (int i = 0; i < ls.size(); i++) Util.ctext(g, ls.get(i), W / 2, y0 + i * 36, new Color(230, 222, 205));
        } else {
            g.setColor(new Color(0, 0, 0, 110)); g.fillRect(0, 0, W, H);
            int bx = 40, by = H - 190, bw = W - 80, bh = 160;
            Color c = line.getSpeaker().getColor();
            g.setColor(new Color(10, 12, 16, 235)); g.fillRoundRect(bx, by, bw, bh, 18, 18);
            g.setColor(c); g.setStroke(new BasicStroke(2f)); g.drawRoundRect(bx, by, bw, bh, 18, 18);
            Portraits.draw(g, line.getSpeaker(), bx + 18, by + 18, 124);
            g.setFont(Fonts.LARGE); Util.text(g, line.getSpeaker().getDisplayName(), bx + 162, by + 40, c);
            g.setFont(Fonts.DIALOGUE);
            List<String> ls = Util.wrap(g.getFontMetrics(), txt, bw - 190);
            for (int i = 0; i < ls.size(); i++) Util.text(g, ls.get(i), bx + 162, by + 74 + i * 27, new Color(238, 238, 238));
        }
        g.setFont(Fonts.SMALL);
        if (((int) (time * 2.5)) % 2 == 0 && chars >= line.getText().length()) Util.ctext(g, "SPACE / CLICK to continue", W / 2, H - 14, new Color(200, 200, 200));
        Util.text(g, "ESC to skip", 14, H - 14, new Color(150, 150, 150));
    }
}
