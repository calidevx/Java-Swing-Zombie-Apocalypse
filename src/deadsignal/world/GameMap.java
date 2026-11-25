package deadsignal.world;

import deadsignal.core.Const;
import deadsignal.core.Util;
import deadsignal.gfx.Assets;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The tile grid, collision queries and the zombie flow-field.
 * Raw arrays are package-private so only the generator/renderer in this package can touch them;
 * every other class must go through the public query methods (encapsulation).
 */
public final class GameMap {
    final int w, h;
    final int[] tiles, under, dist, queue;
    final boolean[] reach;
    final Theme theme;
    final List<Tree> trees = new ArrayList<>();
    final List<MapLight> lights = new ArrayList<>();
    final List<CarWreck> cars = new ArrayList<>();
    final List<int[]> lamps = new ArrayList<>();
    int spawnTx, spawnTy;
    private final java.util.Map<Integer, Integer> raisedRocks = new java.util.HashMap<>();
    BufferedImage image, minimap;
    Graphics2D decals;

    GameMap(int w, int h, Theme theme) {
        this.w = w; this.h = h; this.theme = theme;
        tiles = new int[w * h]; under = new int[w * h]; dist = new int[w * h]; queue = new int[w * h]; reach = new boolean[w * h];
    }

    /** Factory method: generate + pre-render a map for the given theme. */
    public static GameMap create(Theme theme, long seed, Assets assets) {
        GameMap m = MapGenerator.generate(theme, seed);
        MapRenderer.render(m, assets);
        return m;
    }

    public static int toTile(float px) { return (int) Math.floor(px / Const.TILE); }

    // ------------------------------------------------------------ read-only accessors
    public int getWidth() { return w; }
    public int getHeight() { return h; }
    public int getPixelWidth() { return w * Const.TILE; }
    public int getPixelHeight() { return h * Const.TILE; }
    public Theme getTheme() { return theme; }
    public BufferedImage getImage() { return image; }
    public BufferedImage getMinimap() { return minimap; }
    /** Graphics of the ground layer: blood, corpses and scorch marks are painted permanently here. */
    public Graphics2D getDecals() { return decals; }
    public List<Tree> getTrees() { return Collections.unmodifiableList(trees); }
    public List<MapLight> getLights() { return Collections.unmodifiableList(lights); }
    public float getSpawnX() { return spawnTx * Const.TILE + 16; }
    public float getSpawnY() { return spawnTy * Const.TILE + 16; }

    public int getTile(int x, int y) { return (x < 0 || y < 0 || x >= w || y >= h) ? Tile.WALL_BRICK : tiles[y * w + x]; }
    public boolean isSolid(int x, int y) { return Tile.isSolid(getTile(x, y)); }
    public boolean isSolidAt(float px, float py) { return isSolid(toTile(px), toTile(py)); }
    public int tileAt(float px, float py) { return getTile(toTile(px), toTile(py)); }
    public boolean isReachableTile(int x, int y) { return x >= 0 && y >= 0 && x < w && y < h && reach[y * w + x]; }

    /** Every tile in the 3x3 block around (x,y) is walkable. */
    public boolean isOpenArea(int x, int y) {
        for (int oy = -1; oy <= 1; oy++) for (int ox = -1; ox <= 1; ox++) if (isSolid(x + ox, y + oy)) return false;
        return true;
    }

    /** Circle vs. tile-grid collision. */
    public boolean collides(float x, float y, float r) {
        int x0 = toTile(x - r), x1 = toTile(x + r), y0 = toTile(y - r), y1 = toTile(y + r);
        for (int ty = y0; ty <= y1; ty++) for (int tx = x0; tx <= x1; tx++) {
            if (!isSolid(tx, ty)) continue;
            float nx = Util.clamp(x, tx * 32, tx * 32 + 32), ny = Util.clamp(y, ty * 32, ty * 32 + 32);
            float dx = x - nx, dy = y - ny;
            if (dx * dx + dy * dy < r * r) return true;
        }
        return false;
    }

