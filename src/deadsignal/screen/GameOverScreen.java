package deadsignal.screen;

import deadsignal.core.Const;
import deadsignal.core.Util;
import deadsignal.game.Game;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Fonts;
import deadsignal.ui.MenuList;

import java.awt.*;

public final class GameOverScreen extends Screen {
    private final GameWorld world;
    private final MenuList menu = new MenuList(420, 56, 340, "RETRY CHAPTER", "MAIN MENU");
    private float timer;

    public GameOverScreen(Game game, GameWorld world) { super(game); this.world = world; }

    @Override
    public void update(float dt) {
        timer += dt;
        if (timer < 0.8f) return;
        int sel = menu.handle(game.getInput(), game.getAudio());
        if (sel == 0) game.startChapter(game.getChapterIndex(), true);
        else if (sel == 1) game.returnToTitle();
    }

    @Override
    public void render(Graphics2D g) {
        world.render(g, false);
        float k = Math.min(1, timer / 0.8f);
        g.setColor(new Color(40, 0, 0, (int) (200 * k))); g.fillRect(0, 0, Const.WIDTH, Const.HEIGHT);
        g.setFont(Fonts.TITLE); Util.ctext(g, "YOU DIED", Const.WIDTH / 2, 260, new Color(200, 30, 30, (int) (255 * k)));
        g.setFont(Fonts.CINEMATIC); Util.ctext(g, "The signal goes quiet... but Lena is still out there.", Const.WIDTH / 2, 320, new Color(220, 200, 190, (int) (255 * k)));
        if (timer >= 0.8f) { menu.render(g); MenuList.drawPointer(g, game.getInput()); }
    }
}
