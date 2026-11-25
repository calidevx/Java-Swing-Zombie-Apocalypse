package deadsignal.entity.boss;

import deadsignal.core.Util;
import deadsignal.entity.ability.BloodRite;
import deadsignal.entity.projectile.AcidGlob;
import deadsignal.entity.zombie.ZombieType;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;
import deadsignal.gfx.BossSprite;
import deadsignal.gfx.ParticleType;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Chapter 3 boss. Spits acid, casts BLOOD RITE and births SPIDERLINGS - one per leg she still has.
 * Every sixth of her health lost severs a leg: fewer spiderlings, but she gets slower and angrier.
 */
public final class Broodmother extends Boss {
    private static final int MAX_LEGS = 6;
    private int legs = MAX_LEGS;
    private float volleyCooldown = 2.5f, broodCooldown = 2f, riteCooldown = 4f;

    public Broodmother(float x, float y, float hpMul) {
        super("THE BROODMOTHER", "Hive matriarch", BossSprite.BROODMOTHER, x, y, 44, 2400 * hpMul, 15);
    }

    @Override
    protected void think(GameWorld w, float dt, float dx, float dy, float d) {
        volleyCooldown -= dt; broodCooldown -= dt; riteCooldown -= dt;
        float frac = getHealthFraction(), face = (float) Math.atan2(dy, dx);
        int shouldHave = Math.max(1, (int) Math.ceil(frac * MAX_LEGS - 0.001f));
        while (legs > shouldHave) severLeg(w);
        float legSpeed = 0.55f + 0.45f * legs / MAX_LEGS;
        chase(w, (d < 200 ? 22 : 52) * legSpeed, dt, dx, dy, d);
        faceToward(face, 2.5f * dt);
        if (volleyCooldown <= 0 && canSeePlayer()) {
            int n = legs <= 3 ? 7 : 5;
            for (int k = 0; k < n; k++)
                w.addProjectile(new AcidGlob(getX() + (float) Math.cos(face) * 40, getY() + (float) Math.sin(face) * 40, face + (k - (n - 1) / 2f) * 0.2f, 280, 12, 7, Math.min(1.8f, d / 280 + 0.3f)));
            volleyCooldown = legs <= 3 ? 1.6f : 2.2f;
            attackPose(0.4f);
            w.audio().play("splat", 0.8f, 0.8f);
        }
        if (riteCooldown <= 0 && d < 650) {
            w.addEffect(new BloodRite(w.getPlayer().getX(), w.getPlayer().getY()));
            announce(w, "BLOOD RITE", new Color(255, 70, 70));
            riteCooldown = legs <= 3 ? 4.5f : 6.5f;
        }
        if (broodCooldown <= 0) {
            summon(w, legs, ZombieType.SPIDERLING);       // one spiderling per remaining leg
            announce(w, "BROOD x" + legs, new Color(200, 120, 230));
            broodCooldown = 5.5f;
            w.audio().play("splat", 1f, 0.5f);
        }
    }

    private void severLeg(GameWorld w) {
        legs--;
        for (int i = 0; i < 26; i++)
            w.particles().spawn(ParticleType.BLOOD, getX() + Util.rf(-40, 40), getY() + Util.rf(-40, 40), Util.rf(-200, 200), Util.rf(-200, 200), Util.rf(0.4f, 0.9f), Util.rf(2, 4), new Color(120, 30, 130));
        for (int i = 0; i < 3; i++)
            w.particles().spawn(ParticleType.CHUNK, getX(), getY(), Util.rf(-220, 220), Util.rf(-220, 220), 0.9f, 4, new Color(64, 30, 64)).withSpin(Util.rf(-20, 20));
        w.addText("LEG SEVERED!  (" + legs + " left)", getX(), getY() - 60, new Color(230, 150, 255), 17);
        w.audio().play("roar", 0.8f, 1.6f);
        w.shake(10);
    }

    /** POLYMORPHISM: draw the frame that matches how many legs she has left. */
    @Override protected BufferedImage bodyFrame(Assets a, int frame, boolean attack, boolean flash) { return a.broodmother(legs, frame, attack, flash); }

    public int getLegs() { return legs; }
}
