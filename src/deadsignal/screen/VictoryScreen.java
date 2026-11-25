package deadsignal.screen;

import deadsignal.core.Const;
import deadsignal.core.Util;
import deadsignal.game.Game;
import deadsignal.gfx.Fonts;
import deadsignal.progress.Medal;
import deadsignal.progress.PlayerProfile;

import java.awt.*;

public final class VictoryScreen extends Screen {
    private float timer;

    public VictoryScreen(Game game) { super(game); }

    @Override public void update(float dt) {
        timer += dt;
        game.getBackdrop().update(dt);
        if (timer > 2 && game.getInput().confirmPressed()) game.returnToTitle();
    }

    @Override public void render(Graphics2D g) {
        game.getBackdrop().render(g);
        int W = Const.WIDTH, H = Const.HEIGHT;
        PlayerProfile p = game.getProfile();
        g.setColor(new Color(0, 0, 0, 200)); g.fillRect(0, 0, W, H);
        g.setFont(Fonts.TITLE); Util.ctext(g, "SIGNAL RESTORED", W / 2, 170, Util.alpha(new Color(120, 230, 170), (int) (255 * Math.min(1, timer / 2))));
        g.setFont(Fonts.CINEMATIC); Util.ctext(g, "Port Meridian will live again.", W / 2, 215, new Color(230, 222, 205));
        String[][] rows = {
                {"Difficulty", game.getDifficulty().getLabel()},
                {"Total play time", String.format("%d:%02d", (int) p.getPlayTime() / 60, (int) p.getPlayTime() % 60)},
                {"Infected killed", String.valueOf(p.getKills())},
                {"Deaths", String.valueOf(p.getDeaths())},
                {"Medals earned", p.getMedals().size() + " / " + Medal.values().length},
                {"Cash remaining", "$" + p.getCash()}};
        g.setFont(Fonts.LARGE);
        for (int i = 0; i < rows.length; i++) {
            Util.text(g, rows[i][0], 300, 290 + i * 36, new Color(200, 200, 200));
            Util.text(g, rows[i][1], W - 300 - g.getFontMetrics().stringWidth(rows[i][1]), 290 + i * 36, Color.WHITE);
        }
        g.setFont(Fonts.SMALL);
        Util.ctext(g, "DEAD SIGNAL  -  made with Java Swing. Every pixel and sound generated in code.", W / 2, H - 50, new Color(170, 170, 170));
        if (timer > 2 && ((int) (timer * 2.5)) % 2 == 0) Util.ctext(g, "Press ENTER to return to the title screen", W / 2, H - 24, Color.WHITE);
    }
}
