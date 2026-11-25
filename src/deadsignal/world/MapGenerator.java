package deadsignal.world;

import deadsignal.core.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/** Procedural city-block generator. Single responsibility: fill a GameMap's tiles. */
final class MapGenerator {
    private MapGenerator() { }

    static GameMap generate(Theme th, long seed) {
        for (int a = 0; a < 12; a++) {
            GameMap m = attempt(th, seed + a * 7919L, a == 11);
            if (m != null) return m;
        }
        throw new IllegalStateException("map generation failed");
    }

    private static GameMap attempt(Theme th, long seed, boolean force) {
        Random r = new Random(seed);
        GameMap m = new GameMap(80, 64, th);
        int W = m.w, H = m.h;
        Arrays.fill(m.tiles, th.getGround());
        for (int k = 0; k < 40; k++) {
            int cx = r.nextInt(W), cy = r.nextInt(H), rad = 2 + r.nextInt(4);
            for (int dy = -rad; dy <= rad; dy++) for (int dx = -rad; dx <= rad; dx++) {
                int x = cx + dx, y = cy + dy;
                if (x > 0 && y > 0 && x < W - 1 && y < H - 1 && dx * dx + dy * dy <= rad * rad && r.nextInt(4) != 0) m.tiles[y * W + x] = th.getGround2();
            }
        }
        int[] rows = {8 + r.nextInt(3), 29 + r.nextInt(4), 50 + r.nextInt(4)};
        int[] cols = {9 + r.nextInt(3), 29 + r.nextInt(3), 49 + r.nextInt(3), 68 + r.nextInt(3)};
        boolean[] isRoad = new boolean[W * H];
        for (int y0 : rows) for (int x = 1; x < W - 1; x++) for (int k = -1; k <= 3; k++) {
            int i = (y0 + k) * W + x;
            if (k == -1 || k == 3) { if (!isRoad[i]) m.tiles[i] = th.getSide(); } else { m.tiles[i] = th.getRoad(); isRoad[i] = true; }
        }
        for (int x0 : cols) for (int y = 1; y < H - 1; y++) for (int k = -1; k <= 3; k++) {
            int i = y * W + x0 + k;
            if (k == -1 || k == 3) { if (!isRoad[i]) m.tiles[i] = th.getSide(); } else { m.tiles[i] = th.getRoad(); isRoad[i] = true; }
        }
        if (th.hasRoadLines()) {
            for (int y0 : rows) for (int x = 1; x < W - 1; x++) if (!inBand(x, cols)) m.tiles[(y0 + 1) * W + x] = Tile.LINE_H;
            for (int x0 : cols) for (int y = 1; y < H - 1; y++) if (!inBand(y, rows)) m.tiles[y * W + x0 + 1] = Tile.LINE_V;
        }
        List<int[]> xr = new ArrayList<>(), yr = new ArrayList<>();
        int s = 1;
        for (int c : cols) { xr.add(new int[]{s, c - 2}); s = c + 4; }
        xr.add(new int[]{s, W - 2});
        s = 1;
        for (int c : rows) { yr.add(new int[]{s, c - 2}); s = c + 4; }
        yr.add(new int[]{s, H - 2});
        for (int[] bx : xr) for (int[] by : yr) fillBlock(m, r, th, bx[0], by[0], bx[1], by[1]);
        if (th.getTreeKind() >= 0) for (int y = 2; y < H - 2; y++) for (int x = 2; x < W - 2; x++) {
            int tt = m.tiles[y * W + x];
            if ((tt == Tile.GRASS || tt == Tile.ASH || tt == Tile.DIRT) && r.nextDouble() < 0.012 * th.getTreeDensity() && m.isOpenArea(x, y)) addTree(m, r, x, y);
        }
        placeCars(m, r, th, rows, cols);
        placeHazards(m, r, th);
        for (int i = 0; i < W * H; i++)
            if ((m.tiles[i] == th.getGround() || m.tiles[i] == th.getGround2()) && i % W > 0 && r.nextDouble() < th.getRubbleDensity() * 0.02) m.tiles[i] = Tile.RUBBLE;
        for (int x = 0; x < W; x++) { m.tiles[x] = th.getWall(); m.tiles[(H - 1) * W + x] = th.getWall(); }
        for (int y = 0; y < H; y++) { m.tiles[y * W] = th.getWall(); m.tiles[y * W + W - 1] = th.getWall(); }
        chooseSpawn(m, rows, cols);
        m.bfs(m.spawnTx, m.spawnTy);
        int walk = 0, reach = 0;
        for (int i = 0; i < W * H; i++) {
            if (!Tile.isSolid(m.tiles[i])) walk++;
            m.reach[i] = m.dist[i] >= 0;
            if (m.reach[i]) reach++;
        }
        if (!force && reach < walk * 0.7) return null;
        placeLights(m, r, th, rows);
        return m;
    }

