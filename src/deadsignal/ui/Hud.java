package deadsignal.ui;

import deadsignal.core.*;
import deadsignal.entity.Player;
import deadsignal.entity.boss.Boss;
import deadsignal.entity.item.ObjectiveItem;
import deadsignal.entity.zombie.Zombie;
import deadsignal.game.ComboCounter;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;
import deadsignal.gfx.Fonts;
import deadsignal.gfx.PickupSprite;
import deadsignal.mission.Mission;
import deadsignal.progress.PlayerProfile;
import deadsignal.weapon.Weapon;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

/** Heads-up display. Reads game state through public getters only - it never changes anything. */
public final class Hud {

    public void render(Graphics2D g, GameWorld w, Assets a) {
        Player p = w.getPlayer();
        PlayerProfile prof = w.getProfile();
        drawVitals(g, p, prof, a);
        drawObjective(g, w);
        drawBossBar(g, w.getBoss());
        drawMinimap(g, w);
        drawCombo(g, w.combo());
        drawWeapon(g, prof);
        drawSkills(g, w);
        w.radio().render(g);
        drawObjectiveArrow(g, w);
        w.announcer().render(g);
        drawCrosshair(g, w.input(), prof.getCurrentWeapon());
    }

    private void drawVitals(Graphics2D g, Player p, PlayerProfile prof, Assets a) {
        g.setColor(new Color(0, 0, 0, 130)); g.fillRoundRect(10, 10, 262, 92, 12, 12);
        g.setFont(Fonts.SMALL); Util.text(g, "KAI NAVARRO", 20, 28, new Color(150, 200, 255));
        float hf = p.getHealthFraction();
        Util.bar(g, 20, 34, 240, 16, hf, hf > 0.3f ? new Color(100, 230, 110) : new Color(240, 80, 60), hf > 0.3f ? new Color(40, 140, 60) : new Color(150, 30, 20));
        Util.ctext(g, (int) Math.ceil(p.getHealth()) + " / " + (int) p.getMaxHealth(), 140, 47, Color.WHITE);
        g.drawImage(a.pickup(PickupSprite.HEALTH), 18, 56, null);
        Util.text(g, "x" + prof.getMedkits() + " [Q]", 42, 73, Color.WHITE);
        g.drawImage(a.pickup(PickupSprite.GRENADE), 104, 56, null);
        Util.text(g, "x" + prof.getGrenades() + " [G]", 128, 73, Color.WHITE);
        float rc = p.getRollReadiness();
        g.setColor(new Color(60, 60, 70)); g.fillRoundRect(196, 60, 64, 12, 6, 6);
        g.setColor(rc >= 1 ? new Color(120, 200, 255) : new Color(80, 110, 140)); g.fillRoundRect(196, 60, (int) (64 * rc), 12, 6, 6);
        g.setFont(Fonts.TINY); Util.ctext(g, "DODGE", 228, 70, Color.WHITE);
        g.setFont(Fonts.LARGE); Util.text(g, "$ " + String.format("%,d", prof.getCash()), 20, 96, new Color(255, 215, 90));
        int sx = 20;
        if (p.isStunned()) sx = statusTag(g, sx, "STUNNED", new Color(230, 190, 40));
        if (p.isJammed()) sx = statusTag(g, sx, "JAMMED", new Color(210, 40, 50));
        if (p.isSlowed()) sx = statusTag(g, sx, "SLOWED", new Color(90, 170, 60));
        if (p.getMagnetTime() > 0) statusTag(g, sx, "MAGNET " + (int) Math.ceil(p.getMagnetTime()) + "s", new Color(60, 140, 230));
    }

    private int statusTag(Graphics2D g, int x, String text, Color c) {
        g.setFont(Fonts.TINY);
        int w = g.getFontMetrics().stringWidth(text) + 12;
        g.setColor(c); g.fillRoundRect(x, 108, w, 14, 6, 6);
        g.setColor(Color.WHITE); g.drawString(text, x + 6, 118);
        return x + w + 6;
    }

