package deadsignal.gfx;

import deadsignal.core.Const;
import deadsignal.core.Util;

import java.awt.*;

/** Screen-space weather: rain, embers, dust or drifting spores. */
public final class Weather {
    public enum Kind { NONE, RAIN, EMBERS, DUST, SPORES, CONFETTI }

    private final float[][] drops = new float[170][4];

    public Weather() {
        for (float[] w : drops) { w[0] = Util.rf(0, Const.WIDTH); w[1] = Util.rf(0, Const.HEIGHT); w[2] = Util.rf(0.6f, 1.4f); w[3] = Util.rf(0, 1); }
    }

    public void update(float dt, Kind kind, float time) {
        int W = Const.WIDTH, H = Const.HEIGHT;
        for (float[] w : drops) {
            switch (kind) {
                case RAIN: w[1] += 620 * w[2] * dt; w[0] += 130 * w[2] * dt; break;
                case EMBERS: w[1] -= 45 * w[2] * dt; w[0] += (float) Math.sin(time * 2 + w[3] * 10) * 20 * dt; break;
                case DUST: w[0] += 40 * w[2] * dt; w[1] += (float) Math.sin(time + w[3] * 10) * 8 * dt; break;
                case SPORES: w[1] += 18 * w[2] * dt; w[0] += (float) Math.sin(time + w[3] * 10) * 6 * dt; break;
                case CONFETTI: w[1] += 55 * w[2] * dt; w[0] += (float) Math.sin(time * 2 + w[3] * 10) * 30 * dt; break;
                default: return;
            }
            if (w[1] > H + 20) { w[1] = -20; w[0] = Util.rf(-100, W); }
            if (w[1] < -20) { w[1] = H + 20; w[0] = Util.rf(0, W); }
            if (w[0] > W + 20) w[0] = -20;
            if (w[0] < -20) w[0] = W + 20;
        }
    }

    public void render(Graphics2D g, Kind kind, float time) {
        for (float[] w : drops) {
            switch (kind) {
                case RAIN:
                    g.setColor(new Color(170, 180, 210, (int) (60 + 50 * w[3]))); g.setStroke(new BasicStroke(1f));
                    Util.line(g, w[0], w[1], w[0] - 4 * w[2], w[1] - 18 * w[2]); break;
                case EMBERS:
                    g.setColor(new Color(255, (int) (110 + 90 * w[3]), 40, (int) (140 + 50 * Math.sin(time * 5 + w[3] * 20))));
                    Util.oval(g, w[0], w[1], 1.2 + w[2], 1.2 + w[2]); break;
                case DUST:
                    g.setColor(new Color(190, 160, 120, 40)); Util.oval(g, w[0], w[1], 1.5 * w[2], 1.5 * w[2]); break;
                case SPORES:
                    g.setColor(new Color(170, 230, 170, 45)); Util.oval(g, w[0], w[1], 1.2 * w[2], 1.2 * w[2]); break;
                case CONFETTI: {
                    Color[] c = {new Color(255, 90, 200, 150), new Color(90, 220, 255, 150), new Color(255, 220, 80, 150), new Color(150, 255, 120, 150)};
                    g.setColor(c[(int) (w[3] * 4) % 4]);
                    java.awt.geom.AffineTransform at = g.getTransform(); g.translate(w[0], w[1]); g.rotate(time * 3 * w[2] + w[3] * 6);
                    g.fillRect(-2, -1, 4, 2); g.setTransform(at); break;
                }
                default: return;
            }
        }
    }
}