    private static void fillBlock(GameMap m, Random r, Theme th, int bx0, int by0, int bx1, int by1) {
        int bw = bx1 - bx0 + 1, bh = by1 - by0 + 1;
        if (bw < 4 || bh < 4) return;
        double roll = r.nextDouble();
        if (roll < th.getBuildChance() && bw >= 9 && bh >= 8) {
            building(m, r, th, bx0 + r.nextInt(2), by0 + r.nextInt(2), bx1 - r.nextInt(2), by1 - r.nextInt(2));
        } else if (th.hasParks() && roll < th.getBuildChance() + 0.25) {
            for (int y = by0; y <= by1; y++) for (int x = bx0; x <= bx1; x++) m.tiles[y * m.w + x] = r.nextInt(9) == 0 ? Tile.DIRT : Tile.GRASS;
            for (int y = by0 + 1; y < by1; y++) for (int x = bx0 + 1; x < bx1; x++)
                if (r.nextDouble() < 0.07 * th.getTreeDensity() && m.isOpenArea(x, y)) addTree(m, r, x, y);
        } else {
            for (int y = by0 + 1; y < by1; y++) for (int x = bx0 + 1; x < bx1; x++)
                if (r.nextDouble() < 0.035 * th.getPropDensity() && m.isOpenArea(x, y)) set(m, x, y, th.randomProp(r));
            if (th == Theme.MILITARY && bw > 10 && bh > 8) sandbagRing(m, r, bx0 + bw / 2, by0 + bh / 2);
        }
    }

    static void set(GameMap m, int x, int y, int v) {
        if (x <= 0 || y <= 0 || x >= m.w - 1 || y >= m.h - 1) return;
        int i = y * m.w + x;
        if (Tile.isSolid(v) && !Tile.isSolid(m.tiles[i])) m.under[i] = m.tiles[i];
        m.tiles[i] = v;
    }
    private static boolean inBand(int v, int[] bands) { for (int b : bands) if (v >= b && v <= b + 2) return true; return false; }
    private static boolean roadish(int t, Theme th) { return t == Tile.ROAD || t == Tile.LINE_H || t == Tile.LINE_V || (t == th.getRoad() && !Tile.isSolid(t)); }
    private static void addTree(GameMap m, Random r, int x, int y) {
        set(m, x, y, Tile.TREE);
        m.trees.add(new Tree(x * 32 + 16, y * 32 + 16, m.theme.getTreeKind(), r.nextInt(2), 0.85f + r.nextFloat() * 0.35f));
    }
    private static void sandbagRing(GameMap m, Random r, int cx, int cy) {
        for (int x = cx - 2; x <= cx + 2; x++) if (r.nextInt(5) > 0 && m.isOpenArea(x, cy - 2)) set(m, x, cy - 2, Tile.SANDBAG);
        for (int y = cy - 1; y <= cy + 1; y++) if (m.isOpenArea(cx - 3, y)) set(m, cx - 3, y, Tile.SANDBAG);
    }

