package deadsignal.entity.boss;

import deadsignal.entity.Player;
import deadsignal.entity.projectile.EnergyOrb;
import deadsignal.entity.projectile.HomingSpore;
import deadsignal.game.GameWorld;

import java.awt.*;

/** The caster aspect: keeps its distance and fills the air with spore rings and homing spores. */
public final class BlightAspect extends ShepherdAspect {
    private float ringCooldown = 1.2f, sporeCooldown = 2.5f, spin;

    public BlightAspect(Shepherd parent, float x, float y, float hp) { super(parent, "BLIGHT", new Color(150, 240, 90), x, y, 24, hp); }

    @Override protected float getSpeed() { return 120; }
    @Override protected float getPreferredRange() { return 340; }

    @Override
    protected void attack(GameWorld w, Player p, float dt, float dx, float dy, float d) {
        ringCooldown -= dt; sporeCooldown -= dt;
        if (ringCooldown <= 0) {
            spin += 0.4f;
            for (int k = 0; k < 12; k++) w.addProjectile(new EnergyOrb(getX(), getY(), spin + k / 12f * 6.283f, 190, 8, 6, new Color(160, 240, 90)));
            ringCooldown = 2.1f; w.audio().play("zap", 0.5f, 0.9f);
        }
        if (sporeCooldown <= 0) {
            float face = (float) Math.atan2(dy, dx);
            for (int k = 0; k < 2; k++) w.addProjectile(new HomingSpore(getX(), getY(), face + (k == 0 ? -0.7f : 0.7f)));
            sporeCooldown = 3.4f;
        }
    }
}
