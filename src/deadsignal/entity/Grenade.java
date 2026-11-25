package deadsignal.entity;

import deadsignal.core.Util;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;

import java.awt.*;

/** A lobbed frag grenade. Subclassed by {@link EnemyGrenade} which overrides the blast and look. */
public class Grenade extends Entity {
    private final float startX, startY, targetX, targetY, flightTime, fuseTime;
    private float timer, height;

    public Grenade(float sx, float sy, float tx, float ty, float flightTime, float fuseTime) {
        super(sx, sy, 5);
        startX = sx; startY = sy; targetX = tx; targetY = ty; this.flightTime = flightTime; this.fuseTime = fuseTime;
    }

    @Override
    public final void update(GameWorld w, float dt) {
        timer += dt;
        if (timer < flightTime) {
            float k = timer / flightTime;
            setPosition(startX + (targetX - startX) * k, startY + (targetY - startY) * k);
            height = (float) Math.sin(k * Math.PI) * 46;
        } else { setPosition(targetX, targetY); height = 0; }
        if (timer >= fuseTime) { detonate(w); remove(); }
    }

    /** Hook: what the blast does. Player grenades hurt every hostile. */
    protected void detonate(GameWorld w) { w.explode(getX(), getY(), 120, 140, Faction.PLAYER); }
    protected Color bodyColor() { return new Color(60, 84, 46); }

    @Override
    public void render(Graphics2D g, Assets a) {
        g.setColor(new Color(0, 0, 0, 90)); Util.oval(g, getX(), getY() + 3, 5, 3);
        g.setColor(bodyColor()); Util.oval(g, getX(), getY() - height, 5, 5);
        if (((int) (timer * 10)) % 2 == 0) { g.setColor(new Color(255, 60, 40)); Util.oval(g, getX(), getY() - height, 1.5, 1.5); }
    }

    protected float getTargetX() { return targetX; }
    protected float getTargetY() { return targetY; }
    protected float getFuseProgress() { return Math.min(1, timer / fuseTime); }
}
