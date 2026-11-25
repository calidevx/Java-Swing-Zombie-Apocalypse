package deadsignal.screen;

import deadsignal.core.Const;
import deadsignal.core.Util;
import deadsignal.game.Game;
import deadsignal.gfx.Fonts;

import java.awt.*;
import java.awt.event.KeyEvent;

public final class ControlsScreen extends Screen {
    private static final String[][] ROWS = {{"WASD / Arrows", "Move"}, {"Mouse", "Aim"}, {"Left click (hold)", "Shoot"}, {"Right click / G", "Throw grenade"},
            {"R", "Reload"}, {"SPACE", "Dodge roll (invulnerable)"}, {"1 - 6 / Mouse wheel", "Switch weapon"}, {"Q", "Use medkit"},
            {"E (hold)", "Interact / rescue / sabotage"}, {"F / C", "Skills: Shockwave / Decoy Dash"}, {"ESC / P", "Pause"}, {"F11", "Maximise window"}};
    private final Screen returnTo;

    public ControlsScreen(Game game, Screen returnTo) { super(game); this.returnTo = returnTo; }

    @Override public void update(float dt) {
        game.getBackdrop().update(dt);
        if (game.getInput().confirmPressed() || game.getInput().wasPressed(KeyEvent.VK_ESCAPE)) game.setScreen(returnTo);
    }

    @Override public void render(Graphics2D g) {
        game.getBackdrop().render(g);
        g.setColor(new Color(0, 0, 0, 200)); g.fillRoundRect(150, 70, Const.WIDTH - 300, Const.HEIGHT - 140, 20, 20);
        g.setFont(Fonts.XL); Util.ctext(g, "CONTROLS", Const.WIDTH / 2, 120, new Color(255, 120, 100));
        g.setFont(Fonts.MEDIUM);
        for (int i = 0; i < ROWS.length; i++) {
            Util.text(g, ROWS[i][0], 230, 168 + i * 34, new Color(255, 220, 140));
            Util.text(g, ROWS[i][1], 500, 168 + i * 34, Color.WHITE);
        }
        g.setFont(Fonts.SMALL); Util.ctext(g, "Press any key to return", Const.WIDTH / 2, Const.HEIGHT - 90, new Color(180, 180, 180));
    }
}
