package deadsignal.ui;

import deadsignal.audio.Audio;
import deadsignal.core.Const;
import deadsignal.core.Util;
import deadsignal.gfx.Fonts;
import deadsignal.progress.Medal;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.Queue;

/** Big on-screen announcements: chapter/boss title cards, banners and medal pop-ups. */
public final class Announcer {
    private static final float CARD_TIME = 3.6f, BANNER_TIME = 2.2f, MEDAL_TIME = 3.2f;
    private String cardTitle, cardSub, banner;
    private Color cardColor = Color.WHITE, bannerColor = Color.WHITE;
    private float cardTimer, bannerTimer, medalTimer;
    private final Queue<Medal> medalQueue = new ArrayDeque<>();
    private Medal medal;

    public void showCard(String title, String sub, Color c) { cardTitle = title; cardSub = sub; cardColor = c; cardTimer = CARD_TIME; }
    public void showBanner(String text, Color c) { banner = text; bannerColor = c; bannerTimer = BANNER_TIME; }
    public void showMedal(Medal m) { medalQueue.add(m); }

    public void update(float dt, Audio audio) {
        cardTimer -= dt; bannerTimer -= dt;
        if (medal == null) { medal = medalQueue.poll(); if (medal != null) { medalTimer = MEDAL_TIME; audio.play("medal"); } }
        else { medalTimer -= dt; if (medalTimer <= 0) medal = null; }
    }

    public void render(Graphics2D g) {
        int W = Const.WIDTH, H = Const.HEIGHT;
        if (cardTimer > 0 && cardTitle != null) {
            float a = Util.clamp(Math.min((CARD_TIME - cardTimer) * 2, cardTimer * 1.5f), 0, 1);
            Composite oc = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
            g.setColor(new Color(0, 0, 0, 150)); g.fillRect(0, H / 2 - 74, W, 120);
            g.setFont(Fonts.HUGE); Util.ctext(g, cardTitle, W / 2, H / 2, cardColor);
            g.setFont(Fonts.CINEMATIC); Util.ctext(g, cardSub, W / 2, H / 2 + 34, new Color(220, 210, 190));
            g.setComposite(oc);
        } else if (bannerTimer > 0 && banner != null) {
            g.setFont(Fonts.XL); Util.ctext(g, banner, W / 2, 160, Util.alpha(bannerColor, (int) (255 * Math.min(1, bannerTimer * 2))));
        }
        renderMedal(g, 120);
    }

    /** @param targetY where the pop-up comes to rest (it slides down from above). */
    public void renderMedal(Graphics2D g, int targetY) {
        if (medal == null) return;
        float k = Math.min(1, (MEDAL_TIME - medalTimer) * 4) * Math.min(1, medalTimer * 3);
        int mx = Const.WIDTH / 2 - 170, my = (int) (-70 + (targetY + 70) * k);
        g.setColor(new Color(20, 16, 8, 220)); g.fillRoundRect(mx, my, 340, 62, 14, 14);
        g.setColor(new Color(255, 200, 80)); g.setStroke(new BasicStroke(2f)); g.drawRoundRect(mx, my, 340, 62, 14, 14);
        Util.drawStar(g, mx + 32, my + 31, 16, new Color(255, 200, 80));
        g.setFont(new Font("SansSerif", Font.BOLD, 11)); Util.text(g, "MEDAL UNLOCKED  +$" + Medal.CASH_BONUS, mx + 62, my + 20, new Color(255, 200, 80));
        g.setFont(Fonts.LARGE); Util.text(g, medal.getTitle(), mx + 62, my + 42, Color.WHITE);
        g.setFont(new Font("SansSerif", Font.PLAIN, 11)); Util.text(g, medal.getDescription(), mx + 62, my + 56, new Color(210, 210, 200));
    }

    public boolean isCardShowing() { return cardTimer > 0; }
}
