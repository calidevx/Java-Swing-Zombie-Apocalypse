package deadsignal.screen;

import deadsignal.core.Const;
import deadsignal.core.Util;
import deadsignal.game.Game;
import deadsignal.gfx.Fonts;
import deadsignal.story.PlayerName;
import deadsignal.ui.MenuList;

import java.awt.*;
import java.awt.event.KeyEvent;

/** Asks for the hero's name before a new game starts. ENTER confirms, ESC goes back. */
public final class NameEntryScreen extends Screen {
    private final Screen back;
    private final StringBuilder text = new StringBuilder();
    private float time;

    public NameEntryScreen(Game game, Screen back) {
        super(game);
        this.back = back;
        game.getInput().drainTyped();   // discard keystrokes typed before this screen opened
    }

    @Override
    public void update(float dt) {
        time += dt;
        game.getBackdrop().update(dt);
        var in = game.getInput();
        if (in.wasPressed(KeyEvent.VK_ESCAPE)) { game.setScreen(back); return; }
        for (char c : in.drainTyped().toCharArray())
            if (text.length() < PlayerName.MAX_LENGTH) text.append(c);
        if (in.wasPressed(KeyEvent.VK_BACK_SPACE) && text.length() > 0) text.deleteCharAt(text.length() - 1);
        if (in.wasPressed(KeyEvent.VK_ENTER)) {
            PlayerName.set(text.toString());
            game.getAudio().play("objective");
            game.newGame();
        }
    }

    @Override
    public void render(Graphics2D g) {
        game.getBackdrop().render(g);
        int W = Const.WIDTH;
        g.setColor(new Color(0, 0, 0, 170)); g.fillRect(0, 0, W, Const.HEIGHT);
        g.setFont(Fonts.XL); Util.ctext(g, "ENTER YOUR NAME", W / 2, 250, Color.WHITE);
        int bw = 420, bh = 56, bx = W / 2 - bw / 2, by = 290;
        g.setColor(new Color(20, 20, 26, 230)); g.fillRoundRect(bx, by, bw, bh, 12, 12);
        g.setColor(new Color(150, 200, 255)); g.setStroke(new BasicStroke(2f)); g.drawRoundRect(bx, by, bw, bh, 12, 12);
        String shown = text.toString() + (((int) (time * 2)) % 2 == 0 ? "_" : " ");
        g.setFont(Fonts.LARGE); Util.ctext(g, shown, W / 2, by + 37, Color.WHITE);
        g.setFont(Fonts.SMALL);
        Util.ctext(g, "Leave it empty to play as " + PlayerName.DEFAULT, W / 2, by + 90, new Color(170, 170, 170));
        Util.ctext(g, "ENTER to start  -  ESC to go back", W / 2, Const.HEIGHT - 20, new Color(170, 170, 170));
        MenuList.drawPointer(g, game.getInput());
    }
}