    private void drawObjective(Graphics2D g, GameWorld w) {
        Mission m = w.getMission();
        if (m == null) return;
        String txt = m.getHudText();
        g.setFont(Fonts.MEDIUM);
        int ow = Math.max(320, g.getFontMetrics().stringWidth(txt) + 40), W = Const.WIDTH;
        g.setColor(new Color(0, 0, 0, 140)); g.fillRoundRect(W / 2 - ow / 2, 10, ow, 54, 12, 12);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        Util.ctext(g, w.getChapter().getLabel() + "  -  " + w.getChapter().getTitle() + "    OBJECTIVE " + (w.getMissionIndex() + 1) + "/" + w.getChapter().getMissions().size(),
                W / 2, 28, new Color(200, 190, 170));
        g.setFont(Fonts.MEDIUM);
        Util.ctext(g, txt, W / 2, 52, m.isComplete() ? new Color(140, 255, 150) : new Color(255, 230, 140));
    }

    /** Draws every bar the boss reports (POLYMORPHISM: Graves adds RAGE, the Shepherd shows lives/aspects). */
    private void drawBossBar(Graphics2D g, Boss boss) {
        if (boss == null) return;
        java.util.List<deadsignal.entity.boss.BossBar> bars = boss.getHudBars();
        int bw = 480, bx = Const.WIDTH / 2 - bw / 2, by = 82;
        int extra = Math.max(0, bars.size() - 1);
        g.setColor(new Color(0, 0, 0, 150)); g.fillRoundRect(bx - 10, by - 6, bw + 20, 40 + extra * 16, 10, 10);
        deadsignal.entity.boss.BossBar main = bars.get(0);
        Util.bar(g, bx, by + 14, bw, 12, main.getFraction(), main.getTop(), main.getBottom());
        g.setFont(Fonts.MEDIUM); Util.ctext(g, main.getLabel(), Const.WIDTH / 2, by + 10, new Color(255, 120, 110));
        for (int i = 1; i < bars.size(); i++) {
            deadsignal.entity.boss.BossBar b = bars.get(i);
            int y = by + 14 + i * 16;
            Util.bar(g, bx + 70, y, bw - 70, 8, b.getFraction(), b.getTop(), b.getBottom());
            g.setFont(Fonts.TINY); Util.text(g, b.getLabel(), bx, y + 8, b.getTop());
        }
        String ability = boss.getActiveAbility();
        if (ability != null) {
            int ay = by + 32 + extra * 16;
            g.setFont(Fonts.SMALL);
            int tw = g.getFontMetrics().stringWidth(ability) + 34;
            g.setColor(new Color(90, 20, 10, 210)); g.fillRoundRect(Const.WIDTH / 2 - tw / 2, ay, tw, 18, 8, 8);
            g.setColor(new Color(255, 170, 90)); g.drawRoundRect(Const.WIDTH / 2 - tw / 2, ay, tw, 18, 8, 8);
            Util.ctext(g, "> " + ability + " <", Const.WIDTH / 2, ay + 13, new Color(255, 220, 150));
        }
    }

    /** Skill buttons between the radio box and the weapon panel, with cooldown sweeps. */
    private void drawSkills(Graphics2D g, GameWorld w) {
        java.util.List<deadsignal.skill.Skill> skills = w.getPlayer().getSkills();
        if (skills.isEmpty()) return;
        int size = 52, gap = 10, x0 = 590 - (skills.size() * (size + gap) - gap) / 2, y = Const.HEIGHT - 70;
        for (int i = 0; i < skills.size(); i++) {
            deadsignal.skill.Skill s = skills.get(i);
            int x = x0 + i * (size + gap);
            boolean active = s.isActiveNow(w);
            g.setColor(new Color(0, 0, 0, 170)); g.fillRoundRect(x, y, size, size, 10, 10);
            if (active) { g.setColor(new Color(200, 30, 50, 120 + (int) (80 * Math.sin(w.getGame().getRealTime() * 8)))); g.fillRoundRect(x, y, size, size, 10, 10); }
            s.getType().drawIcon(g, x + size / 2f, y + size / 2f - 2, 1.2f);
            if (!s.isPassive() && !s.isReady()) {
                g.setColor(new Color(0, 0, 0, 170));
                g.fill(new java.awt.geom.Arc2D.Float(x - 10, y - 10, size + 20, size + 20, 90, 360 * s.getCooldownFraction(), java.awt.geom.Arc2D.PIE));
                g.setFont(Fonts.MEDIUM); Util.ctext(g, String.valueOf((int) Math.ceil(s.getCooldownLeft())), x + size / 2, y + size / 2 + 6, Color.WHITE);
            }
            g.setColor(s.isReady() || s.isPassive() ? new Color(150, 220, 255) : new Color(90, 90, 100)); g.setStroke(new BasicStroke(1.5f));
            g.drawRoundRect(x, y, size, size, 10, 10);
            g.setFont(Fonts.TINY);
            Util.text(g, s.isPassive() ? (active ? "ACTIVE" : "x50") : s.getType().getKeyLabel(), x + 4, y + 11, active ? new Color(255, 120, 130) : new Color(230, 230, 230));
            Util.text(g, "LV" + s.getLevel(), x + size - 22, y + size - 4, new Color(255, 215, 120));
        }
    }

