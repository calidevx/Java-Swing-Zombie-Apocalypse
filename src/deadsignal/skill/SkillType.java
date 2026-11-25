package deadsignal.skill;

import deadsignal.core.Util;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Path2D;

/**
 * FACTORY ENUM for skills: every constant creates its own Skill subclass and draws its own icon
 * (constant-specific methods = polymorphism inside an enum).
 */
public enum SkillType {
    BLOODLUST("BLOODLUST", "Your hits heal you while your kill combo is x50 or higher", 0, 600) {
        @Override public Skill create(int level) { return new Bloodlust(level); }
        @Override public void drawIcon(Graphics2D g, float x, float y, float s) {
            g.setColor(new Color(200, 20, 40));
            Path2D drop = new Path2D.Float();
            drop.moveTo(x, y - 12 * s); drop.quadTo(x + 11 * s, y + 2 * s, x, y + 10 * s); drop.quadTo(x - 11 * s, y + 2 * s, x, y - 12 * s);
            g.fill(drop);
            g.setColor(new Color(255, 150, 160)); Util.oval(g, x - 3 * s, y + 1 * s, 2.5 * s, 3.5 * s);
        }
    },
    SHOCKWAVE("SHOCKWAVE", "Stun and damage every enemy around you", KeyEvent.VK_F, 700) {
        @Override public Skill create(int level) { return new Shockwave(level); }
        @Override public void drawIcon(Graphics2D g, float x, float y, float s) {
            g.setColor(new Color(120, 210, 255)); g.setStroke(new BasicStroke(2.5f * s));
            for (int k = 1; k <= 3; k++) Util.ring(g, x, y, 4 * k * s, 4 * k * s);
            g.setColor(new Color(255, 240, 150)); g.fillPolygon(new int[]{(int) (x - 2 * s), (int) (x + 4 * s), (int) x, (int) (x + 3 * s), (int) (x - 4 * s), (int) x},
                    new int[]{(int) (y - 9 * s), (int) (y - 9 * s), (int) (y - 1 * s), (int) (y - 1 * s), (int) (y + 9 * s), (int) (y + 1 * s)}, 6);
        }
    },
    DECOY("DECOY DASH", "Drop a 500 HP decoy that lures zombies and explodes, then dash away", KeyEvent.VK_C, 900) {
        @Override public Skill create(int level) { return new DecoyDash(level); }
        @Override public void drawIcon(Graphics2D g, float x, float y, float s) {
            g.setColor(new Color(150, 110, 70)); g.setStroke(Util.st(2.5f * s));
            Util.line(g, x - 4 * s, y + 11 * s, x - 4 * s, y - 6 * s); Util.line(g, x - 11 * s, y - 2 * s, x + 3 * s, y - 2 * s);
            g.setColor(new Color(50, 86, 146)); g.fillRoundRect((int) (x - 9 * s), (int) (y - 4 * s), (int) (10 * s), (int) (10 * s), 3, 3);
            g.setColor(new Color(200, 170, 120)); Util.oval(g, x - 4 * s, y - 8 * s, 3.5 * s, 3.5 * s);
            g.setColor(new Color(255, 60, 40)); g.setStroke(new BasicStroke(2f * s));
            Util.line(g, x + 4 * s, y + 2 * s, x + 12 * s, y - 4 * s); Util.line(g, x + 8 * s, y - 5 * s, x + 12 * s, y - 4 * s); Util.line(g, x + 12 * s, y - 4 * s, x + 11 * s, y);
        }
    };

    public static final int MAX_LEVEL = 5;
    private final String title, description;
    private final int key, baseCost;

    SkillType(String title, String description, int key, int baseCost) {
        this.title = title; this.description = description; this.key = key; this.baseCost = baseCost;
    }

    public abstract Skill create(int level);
    public abstract void drawIcon(Graphics2D g, float x, float y, float scale);

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getKey() { return key; }
    public String getKeyLabel() { return key == 0 ? "PASSIVE" : KeyEvent.getKeyText(key); }
    /** Price to reach the next level (level 0 -> 1 is the unlock). */
    public int costForLevel(int currentLevel) { return baseCost * (currentLevel + 1); }
}
