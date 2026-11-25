package deadsignal.skill;

import deadsignal.core.Util;
import deadsignal.entity.Decoy;
import deadsignal.entity.Player;
import deadsignal.entity.zombie.Zombie;
import deadsignal.game.GameWorld;

import java.awt.Color;

/** ACTIVE [C]: leave an exploding decoy behind and dash out of the crowd. 60 s cooldown. */
public final class DecoyDash extends Skill {
    public DecoyDash(int level) { super(SkillType.DECOY, level); }

    @Override public float getCooldown() { return 60f - 5f * (getLevel() - 1); }
    public float getDecoyHealth() { return 500 + 100 * (getLevel() - 1); }
    public float getBlastDamage() { return 180 + 40 * (getLevel() - 1); }

    @Override
    protected boolean activate(GameWorld w, Player p) {
        w.placeDecoy(new Decoy(p.getX(), p.getY(), getDecoyHealth(), getBlastDamage()));
        float dx = p.getMoveX(), dy = p.getMoveY();
        if (dx == 0 && dy == 0) {                      // standing still: dash away from the crowd
            float cx = 0, cy = 0; int n = 0;
            for (Zombie z : w.getZombies()) if (z.distanceTo(p) < 300) { cx += z.getX(); cy += z.getY(); n++; }
            if (n > 0) { dx = p.getX() - cx / n; dy = p.getY() - cy / n; }
            else { dx = (float) Math.cos(p.getAim()); dy = (float) Math.sin(p.getAim()); }
            float l = Math.max(0.01f, Util.len(dx, dy)); dx /= l; dy /= l;
        }
        p.dash(dx, dy, 0.32f, 4.4f);
        w.audio().play("dash", 1f, 0.8f);
        w.addText("DECOY!", p.getX(), p.getY() - 34, new Color(255, 200, 120), 18);
        return true;
    }
}
