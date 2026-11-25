package deadsignal.screen;

import deadsignal.core.Const;
import deadsignal.core.Input;
import deadsignal.core.Util;
import deadsignal.game.Game;
import deadsignal.gfx.Fonts;
import deadsignal.progress.PlayerProfile;
import deadsignal.progress.ShopItem;
import deadsignal.progress.Stat;
import deadsignal.progress.StatUpgrade;
import deadsignal.skill.SkillType;
import deadsignal.ui.MenuList;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Between-chapter shop with two tabs: UPGRADES (uncapped stats + consumables) and SPECIAL SKILLS
 * (unlocked after the Bonus Stage). Works with any ShopItem subclass through the abstract API.
 */
public final class ShopScreen extends Screen {
    private static final int CONTINUE = 99;
    private int tab, selected = CONTINUE, lastMx = -1, lastMy = -1;
    private String message = "";
    private float messageTimer, time;

    public ShopScreen(Game game) {
        super(game);
        PlayerProfile p = game.getProfile();
        boolean ownsSkill = false;
        for (SkillType t : SkillType.values()) if (p.getSkillLevel(t) > 0) ownsSkill = true;
        if (p.areSkillsUnlocked() && !ownsSkill) { tab = 1; message = "NEW: SPECIAL SKILLS UNLOCKED!"; messageTimer = 4f; }
    }

    private List<ShopItem> items() { return tab == 0 ? game.getShop().getItems() : game.getShop().getSkills(); }

    private Rectangle bounds(int i) {
        if (i == CONTINUE) return new Rectangle(Const.WIDTH / 2 - 140, 560, 280, 50);
        if (tab == 0) return new Rectangle(56 + (i % 4) * 216, 190 + (i / 4) * 170, 200, 158);
        return new Rectangle(70 + i * 280, 196, 260, 330);
    }
    private Rectangle tabBounds(int t) { return new Rectangle(Const.WIDTH / 2 - 230 + t * 240, 134, 220, 36); }

    @Override
    public void update(float dt) {
        time += dt;
        game.getBackdrop().update(dt);
        messageTimer -= dt;
        Input in = game.getInput();
        for (int t = 0; t < 2; t++) if (in.leftClickedIn(tabBounds(t))) switchTab(t);
        if (in.wasPressed(KeyEvent.VK_TAB)) switchTab(1 - tab);
        List<ShopItem> items = items();
        int n = items.size();
        boolean moved = in.getMouseX() != lastMx || in.getMouseY() != lastMy;
        lastMx = in.getMouseX(); lastMy = in.getMouseY();
        for (int i = 0; i < n; i++) if (moved && bounds(i).contains(lastMx, lastMy)) selected = i;
        if (moved && bounds(CONTINUE).contains(lastMx, lastMy)) selected = CONTINUE;
        if (in.wasPressed(KeyEvent.VK_LEFT, KeyEvent.VK_A)) selected = selected == CONTINUE ? n - 1 : (selected + n - 1) % n;
        if (in.wasPressed(KeyEvent.VK_RIGHT, KeyEvent.VK_D)) selected = selected == CONTINUE ? 0 : (selected + 1) % n;
        if (in.wasPressed(KeyEvent.VK_UP, KeyEvent.VK_W, KeyEvent.VK_DOWN, KeyEvent.VK_S)) selected = selected == CONTINUE ? 0 : CONTINUE;
        for (int k = 0; k < n && k < 9; k++) if (in.wasPressed(KeyEvent.VK_1 + k)) buy(items.get(k));
        for (int i = 0; i < n; i++) if (in.leftClickedIn(bounds(i))) buy(items.get(i));
        if (in.leftClickedIn(bounds(CONTINUE))) { leave(); return; }
        if (in.wasPressed(KeyEvent.VK_ENTER, KeyEvent.VK_SPACE)) { if (selected == CONTINUE) { leave(); return; } buy(items.get(selected)); }
        if (in.wasPressed(KeyEvent.VK_ESCAPE)) leave();
    }

    private void switchTab(int t) { if (t != tab) { tab = t; selected = CONTINUE; game.getAudio().play("select"); } }

