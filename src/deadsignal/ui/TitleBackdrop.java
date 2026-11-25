package deadsignal.ui;

import deadsignal.core.Const;
import deadsignal.core.Util;
import deadsignal.gfx.*;
import deadsignal.world.GameMap;
import deadsignal.world.Theme;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Rainy panning city with shambling zombies, drawn behind the menus. */
public final class TitleBackdrop {
    private final GameMap map;
    private final Lighting lighting = new Lighting();
    private final Weather weather = new Weather();
    private final List<float[]> walkers = new ArrayList<>();
    private final Assets assets;
    private float pan, time;

    public TitleBackdrop(Assets assets) {
        this.assets = assets;
        this.map = GameMap.create(Theme.CITY, 77, assets);
        for (int i = 0; i < 9; i++) walkers.add(new float[]{Util.rf(0, Const.WIDTH), Util.rf(380, Const.HEIGHT - 20), Util.rf(18, 40), Util.ri(0, 1), Util.ri(0, 2), Util.rf(0, 8)});
    }

    public void update(float dt) {
        time += dt; pan += dt * 22;
        weather.update(dt, Weather.Kind.RAIN, time);
        for (float[] z : walkers) {
            z[0] += z[2] * dt; z[5] += dt * z[2] / 5;
            if (z[0] > Const.WIDTH + 40) { z[0] = -40; z[1] = Util.rf(380, Const.HEIGHT - 20); }
        }
    }

    public void render(Graphics2D g) {
        int maxX = map.getPixelWidth() - Const.WIDTH;
        int sx = (int) Math.abs((pan % (maxX * 2)) - maxX), sy = 900;
        g.drawImage(map.getImage(), 0, 0, Const.WIDTH, Const.HEIGHT, sx, sy, sx + Const.WIDTH, sy + Const.HEIGHT, null);
        for (float[] z : walkers) {
            ZombieSprite s = z[3] < 1 ? ZombieSprite.WALKER : ZombieSprite.RUNNER;
            g.setColor(new Color(0, 0, 0, 70)); Util.oval(g, z[0] + 3, z[1] + 4, 13, 10);
            Util.drawRotated(g, assets.zombie(s, (int) z[4], (int) z[5], false), z[0], z[1], 0, 1.3f);
        }
        lighting.render(g, map, sx, sy, Collections.<LightEmitter>emptyList(), false, 0, 0, 0, time, 0.6f);
        weather.render(g, Weather.Kind.RAIN, time);
        g.drawImage(assets.vignette(), 0, 0, null);
        g.drawImage(assets.vignette(), 0, 0, null);
    }
}