    private void drawMinimap(Graphics2D g, GameWorld w) {
        int mx = Const.WIDTH - 172, my = 10;
        g.setColor(new Color(0, 0, 0, 160)); g.fillRoundRect(mx - 6, my - 4, 172, 140, 10, 10);
        g.drawImage(w.getMap().getMinimap(), mx, my, null);
        float s = 2f / 32f;
        Camera cam = w.camera();
        g.setColor(new Color(255, 255, 255, 120)); g.setStroke(new BasicStroke(1f));
        g.drawRect(mx + (int) (cam.getX() * s), my + (int) (cam.getY() * s), (int) (Const.WIDTH * s), (int) (Const.HEIGHT * s));
        g.setColor(new Color(255, 60, 50));
        for (Zombie z : w.getZombies()) g.fillRect(mx + (int) (z.getX() * s) - 1, my + (int) (z.getY() * s) - 1, 2, 2);
        boolean blink = ((int) (w.getGame().getRealTime() * 3)) % 2 == 0;
        for (ObjectiveItem it : w.getItems()) if (!it.isCompleted()) {
            g.setColor(it.getMinimapColor());
            int d = blink ? 5 : 3; g.fillOval(mx + (int) (it.getX() * s) - d / 2, my + (int) (it.getY() * s) - d / 2, d, d);
        }
        if (w.getBoss() != null) { g.setColor(new Color(255, 40, 40)); g.fillOval(mx + (int) (w.getBoss().getX() * s) - 3, my + (int) (w.getBoss().getY() * s) - 3, 6, 6); }
        Player p = w.getPlayer();
        g.setColor(new Color(110, 200, 255)); g.fillOval(mx + (int) (p.getX() * s) - 3, my + (int) (p.getY() * s) - 3, 6, 6);
    }

    private void drawCombo(Graphics2D g, ComboCounter combo) {
        if (!combo.isShowing()) return;
        int x = Const.WIDTH - 86;
        g.setFont(Fonts.XL); Util.ctext(g, "x" + combo.getCount(), x, 180, new Color(255, 160 + Math.min(95, combo.getCount() * 5), 60));
        g.setFont(Fonts.SMALL); Util.ctext(g, "COMBO  ($ x" + String.format("%.1f", combo.getMultiplier()) + ")", x, 198, new Color(255, 220, 160));
        g.setColor(new Color(255, 180, 60)); g.fillRect(x - 50, 204, (int) (100 * combo.getTimerFraction()), 3);
    }

