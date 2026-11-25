package deadsignal.progress;

import deadsignal.skill.SkillType;

import java.awt.Graphics2D;

/** Shop card that unlocks / levels up a special skill (only after the Bonus Stage). */
public final class SkillPurchase extends ShopItem {
    private final SkillType type;

    public SkillPurchase(SkillType type) { super(type.getTitle(), type.getDescription()); this.type = type; }

    @Override public boolean isLocked(PlayerProfile p) { return !p.areSkillsUnlocked(); }
    @Override public int getCost(PlayerProfile p) { return type.costForLevel(p.getSkillLevel(type)); }
    @Override public boolean isAvailable(PlayerProfile p) { return p.getSkillLevel(type) < SkillType.MAX_LEVEL; }
    @Override protected void applyTo(PlayerProfile p) { p.raiseSkill(type); }
    @Override public int getLevel(PlayerProfile p) { return p.getSkillLevel(type); }
    @Override public String getStatusText(PlayerProfile p) {
        if (isLocked(p)) return "LOCKED - clear the Bonus Stage";
        int lv = p.getSkillLevel(type);
        return (lv == 0 ? "NOT OWNED" : "LEVEL " + lv + " / " + SkillType.MAX_LEVEL) + "   KEY: " + type.getKeyLabel();
    }
    @Override public void drawIcon(Graphics2D g, int x, int y) { type.drawIcon(g, x, y, 1.1f); }

    public SkillType getType() { return type; }
}
