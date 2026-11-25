package deadsignal.weapon;

import deadsignal.core.Util;
import deadsignal.entity.Player;
import deadsignal.entity.projectile.FlameJet;
import deadsignal.game.GameWorld;

import java.awt.*;

/** Sprays FlameJets instead of bullets; overrides the muzzle effects and accuracy rules. */
public final class Flamethrower extends Weapon {
    public Flamethrower() { super("FLAMETHROWER", 4, 120, 360, 28f, 2.2f, 0.5f, 0.4f, 35, "flame"); }

    @Override protected Weapon newInstance() { return new Flamethrower(); }
    @Override protected boolean countsForAccuracy() { return false; }
    @Override protected float soundVolume() { return 0.5f; }
    @Override public boolean showMuzzleFlash() { return false; }

    @Override protected void fireProjectiles(GameWorld w, float x, float y, float aim, float dmg, int shotId) {
        w.addProjectile(new FlameJet(x, y, aim + Util.gauss() * 0.16f, 360 * Util.rf(0.9f, 1.1f), 0.5f * Util.rf(0.85f, 1.1f), 7 * dmg));
    }

    @Override protected void muzzleEffects(GameWorld w, Player shooter, float x, float y, float aim) {
        w.addFlash(x, y, 120, 0.08f, new Color(255, 150, 50));
    }

    @Override public void drawModel(Graphics2D g) {
        g.setColor(new Color(170, 34, 30)); g.fillRoundRect(1, -5, 15, 10, 6, 6);
        g.setColor(new Color(110, 110, 116)); g.fillRect(14, -2, 17, 4);
        g.setColor(new Color(40, 40, 44)); g.fillRect(30, -3, 4, 6);
        g.setColor(new Color(255, 160, 40, 220)); Util.oval(g, 36, 0, 2.5 + Math.random(), 2);
    }
}