    private static void building(GameMap m, Random r, Theme th, int x0, int y0, int x1, int y1) {
        int fl = r.nextBoolean() ? th.getFloor() : th.getFloor2();
        for (int y = y0; y <= y1; y++) for (int x = x0; x <= x1; x++) {
            if (x == x0 || x == x1 || y == y0 || y == y1) set(m, x, y, th.getWall()); else m.tiles[y * m.w + x] = fl;
        }
        int bw = x1 - x0, bh = y1 - y0, xm = -99, ym = -99;
        if (bh >= 10) ym = y0 + bh / 2 + r.nextInt(3) - 1;
        if (bw >= 12) xm = x0 + bw / 2 + r.nextInt(3) - 1;
        if (xm > 0) for (int y = y0 + 1; y < y1; y++) set(m, xm, y, th.getWall());
        if (ym > 0) for (int x = x0 + 1; x < x1; x++) set(m, x, ym, th.getWall());
        if (xm > 0) { if (ym > 0) { gapV(m, xm, y0 + 1, ym - 2, fl); gapV(m, xm, ym + 1, y1 - 2, fl); } else gapV(m, xm, y0 + 1, y1 - 2, fl); }
        if (ym > 0) { if (xm > 0) { gapH(m, ym, x0 + 1, xm - 2, fl); gapH(m, ym, xm + 1, x1 - 2, fl); } else gapH(m, ym, x0 + 1, x1 - 2, fl); }
        int[] sides = {0, 1, 2, 3};
        for (int i = 3; i > 0; i--) { int j = r.nextInt(i + 1); int t = sides[i]; sides[i] = sides[j]; sides[j] = t; }
        int doors = 2 + r.nextInt(2);
        for (int d = 0; d < doors; d++) {
            int side = sides[d];
            for (int tries = 0; tries < 20; tries++) {
                if (side < 2) {
                    int x = Util.ri(x0 + 2, Math.max(x0 + 2, x1 - 3));
                    if (Math.abs(x - xm) <= 1 || Math.abs(x + 1 - xm) <= 1) continue;
                    int y = side == 0 ? y0 : y1;
                    m.tiles[y * m.w + x] = fl; m.tiles[y * m.w + x + 1] = fl;
                } else {
                    int y = Util.ri(y0 + 2, Math.max(y0 + 2, y1 - 3));
                    if (Math.abs(y - ym) <= 1 || Math.abs(y + 1 - ym) <= 1) continue;
                    int x = side == 2 ? x0 : x1;
                    m.tiles[y * m.w + x] = fl; m.tiles[(y + 1) * m.w + x] = fl;
                }
                break;
            }
        }
        for (int y = y0 + 1; y < y1; y++) for (int x = x0 + 1; x < x1; x++)
            if (r.nextDouble() < 0.09 * th.getPropDensity() && m.isOpenArea(x, y)) set(m, x, y, th.randomProp(r));
        if (th == Theme.INFERNO) for (int y = y0; y <= y1; y++) for (int x = x0; x <= x1; x++)
            if (m.getTile(x, y) == th.getWall() && r.nextInt(5) == 0) m.tiles[y * m.w + x] = Tile.RUBBLE;
    }
    private static void gapV(GameMap m, int x, int ya, int yb, int fl) {
        int y = Util.ri(ya, Math.max(ya, yb));
        m.tiles[y * m.w + x] = fl; if (y + 1 < m.h - 1) m.tiles[(y + 1) * m.w + x] = fl;
    }
    private static void gapH(GameMap m, int y, int xa, int xb, int fl) {
        int x = Util.ri(xa, Math.max(xa, xb));
        m.tiles[y * m.w + x] = fl; if (x + 1 < m.w - 1) m.tiles[y * m.w + x + 1] = fl;
    }

    private static void placeCars(GameMap m, Random r, Theme th, int[] rows, int[] cols) {
        int ncar = (int) (th.getCarDensity() * 40);
        for (int i = 0, placed = 0; i < ncar * 6 && placed < ncar; i++) {
            boolean horiz = r.nextBoolean();
            int x, y;
            if (horiz) { y = rows[r.nextInt(rows.length)] + r.nextInt(3); x = 2 + r.nextInt(m.w - 5); }
            else { x = cols[r.nextInt(cols.length)] + r.nextInt(3); y = 2 + r.nextInt(m.h - 5); }
            int x2 = horiz ? x + 1 : x, y2 = horiz ? y : y + 1;
            if (!roadish(m.getTile(x, y), th) || !roadish(m.getTile(x2, y2), th)) continue;
            if (horiz ? (inBand(x, cols) || inBand(x2, cols)) : (inBand(y, rows) || inBand(y2, rows))) continue;
            if (m.isSolid(x - 1, y) || m.isSolid(x2 + 1, y2) || m.isSolid(x, y - 1) || m.isSolid(x2, y2 + 1)) continue;
            set(m, x, y, Tile.CAR); set(m, x2, y2, Tile.CAR);
            m.cars.add(new CarWreck(x, y, horiz, r.nextInt(th.carColorCount()), r.nextFloat() < th.getBurntCarChance(), r.nextBoolean()));
            placed++;
        }
    }