    /** Buys an item. Public so the automated test can use it. */
    public void buy(ShopItem item) {
        switch (item.purchase(game.getProfile())) {
            case PURCHASED: message = item.getName() + " PURCHASED"; game.getAudio().play("buy"); break;
            case MAXED_OUT: message = "MAXED OUT"; game.getAudio().play("empty"); break;
            case LOCKED: message = "CLEAR THE BONUS STAGE TO UNLOCK SKILLS"; game.getAudio().play("empty"); break;
            default: message = "NOT ENOUGH CASH"; game.getAudio().play("empty");
        }
        messageTimer = 1.6f;
    }

    private void leave() { game.getAudio().play("objective"); game.leaveShop(); }

    @Override
    public void render(Graphics2D g) {
        game.getBackdrop().render(g);
        int W = Const.WIDTH;
        PlayerProfile p = game.getProfile();
        g.setColor(new Color(0, 0, 0, 195)); g.fillRect(0, 0, W, Const.HEIGHT);
        g.setFont(Fonts.XL); Util.ctext(g, "SAFEHOUSE  -  SUPPLY CACHE", W / 2, 46, new Color(255, 200, 90));
        deadsignal.story.Chapter next = game.getCampaign().get(game.getChapterIndex() + 1);
        g.setFont(Fonts.MEDIUM); Util.ctext(g, "Next: " + next.getLabel() + "  -  " + next.getTitle(), W / 2, 72, new Color(200, 200, 200));
        g.setFont(Fonts.XL); Util.ctext(g, "$ " + String.format("%,d", p.getCash()), W / 2, 112, new Color(255, 215, 90));
        drawTabs(g, p);
        List<ShopItem> items = items();
        for (int i = 0; i < items.size(); i++) drawCard(g, items.get(i), bounds(i), i, p);
        if (tab == 1 && !p.areSkillsUnlocked()) {
            g.setColor(new Color(0, 0, 0, 150)); g.fillRoundRect(60, 186, W - 120, 350, 16, 16);
            g.setFont(Fonts.XL); Util.ctext(g, "LOCKED", W / 2, 340, new Color(255, 120, 100));
            g.setFont(Fonts.MEDIUM); Util.ctext(g, "Clear the BONUS STAGE to unlock special skills", W / 2, 372, Color.WHITE);
        }
        Rectangle c = bounds(CONTINUE);
        g.setColor(selected == CONTINUE ? new Color(150, 20, 20, 230) : new Color(60, 14, 14, 220)); g.fillRoundRect(c.x, c.y, c.width, c.height, 12, 12);
        g.setColor(new Color(255, 120, 100)); g.drawRoundRect(c.x, c.y, c.width, c.height, 12, 12);
        g.setFont(Fonts.LARGE); Util.ctext(g, "CONTINUE  >>", W / 2, c.y + 33, Color.WHITE);
        if (messageTimer > 0) { g.setFont(Fonts.MEDIUM); Util.ctext(g, message, W / 2, 186, new Color(255, 230, 150)); }
        g.setFont(Fonts.SMALL);
        Util.ctext(g, "TAB switches tabs  -  click a card or press its number to buy  -  upgrades have no level cap", W / 2, Const.HEIGHT - 12, new Color(160, 160, 160));
        MenuList.drawPointer(g, game.getInput());
    }

    private void drawTabs(Graphics2D g, PlayerProfile p) {
        String[] names = {"UPGRADES", p.areSkillsUnlocked() ? "SPECIAL SKILLS" : "SPECIAL SKILLS  [LOCKED]"};
        for (int t = 0; t < 2; t++) {
            Rectangle r = tabBounds(t);
            boolean on = t == tab;
            g.setColor(on ? new Color(120, 30, 20, 235) : new Color(30, 30, 34, 220)); g.fillRoundRect(r.x, r.y, r.width, r.height, 10, 10);
            g.setColor(on ? new Color(255, 170, 110) : new Color(100, 100, 110)); g.setStroke(new BasicStroke(on ? 2f : 1f)); g.drawRoundRect(r.x, r.y, r.width, r.height, 10, 10);
            g.setFont(Fonts.MEDIUM); Util.ctext(g, names[t], r.x + r.width / 2, r.y + 24, on ? Color.WHITE : new Color(180, 180, 180));
            if (t == 1 && p.areSkillsUnlocked() && ((int) (time * 3)) % 2 == 0 && tab == 0) { g.setColor(new Color(255, 90, 90)); Util.oval(g, r.x + r.width - 10, r.y + 6, 4, 4); }
        }
    }

