package deadsignal.world;

/** Tile ids. Ground tiles are below 20, solid tiles are 20 and above. */
public final class Tile {
    public static final int GRASS = 0, DIRT = 1, ROAD = 2, LINE_H = 3, LINE_V = 4, WALK = 5, WOOD = 6, WTILE = 7, METAL = 8, CONC = 9,
            SEWER = 10, WATER = 11, ASH = 12, FIRE = 13, TOXIC = 14, RUBBLE = 15, CARPET = 16, GROUND_COUNT = 17;
    public static final int WALL_BRICK = 20, WALL_WHITE = 21, WALL_STEEL = 22, WALL_SEWER = 23, SANDBAG = 24, FENCE = 25, CRATE = 26,
            BARREL = 27, TANK = 28, WALL_BURNT = 29, TREE = 30, CAR = 31, ROCK = 32, DESK = 33;

    private Tile() { }

    public static boolean isSolid(int t) { return t >= 20; }
    public static boolean isWall(int t) { return t == WALL_BRICK || t == WALL_WHITE || t == WALL_STEEL || t == WALL_SEWER || t == WALL_BURNT; }
    public static boolean isAnimated(int t) { return t == WATER || t == FIRE || t == TOXIC; }
    public static boolean isHazard(int t) { return t == FIRE || t == TOXIC; }
}
