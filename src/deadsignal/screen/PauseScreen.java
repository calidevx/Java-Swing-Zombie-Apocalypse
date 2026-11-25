package deadsignal.screen;

import deadsignal.core.Const;
import deadsignal.core.Util;
import deadsignal.game.Game;
import deadsignal.gfx.Fonts;
import deadsignal.progress.Medal;
import deadsignal.progress.PlayerProfile;
import deadsignal.ui.MenuList;

import java.awt.*;
import java.awt.event.KeyEvent;

public final class PauseScreen extends Screen {
    private final PlayScreen play;
    private final MenuList menu = new MenuList(300, 54, 340, "RESUME", "RESTART CHAPTER", "CONTROLS", "QUIT TO TITLE");

    public PauseScreen(Game game, PlayScreen play) { super(game); this.play = play; }

    @Override
    public void update(float dt) {
        if (game.getInput().wasPressed(KeyEvent.VK_ESCAPE, KeyEvent.VK_P)) { game.setScreen(play); return; }
        switch (menu.handle(game.getInput(), game.getAudio())) {
            case 0: game.setScreen(play); break;
            case 1: game.retryChapter(); break;
            case 2: game.setScreen(new ControlsScreen(game, this)); break;
            case 3: game.returnToTitle(); break;
            default: break;
        }
    }

    @Override
    public void render(Graphics2D g) {
        play.render(g);
        g.setColor(new Color(0, 0, 0, 170)); g.fillRect(0, 0, Const.WIDTH, Const.HEIGHT);
        g.setFont(Fonts.HUGE); Util.ctext(g, "PAUSED", Const.WIDTH / 2, 220, Color.WHITE);
        menu.render(g);
        PlayerProfile p = game.getProfile();
        g.setFont(Fonts.SMALL);
        Util.ctext(g, "Kills: " + p.getKills() + "    Cash: $" + p.getCash() + "    Medals: " + p.getMedals().size() + "/" + Medal.values().length,
                Const.WIDTH / 2, Const.HEIGHT - 40, new Color(200, 200, 200));
        MenuList.drawPointer(g, game.getInput());
    }
}