    private void drawCard(Graphics2D g, ShopItem it, Rectangle r, int i, PlayerProfile p) {
        boolean sel = selected == i, locked = it.isLocked(p), available = it.isAvailable(p), afford = p.getCash() >= it.getCost(p);
        g.setColor(sel ? new Color(60, 30, 20, 240) : new Color(18, 18, 22, 230)); g.fillRoundRect(r.x, r.y, r.width, r.height, 14, 14);
        g.setColor(sel ? new Color(255, 150, 90) : new Color(90, 90, 96)); g.setStroke(new BasicStroke(sel ? 2.5f : 1.5f)); g.drawRoundRect(r.x, r.y, r.width, r.height, 14, 14);
        g.setFont(new Font("SansSerif", Font.BOLD, 11)); Util.text(g, "[" + (i + 1) + "]", r.x + 10, r.y + 20, new Color(150, 150, 150));
        boolean big = tab == 1;
        if (big) { g.setColor(new Color(40, 40, 50)); Util.oval(g, r.x + r.width / 2.0, r.y + 70, 40, 40); it.drawIcon(g, r.x + r.width / 2, r.y + 70); }
        else it.drawIcon(g, r.x + r.width - 34, r.y + 30);
        int ty = big ? r.y + 140 : r.y + 46;
        g.setFont(big ? Fonts.LARGE : Fonts.MEDIUM);
        if (big) Util.ctext(g, it.getName(), r.x + r.width / 2, ty, Color.WHITE); else Util.text(g, it.getName(), r.x + 12, ty, Color.WHITE);
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        List<String> desc = Util.wrap(g.getFontMetrics(), it.getDescription(), r.width - 24);
        for (int k = 0; k < desc.size() && k < 3; k++) {
            if (big) Util.ctext(g, desc.get(k), r.x + r.width / 2, ty + 24 + k * 16, new Color(200, 200, 200));
            else Util.text(g, desc.get(k), r.x + 12, ty + 22 + k * 15, new Color(200, 200, 200));
        }
        int level = it.getLevel(p);
        int py = big ? r.y + 250 : r.y + 92;
        if (level >= 0) {
            int pips = big ? SkillType.MAX_LEVEL : Stat.PIPS, px = big ? r.x + r.width / 2 - pips * 11 : r.x + 12;
            for (int k = 0; k < pips; k++) { g.setColor(k < level ? new Color(255, 190, 70) : new Color(60, 60, 66)); g.fillRoundRect(px + k * 22, py, 18, 8, 4, 4); }
            if (it instanceof StatUpgrade && level > Stat.PIPS) { g.setFont(Fonts.SMALL); Util.text(g, "+" + (level - Stat.PIPS), px + pips * 22 + 2, py + 9, new Color(255, 210, 120)); }
            if (big) { g.setFont(Fonts.SMALL); Util.ctext(g, it.getStatusText(p), r.x + r.width / 2, py + 26, locked ? new Color(255, 120, 100) : new Color(180, 220, 180)); }
            else if (it instanceof StatUpgrade) { g.setFont(Fonts.TINY); Util.text(g, "LEVEL " + level, r.x + 12, py + 22, new Color(170, 170, 180)); }
        } else { g.setFont(Fonts.SMALL); Util.text(g, it.getStatusText(p), r.x + 12, py, new Color(180, 220, 180)); }
        g.setFont(Fonts.LARGE);
        String price = locked ? "LOCKED" : !available ? "MAX" : "$" + String.format("%,d", it.getCost(p));
        Color pc = locked || !available ? new Color(140, 140, 140) : afford ? new Color(255, 215, 90) : new Color(220, 80, 70);
        if (big) Util.ctext(g, price, r.x + r.width / 2, r.y + r.height - 18, pc); else Util.text(g, price, r.x + 12, r.y + r.height - 14, pc);
    }
}
