package deadsignal.screen;

import deadsignal.core.Const;
import deadsignal.core.Util;
import deadsignal.game.Game;
import deadsignal.gfx.Fonts;
import deadsignal.ui.MenuList;

import java.awt.*;
import java.awt.event.KeyEvent;

public final class TitleScreen extends Screen {
    private final MenuList menu = new MenuList(400, 50, 340, "NEW GAME", "", "CONTROLS", "QUIT");
    private float time;

    public TitleScreen(Game game) { super(game); }

    @Override
    public void update(float dt) {
        time += dt;
        game.getBackdrop().update(dt);
        if (menu.getSelected() == 1) {
            if (game.getInput().wasPressed(KeyEvent.VK_LEFT, KeyEvent.VK_A)) { game.setDifficulty(game.getDifficulty().previous()); game.getAudio().play("select"); }
            if (game.getInput().wasPressed(KeyEvent.VK_RIGHT, KeyEvent.VK_D)) { game.setDifficulty(game.getDifficulty().next()); game.getAudio().play("select"); }
        }
        switch (menu.handle(game.getInput(), game.getAudio())) {
            case 0: game.getAudio().play("select"); game.setScreen(new NameEntryScreen(game, this)); break;
            case 1: game.setDifficulty(game.getDifficulty().next()); game.getAudio().play("select"); break;
            case 2: game.setScreen(new ControlsScreen(game, this)); break;
            case 3: game.quit(); break;
            default: break;
        }
    }

    @Override
    public void render(Graphics2D g) {
        game.getBackdrop().render(g);
        int W = Const.WIDTH;
        float flicker = ((int) (time * 10)) % 37 == 0 ? 0.4f : 1f;
        g.setFont(Fonts.TITLE);
        String t = "DEAD SIGNAL";
        int tw = g.getFontMetrics().stringWidth(t);
        g.setColor(new Color(0, 0, 0, 200)); g.drawString(t, W / 2 - tw / 2 + 5, 205);
        g.setPaint(new GradientPaint(0, 120, new Color(230, 60, 40), 0, 210, new Color(110, 10, 10)));
        Composite oc = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, flicker));
        g.drawString(t, W / 2 - tw / 2, 200);
        g.setComposite(oc);
        g.setColor(new Color(255, 120, 100, 90)); g.setStroke(new BasicStroke(1f));
        for (int k = 0; k < 3; k++) { int y = 140 + (int) ((time * 80 + k * 30) % 70); g.drawLine(W / 2 - tw / 2, y, W / 2 + tw / 2, y); }
        g.setFont(Fonts.CINEMATIC); Util.ctext(g, "A Zombie Apocalypse Story  -  Port Meridian has fallen", W / 2, 248, new Color(220, 210, 190));
        menu.setLabel(1, "DIFFICULTY:  < " + game.getDifficulty().getLabel() + " >");
        menu.render(g);
        g.setFont(Fonts.SMALL); Util.ctext(g, "WASD/Mouse  -  ENTER to select", W / 2, Const.HEIGHT - 20, new Color(170, 170, 170));
        MenuList.drawPointer(g, game.getInput());
    }
}
