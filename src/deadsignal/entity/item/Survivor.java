package deadsignal.entity.item;

import deadsignal.core.Util;
import deadsignal.game.GameWorld;
import deadsignal.gfx.Assets;
import deadsignal.gfx.Fonts;
import deadsignal.gfx.ParticleType;

import java.awt.*;

/** A trapped civilian. Waves for help and pays a reward when rescued. */
public final class Survivor extends HoldInteractable {
    private static final int REWARD = 60;
    private float facing;

    public Survivor(float x, float y) { super(x, y, 1.0f, "rescue"); }

    @Override protected void updateActive(GameWorld w, float dt) {
        super.updateActive(w, dt);
        facing = angleTo(w.getPlayer());
    }

    @Override protected void onCompleted(GameWorld w) {
        w.getProfile().addCash(REWARD);
        w.audio().play("heal", 0.8f, 1.2f);
        w.addText("RESCUED  +$" + REWARD, getX(), getY() - 24, new Color(140, 255, 160), 15);
        for (int i = 0; i < 20; i++) w.particles().spawn(ParticleType.GLOW, getX(), getY(), Util.rf(-90, 90), Util.rf(-90, 90), 0.7f, 3, new Color(160, 255, 170));
    }

    @Override protected void renderBody(Graphics2D g, Assets a, float pulse) {
        g.setColor(new Color(0, 0, 0, 80)); Util.oval(g, getX() + 3, getY() + 4, 13, 10);
        Util.drawRotated(g, a.survivor((int) (getAnim() * 6)), getX(), getY(), facing, 1f);
        if (!isCompleted()) { g.setFont(Fonts.SMALL); Util.ctext(g, "HELP!", (int) getX(), (int) (getY() - 26 - pulse * 3), new Color(255, 240, 200)); }
    }
}
