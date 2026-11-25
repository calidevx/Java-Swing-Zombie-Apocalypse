package deadsignal.entity.item;

import deadsignal.core.Util;
import deadsignal.entity.DamageType;
import deadsignal.entity.Damageable;
import deadsignal.entity.zombie.ZombieType;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;
import deadsignal.gfx.ParticleType;

import java.awt.*;
import java.util.Random;

/** A pulsing growth that spawns infected. It is BOTH an ObjectiveItem and Damageable. */
public final class HiveNode extends ObjectiveItem implements Damageable {
    private final float maxHealth;
    private float health, spawnTimer = 2f;

    public HiveNode(float x, float y, float maxHealth) { super(x, y, 26); this.maxHealth = maxHealth; this.health = maxHealth; }

    @Override protected void updateActive(GameWorld w, float dt) {
        spawnTimer -= dt;
        if (spawnTimer <= 0 && distanceTo(w.getPlayer()) < 650 && w.zombieCount() < w.getMaxZombies() + 10) {
            spawnTimer = Util.rf(4.5f, 7f);
            for (int k = 0; k < 2; k++) {
                float a = Util.rf(0, 6.28f), x = getX() + (float) Math.cos(a) * 40, y = getY() + (float) Math.sin(a) * 40;
                if (!w.getMap().collides(x, y, 10)) w.spawnZombie(Util.rf() < 0.5f ? ZombieType.CRAWLER : ZombieType.WALKER, x, y);
            }
            for (int k = 0; k < 10; k++) w.particles().spawn(ParticleType.DOT, getX(), getY(), Util.rf(-90, 90), Util.rf(-90, 90), 0.5f, 3, new Color(170, 60, 190));
        }
    }

    @Override public void takeDamage(float amount, DamageType type, GameWorld w) {
        if (isCompleted()) return;
        health -= amount;
        if (Util.rf() < 0.5f) w.particles().spawn(ParticleType.DOT, getX() + Util.rf(-15, 15), getY() + Util.rf(-15, 15), Util.rf(-80, 80), Util.rf(-80, 80), 0.4f, 2.5f, new Color(170, 50, 190));
        if (health <= 0) complete(w);
    }

    @Override protected void onCompleted(GameWorld w) {
        for (int k = 0; k < 40; k++) w.particles().spawn(ParticleType.BLOOD, getX(), getY(), Util.rf(-220, 220), Util.rf(-220, 220), Util.rf(0.4f, 0.9f), Util.rf(2, 4), new Color(120, 30, 140));
        Util.splat(w.getMap().getDecals(), getX(), getY(), 34, new Random(), new Color(80, 20, 90));
        w.shake(10);
        w.audio().play("splat", 1f, 0.6f);
        w.audio().play("roar", 0.4f, 1.6f);
        w.addText("HIVE NODE DESTROYED  +$75", getX(), getY() - 30, new Color(230, 150, 255), 15);
        w.getProfile().addCash(75);
    }

    @Override public boolean isDead() { return isCompleted(); }
    @Override public Color getMinimapColor() { return new Color(220, 100, 255); }

    @Override protected void renderItem(Graphics2D g, Assets a, float pulse) {
        float s = 1 + 0.08f * pulse, x = getX(), y = getY();
        g.setColor(new Color(0, 0, 0, 90)); Util.oval(g, x + 4, y + 6, 28 * s, 22 * s);
        for (int k = 0; k < 6; k++) {
            double an = k * 1.05 + getAnim() * 0.4;
            g.setColor(new Color(90, 30, 100)); g.setStroke(Util.st(5));
            Util.line(g, x, y, x + Math.cos(an) * 40, y + Math.sin(an) * 40);
        }
        g.setPaint(new RadialGradientPaint(x - 6, y - 6, 30 * s, new float[]{0, 0.4f, 1}, new Color[]{new Color(255, 170, 255), new Color(190, 60, 200), new Color(70, 20, 80)}));
        Util.oval(g, x, y, 26 * s, 24 * s);
        g.setColor(new Color(40, 10, 50)); g.setStroke(new BasicStroke(2f)); Util.ring(g, x, y, 26 * s, 24 * s);
        g.setColor(new Color(0, 0, 0, 160)); g.fillRect((int) x - 24, (int) y - 40, 48, 5);
        g.setColor(new Color(220, 110, 255)); g.fillRect((int) x - 24, (int) y - 40, (int) (48 * Math.max(0, health) / maxHealth), 5);
    }
}