    private static void placeHazards(GameMap m, Random r, Theme th) {
        int[][] specs = {{(int) (20 * th.getFireDensity()), Tile.FIRE}, {(int) (16 * th.getToxicDensity()), Tile.TOXIC}};
        for (int[] spec : specs) for (int k = 0; k < spec[0]; k++) {
            int cx = 3 + r.nextInt(m.w - 6), cy = 3 + r.nextInt(m.h - 6), rad = 1 + r.nextInt(2);
            boolean any = false;
            for (int dy = -rad; dy <= rad; dy++) for (int dx = -rad; dx <= rad; dx++) {
                int x = cx + dx, y = cy + dy;
                if (!m.isSolid(x, y) && dx * dx + dy * dy <= rad * rad && r.nextInt(3) != 0) { m.tiles[y * m.w + x] = spec[1]; any = true; }
            }
            if (any) {
                boolean fire = spec[1] == Tile.FIRE;
                m.lights.add(new MapLight(cx * 32 + 16, cy * 32 + 16, fire ? 170 : 110, fire ? MapLight.Style.PULSE : MapLight.Style.STEADY, fire ? 1 : 2, true));
            }
        }
    }

    private static void chooseSpawn(GameMap m, int[] rows, int[] cols) {
        int bestD = Integer.MAX_VALUE;
        for (int y = 2; y < 16; y++) for (int x = 2; x < 16; x++) {
            int tt = m.getTile(x, y);
            if (Tile.isSolid(tt) || Tile.isHazard(tt)) continue;
            int d = (x - 6) * (x - 6) + (y - 6) * (y - 6);
            if (m.isOpenArea(x, y) && d < bestD) { bestD = d; m.spawnTx = x; m.spawnTy = y; }
        }
        if (bestD == Integer.MAX_VALUE) { m.spawnTx = cols[0] + 1; m.spawnTy = rows[0] + 1; }
    }

    private static void placeLights(GameMap m, Random r, Theme th, int[] rows) {
        int W = m.w, H = m.h;
        switch (th.getLampStyle()) {
            case 0:
                for (int y0 : rows) for (int x = 3; x < W - 3; x += 7) for (int yy : new int[]{y0 - 1, y0 + 3}) {
                    if (!m.isSolid(x, yy) && m.getTile(x, yy) == th.getSide()) {
                        m.lamps.add(new int[]{x, yy});
                        m.lights.add(new MapLight(x * 32 + 16, yy * 32 + 16, 170, r.nextInt(6) == 0 ? MapLight.Style.FLICKER : MapLight.Style.STEADY, 0, th == Theme.CITY));
                    }
                }
                break;
            case 1:
                for (int k = 0; k < 600 && m.lights.size() < 60; k++) {
                    int x = 2 + r.nextInt(W - 4), y = 2 + r.nextInt(H - 4), tt = m.getTile(x, y);
                    if (tt == th.getFloor() || tt == th.getFloor2())
                        m.lights.add(new MapLight(x * 32 + 16, y * 32 + 16, 130, r.nextInt(3) == 0 ? MapLight.Style.FLICKER : MapLight.Style.STEADY, 3, true));
                }
                break;
            case 2:
                for (int k = 0; k < 400 && m.lamps.size() < 30; k++) {
                    int x = 2 + r.nextInt(W - 4), y = 2 + r.nextInt(H - 4);
                    if (!m.isSolid(x, y)) {
                        m.lamps.add(new int[]{x, y});
                        m.lights.add(new MapLight(x * 32 + 16, y * 32 + 16, 105, r.nextInt(3) == 0 ? MapLight.Style.FLICKER : MapLight.Style.STEADY, 2, true));
                    }
                }
                break;
            case 3:
                for (int k = 0; k < 400 && m.lamps.size() < 16; k++) {
                    int x = 2 + r.nextInt(W - 4), y = 2 + r.nextInt(H - 4);
                    if (!m.isSolid(x, y) && m.isOpenArea(x, y)) {
                        m.lamps.add(new int[]{x, y});
                        m.lights.add(new MapLight(x * 32 + 16, y * 32 + 16, 240, MapLight.Style.STEADY, 0, false));
                    }
                }
                break;
            case 5: // theater: coloured stage lights
                for (int k = 0; k < 600 && m.lights.size() < 55; k++) {
                    int x = 2 + r.nextInt(W - 4), y = 2 + r.nextInt(H - 4);
                    if (!m.isSolid(x, y)) {
                        int[] palette = {0, 3, 4, 5, 6};
                        m.lights.add(new MapLight(x * 32 + 16, y * 32 + 16, 150, r.nextBoolean() ? MapLight.Style.PULSE : MapLight.Style.STEADY, palette[r.nextInt(palette.length)], true));
                    }
                }
                break;
            default: break;
        }
    }
}
