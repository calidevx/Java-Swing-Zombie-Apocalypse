package deadsignal.screen;

import deadsignal.core.Const;
import deadsignal.core.Util;
import deadsignal.game.ChapterStats;
import deadsignal.game.Game;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Fonts;
import deadsignal.progress.Medal;
import deadsignal.progress.PlayerProfile;
import deadsignal.story.Reward;

import java.awt.*;

/** Chapter report card: stars, statistics, cash bonus and the reward unlocked. */
public final class ResultsScreen extends Screen {
    private final GameWorld world;
    private final int stars, bonus;
    private float timer;

    public ResultsScreen(Game game, GameWorld world) {
        super(game);
        this.world = world;
        ChapterStats s = world.stats();
        PlayerProfile p = world.getProfile();
        stars = s.stars();
        bonus = s.bonus();
        p.addCash(bonus);
        p.addPlayTime(s.getTime());
        if (s.accuracy() >= 0.65f && s.getShots() > 40) world.award(Medal.SHARPSHOOTER);
        if (s.getDamageTaken() < 40) world.award(Medal.IRON_SKIN);
        Reward r = world.getChapter().getReward();
        if (r != null) r.grantTo(p);
    }

    @Override public void onEnter() { game.getAudio().play("medal"); }

    @Override
    public void update(float dt) {
        timer += dt;
        world.announcer().update(dt, game.getAudio());
        if (timer > 0.8f && game.getInput().confirmPressed()) game.afterResults();
    }

    @Override
    public void render(Graphics2D g) {
        world.render(g, false);
        int W = Const.WIDTH, H = Const.HEIGHT, px = 170, py = 60, pw = W - 340, ph = H - 120;
        ChapterStats s = world.stats();
        g.setColor(new Color(0, 0, 0, 200)); g.fillRect(0, 0, W, H);
        g.setColor(new Color(16, 16, 20, 240)); g.fillRoundRect(px, py, pw, ph, 20, 20);
        g.setColor(new Color(255, 200, 90)); g.setStroke(new BasicStroke(2f)); g.drawRoundRect(px, py, pw, ph, 20, 20);
        g.setFont(Fonts.SMALL); Util.ctext(g, world.getChapter().getLabel() + " COMPLETE", W / 2, py + 34, new Color(200, 190, 170));
        g.setFont(Fonts.XL); Util.ctext(g, world.getChapter().getTitle(), W / 2, py + 70, Color.WHITE);
        for (int i = 0; i < 3; i++) {
            float appear = Util.clamp((timer - 0.3f - i * 0.3f) * 4, 0, 1);
            Util.drawStar(g, W / 2 - 70 + i * 70, py + 120, 26 * (0.5f + 0.5f * appear), i < stars ? new Color(255, 210, 70) : new Color(70, 70, 76));
        }
        String[][] rows = {
                {"Time", String.format("%d:%02d", (int) s.getTime() / 60, (int) s.getTime() % 60)},
                {"Infected killed", String.valueOf(s.getKills())},
                {"Accuracy", s.getShots() > 0 ? (int) (s.accuracy() * 100) + "%" : "-"},
                {"Damage taken", String.valueOf((int) s.getDamageTaken())},
                {"Best combo", "x" + s.getBestCombo()},
                {"Chapter bonus", "+$" + bonus}};
        g.setFont(Fonts.MEDIUM);
        for (int i = 0; i < rows.length; i++) {
            int a = (int) (255 * Util.clamp((timer - 0.9f - i * 0.12f) * 5, 0, 1));
            Util.text(g, rows[i][0], px + 90, py + 190 + i * 30, Util.alpha(new Color(200, 200, 200), a));
            Util.text(g, rows[i][1], px + pw - 90 - g.getFontMetrics().stringWidth(rows[i][1]), py + 190 + i * 30, Util.alpha(i == 5 ? new Color(255, 215, 90) : Color.WHITE, a));
        }
        Reward r = world.getChapter().getReward();
        if (r != null) {
            int ry = py + 380;
            g.setColor(new Color(60, 40, 10, 200)); g.fillRoundRect(px + 50, ry, pw - 100, 58, 12, 12);
            g.setColor(new Color(255, 200, 90)); g.drawRoundRect(px + 50, ry, pw - 100, 58, 12, 12);
            r.drawIcon(g, px + 90, ry + 29);
            g.setFont(new Font("SansSerif", Font.BOLD, 11)); Util.text(g, "REWARD UNLOCKED", px + 160, ry + 22, new Color(255, 200, 90));
            g.setFont(Fonts.MEDIUM); Util.text(g, r.getDescription(), px + 160, ry + 44, Color.WHITE);
        }
        if (timer > 0.8f && ((int) (timer * 2.5)) % 2 == 0) { g.setFont(Fonts.SMALL); Util.ctext(g, "SPACE / CLICK to continue", W / 2, py + ph - 16, new Color(200, 200, 200)); }
        world.announcer().renderMedal(g, 0);
    }
}
