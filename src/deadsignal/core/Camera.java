package deadsignal.core;

/** Smooth-follow camera with screen shake. Position is encapsulated; callers read view offsets. */
public final class Camera {
    private static final float EDGE_MARGIN = 120;
    private float x, y, shake, offX, offY;

    public void snapTo(float px, float py, int mapW, int mapH) {
        x = px - Const.WIDTH / 2f; y = py - Const.HEIGHT / 2f;
        clamp(mapW, mapH);
    }

    /** Follows the target, leaning slightly toward the point the player is aiming at. */
    public void follow(float tx, float ty, float lookX, float lookY, float dt, int mapW, int mapH) {
        float gx = tx - Const.WIDTH / 2f + (lookX - tx) * 0.18f;
        float gy = ty - Const.HEIGHT / 2f + (lookY - ty) * 0.18f;
        float k = Math.min(1, dt * 7);
        x += (gx - x) * k; y += (gy - y) * k;
        clamp(mapW, mapH);
        shake = Math.max(0, Math.min(20, shake) - dt * 40);
        offX = Util.rf(-shake, shake) * 0.6f; offY = Util.rf(-shake, shake) * 0.6f;
    }

    private void clamp(int mapW, int mapH) {
        x = Util.clamp(x, -EDGE_MARGIN, mapW - Const.WIDTH + EDGE_MARGIN);
        y = Util.clamp(y, -EDGE_MARGIN, mapH - Const.HEIGHT + EDGE_MARGIN);
    }

    public void addShake(float amount) { shake += amount; }
    public float getX() { return x; }
    public float getY() { return y; }
    public int viewX() { return (int) (x + offX); }
    public int viewY() { return (int) (y + offY); }
    public boolean isVisible(float wx, float wy, float margin) {
        return wx > x - margin && wx < x + Const.WIDTH + margin && wy > y - margin && wy < y + Const.HEIGHT + margin;
    }
}