    public boolean hasLineOfSight(float ax, float ay, float bx, float by) {
        float d = Util.dist(ax, ay, bx, by);
        int n = (int) (d / 14) + 1;
        for (int i = 1; i < n; i++) {
            float k = i / (float) n;
            if (isSolidAt(ax + (bx - ax) * k, ay + (by - ay) * k)) return false;
        }
        return true;
    }

    // ------------------------------------------------------------ dynamic terrain (Fault Line ability)
    /** Turns a walkable tile into rock until {@link #lowerRock} is called. @return true if a rock was raised. */
    public boolean raiseRock(int tx, int ty) {
        if (tx <= 0 || ty <= 0 || tx >= w - 1 || ty >= h - 1 || isSolid(tx, ty)) return false;
        int i = ty * w + tx;
        raisedRocks.put(i, tiles[i]);
        tiles[i] = Tile.ROCK;
        return true;
    }
    public void lowerRock(int tx, int ty) {
        Integer original = raisedRocks.remove(ty * w + tx);
        if (original != null) tiles[ty * w + tx] = original;
    }

    // ------------------------------------------------------------ flow field (zombie path-finding)
    /** Breadth-first search from the given pixel position over all walkable tiles. */
    public void computeFlowField(float px, float py) { bfs(toTile(px), toTile(py)); }

    void bfs(int sx, int sy) {
        Arrays.fill(dist, -1);
        if (sx < 0 || sy < 0 || sx >= w || sy >= h || isSolid(sx, sy)) {
            boolean found = false;
            for (int r = 1; r < 4 && !found; r++)
                for (int oy = -r; oy <= r && !found; oy++) for (int ox = -r; ox <= r && !found; ox++)
                    if (!isSolid(sx + ox, sy + oy)) { sx += ox; sy += oy; found = true; }
            if (!found) return;
        }
        int head = 0, tail = 0, s = sy * w + sx;
        dist[s] = 0; queue[tail++] = s;
        while (head < tail) {
            int c = queue[head++], cx = c % w, cy = c / w, d = dist[c] + 1;
            if (cx > 0 && dist[c - 1] < 0 && !Tile.isSolid(tiles[c - 1])) { dist[c - 1] = d; queue[tail++] = c - 1; }
            if (cx < w - 1 && dist[c + 1] < 0 && !Tile.isSolid(tiles[c + 1])) { dist[c + 1] = d; queue[tail++] = c + 1; }
            if (cy > 0 && dist[c - w] < 0 && !Tile.isSolid(tiles[c - w])) { dist[c - w] = d; queue[tail++] = c - w; }
            if (cy < h - 1 && dist[c + w] < 0 && !Tile.isSolid(tiles[c + w])) { dist[c + w] = d; queue[tail++] = c + w; }
        }
    }

    /** Steps (in tiles) from the last flow-field origin, or -1 if unreachable. */
    public int stepsAt(int tx, int ty) { return (tx < 0 || ty < 0 || tx >= w || ty >= h) ? -1 : dist[ty * w + tx]; }
    public boolean isReachableNow(float px, float py) { int tx = toTile(px), ty = toTile(py); return stepsAt(tx, ty) >= 0 && !isSolid(tx, ty); }

    /** Unit vector pointing one tile closer to the flow-field origin, or null. */
    public float[] flowDirection(float x, float y) {
        int tx = toTile(x), ty = toTile(y);
        int cd = stepsAt(tx, ty);
        if (cd < 0) return null;
        int best = cd, bx = -9, by = -9;
        for (int oy = -1; oy <= 1; oy++) for (int ox = -1; ox <= 1; ox++) {
            if ((ox == 0 && oy == 0) || isSolid(tx + ox, ty + oy)) continue;
            if (ox != 0 && oy != 0 && (isSolid(tx + ox, ty) || isSolid(tx, ty + oy))) continue;
            int nd = stepsAt(tx + ox, ty + oy);
            if (nd >= 0 && nd < best) { best = nd; bx = ox; by = oy; }
        }
        if (bx == -9) return null;
        float gx = (tx + bx) * 32 + 16 - x, gy = (ty + by) * 32 + 16 - y, l = Util.len(gx, gy);
        return l < 0.01f ? null : new float[]{gx / l, gy / l};
    }
}