    private void drawWeapon(Graphics2D g, PlayerProfile prof) {
        Weapon wpn = prof.getCurrentWeapon();
        int wx = Const.WIDTH - 262, wy = Const.HEIGHT - 92;
        g.setColor(new Color(0, 0, 0, 140)); g.fillRoundRect(wx, wy, 252, 82, 12, 12);
        g.setPaint(new GradientPaint(wx + 8, wy + 10, new Color(150, 150, 160, 120), wx + 8, wy + 70, new Color(60, 60, 70, 120)));
        g.fillRoundRect(wx + 8, wy + 10, 80, 62, 10, 10);
        AffineTransform at = g.getTransform();
        g.translate(wx + 48, wy + 41); g.scale(2.0, 2.0); g.translate(-18, 0);
        wpn.drawModel(g);
        g.setTransform(at);
        g.setFont(Fonts.MEDIUM); Util.text(g, wpn.getName(), wx + 96, wy + 26, Color.WHITE);
        g.setFont(Fonts.XL);
        String ammo = wpn.getMag() + " / " + (wpn.hasInfiniteAmmo() ? "INF" : String.valueOf(wpn.getReserve()));
        Util.text(g, ammo, wx + 96, wy + 58, wpn.isReloading() ? new Color(255, 200, 100) : wpn.getMag() == 0 ? new Color(255, 90, 80) : Color.WHITE);
        for (int k = 0; k < PlayerProfile.WEAPON_SLOTS; k++) {
            int bx = wx + 96 + k * 24, by = wy + 64;
            boolean legendary = k == 5 && prof.ownsWeapon(k);
            g.setColor(prof.getCurrentSlot() == k ? new Color(255, 220, 120) : legendary ? new Color(90, 190, 240) : prof.ownsWeapon(k) ? new Color(120, 120, 130) : new Color(50, 50, 55));
            g.fillRoundRect(bx, by, 20, 12, 4, 4);
            g.setFont(Fonts.TINY); g.setColor(Color.BLACK); g.drawString(String.valueOf(k + 1), bx + 7, by + 10);
        }
        if (wpn.isReloading()) { g.setFont(Fonts.SMALL); Util.text(g, "RELOADING...", wx + 12, wy + 74, new Color(255, 200, 100)); }
    }

    private void drawObjectiveArrow(Graphics2D g, GameWorld w) {
        Player p = w.getPlayer();
        float tx = -1, ty = -1, best = Float.MAX_VALUE;
        for (ObjectiveItem it : w.getItems()) if (!it.isCompleted()) {
            float d = it.distanceTo(p);
            if (d < best) { best = d; tx = it.getX(); ty = it.getY(); }
        }
        boolean bossTarget = false;
        if (tx < 0 && w.getBoss() != null) { tx = w.getBoss().getX(); ty = w.getBoss().getY(); bossTarget = true; }
        if (tx < 0) return;
        float sx = tx - w.camera().getX(), sy = ty - w.camera().getY();
        if (sx > 20 && sx < Const.WIDTH - 20 && sy > 20 && sy < Const.HEIGHT - 20) return;
        float cx = Const.WIDTH / 2f, cy = Const.HEIGHT / 2f, ang = (float) Math.atan2(sy - cy, sx - cx);
        float ex = cx + (float) Math.cos(ang) * 270, ey = cy + (float) Math.sin(ang) * 230;
        AffineTransform at = g.getTransform();
        g.translate(ex, ey); g.rotate(ang);
        Path2D ar = new Path2D.Float(); ar.moveTo(18, 0); ar.lineTo(-8, -12); ar.lineTo(-2, 0); ar.lineTo(-8, 12); ar.closePath();
        g.setColor(new Color(0, 0, 0, 140)); g.translate(2, 2); g.fill(ar); g.translate(-2, -2);
        g.setColor(bossTarget ? new Color(255, 80, 70) : new Color(255, 220, 90)); g.fill(ar);
        g.setTransform(at);
        g.setFont(Fonts.SMALL); Util.ctext(g, (int) (Util.dist(tx, ty, p.getX(), p.getY()) / 32) + "m", (int) ex, (int) ey + 26, new Color(255, 230, 160));
    }

    private void drawCrosshair(Graphics2D g, Input in, Weapon wpn) {
        int cx = in.getMouseX(), cy = in.getMouseY();
        float spread = wpn.getCrosshairSpread();
        g.setStroke(new BasicStroke(2f));
        for (int pass = 0; pass < 2; pass++) {
            g.setColor(pass == 0 ? new Color(0, 0, 0, 160) : new Color(255, 255, 255, 230));
            int o = pass == 0 ? 1 : 0;
            for (int k = 0; k < 4; k++) {
                double a = k * Math.PI / 2;
                Util.line(g, cx + Math.cos(a) * spread + o, cy + Math.sin(a) * spread + o, cx + Math.cos(a) * (spread + 7) + o, cy + Math.sin(a) * (spread + 7) + o);
            }
        }
        g.setColor(new Color(255, 80, 70)); g.fillOval(cx - 2, cy - 2, 4, 4);
    }
}
