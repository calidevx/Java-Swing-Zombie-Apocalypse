package deadsignal.weapon;

import deadsignal.core.Util;
import deadsignal.entity.Player;
import deadsignal.entity.projectile.TeslaBolt;
import deadsignal.game.GameWorld;
import deadsignal.gfx.ParticleType;

import java.awt.*;

/**
 * LEGENDARY weapon (bonus stage reward). Each shot chains lightning between enemies and
 * deals ENERGY damage - which even the Shepherd's Veil Walk cannot ignore.
 */
public final class TeslaRifle extends Weapon {
    public TeslaRifle() { super("TESLA RIFLE", 5, 24, 144, 4.5f, 1.8f, 3, 3, 36, "zap"); }

    @Override protected Weapon newInstance() { return new TeslaRifle(); }
    @Override protected float soundVolume() { return 0.7f; }

    @Override protected void fireProjectiles(GameWorld w, float x, float y, float aim, float dmg, int shotId) {
        w.addProjectile(new TeslaBolt(x, y, aim + Util.gauss() * 0.02f, 40 * dmg, shotId));
    }

    @Override protected void muzzleEffects(GameWorld w, Player shooter, float x, float y, float aim) {
        for (int i = 0; i < 6; i++)
            w.particles().spawn(ParticleType.SPARK, x, y, (float) Math.cos(aim) * Util.rf(40, 200) + Util.rf(-80, 80), (float) Math.sin(aim) * Util.rf(40, 200) + Util.rf(-80, 80), 0.15f, 1.5f, new Color(170, 235, 255));
        w.addFlash(x, y, 140, 0.08f, new Color(90, 200, 255));
    }

    @Override public void drawModel(Graphics2D g) {
        g.setColor(new Color(52, 60, 72)); g.fillRoundRect(3, -3, 26, 6, 3, 3);
        g.setColor(new Color(196, 120, 60)); for (int k = 0; k < 4; k++) g.fillRect(9 + k * 4, -4, 2, 8);
        g.setColor(new Color(90, 100, 116)); g.fillRect(29, -2, 6, 4);
        g.setColor(new Color(120, 230, 255, 230)); Util.oval(g, 36, 0, 2.6 + Math.random() * 0.8, 2.6);
        g.setColor(new Color(120, 230, 255, 120)); g.fillRect(4, -1, 22, 2);
    }
}
