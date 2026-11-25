package deadsignal.entity;

import deadsignal.core.Util;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;
import deadsignal.gfx.LightEmitter;
import deadsignal.gfx.ParticleType;

import java.awt.*;

/**
 * A dummy wearing Kai's jacket with a blaring siren. Zombies nearby attack IT instead of the player.
 * When destroyed (or after 25 s) it detonates. A friendly Creature - another subclass of the same base.
 */
public final class Decoy extends Creature implements LightEmitter {
    public static final float LURE_RADIUS = 650, MAX_LIFE = 25f;
    private final float blastDamage;
    private float age;

    public Decoy(float x, float y, float health, float blastDamage) {
        super(x, y, 13, health);
        this.blastDamage = blastDamage;
    }

    @Override
    public void update(GameWorld w, float dt) {
        tickFlash(dt);
        age += dt;
        if (((int) (age * 2)) != ((int) ((age - dt) * 2)))
            w.particles().spawn(ParticleType.RING, getX(), getY(), 0, 0, 0.6f, 90, new Color(255, 80, 60));
        if (age > MAX_LIFE) kill(w, DamageType.PURGE);
    }

    @Override protected void onDeath(GameWorld w, DamageType type) {
        remove();
        w.addText("DECOY DETONATED", getX(), getY() - 30, new Color(255, 180, 90), 16);
        w.explode(getX(), getY(), 170, blastDamage, Faction.PLAYER);
    }

    @Override
    public void render(Graphics2D g, Assets a) {
        float x = getX(), y = getY();
        g.setColor(new Color(0, 0, 0, 90)); Util.oval(g, x + 3, y + 5, 14, 10);
        g.setColor(new Color(120, 88, 56)); g.setStroke(Util.st(4)); Util.line(g, x - 14, y, x + 14, y);
        g.setColor(isFlashing() ? Color.WHITE : new Color(50, 86, 146)); Util.oval(g, x, y, 9, 13);
        g.setColor(new Color(230, 220, 90)); g.setStroke(new BasicStroke(2f)); Util.line(g, x - 3, y - 11, x - 3, y + 11);
        g.setColor(new Color(196, 168, 120)); Util.oval(g, x + 1, y, 7, 7);
        g.setColor(new Color(120, 96, 64)); g.setStroke(new BasicStroke(1.2f)); Util.line(g, x - 2, y - 4, x + 4, y + 4); Util.line(g, x + 4, y - 4, x - 2, y + 4);
        boolean on = ((int) (age * 6)) % 2 == 0;
        g.setColor(on ? new Color(255, 50, 40) : new Color(90, 20, 20)); Util.oval(g, x + 1, y - 2, 3, 3);
        g.setColor(new Color(0, 0, 0, 160)); g.fillRect((int) x - 18, (int) y - 26, 36, 5);
        g.setColor(new Color(255, 170, 70)); g.fillRect((int) x - 18, (int) y - 26, (int) (36 * getHealthFraction()), 5);
        g.setColor(new Color(255, 255, 255, 140)); g.fillRect((int) x - 18, (int) y - 20, (int) (36 * (1 - age / MAX_LIFE)), 1);
    }

    @Override public float getLightX() { return getX(); }
    @Override public float getLightY() { return getY(); }
    @Override public float getLightRadius() { return 90; }
    @Override public float getLightIntensity(float t) { return ((int) (age * 6)) % 2 == 0 ? 0.9f : 0.4f; }
    @Override public Color getGlowColor() { return new Color(255, 60, 40); }
    @Override public int getGlowAlpha() { return ((int) (age * 6)) % 2 == 0 ? 60 : 0; }
}
