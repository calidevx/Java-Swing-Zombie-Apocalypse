package deadsignal.world;

import deadsignal.gfx.Weather;

import java.awt.Color;

/**
 * Visual / generation settings for one environment. Fields are private and only
 * configured inside this class; everyone else gets read-only getters (encapsulation).
 */
public final class Theme {
    public static final Theme HOSPITAL = new Theme("St. Agnes Hospital"), CITY = new Theme("Downtown"), SEWER = new Theme("Tunnels"),
            LAB = new Theme("BioGen Labs"), MILITARY = new Theme("Fort Harlow"), INFERNO = new Theme("Ground Zero"),
            THEATER = new Theme("The Majestic Theater");

    private final String name;
    private int ground, ground2, road, side, floor, floor2, wall;
    private boolean lines = true, flashlight, parks;
    private float darkness;
    private Color ambient;
    private int treeKind = 0, lampStyle = 0;
    private Weather.Kind weather = Weather.Kind.NONE;
    private double buildChance = .5, carDensity = .5, treeDensity = .3, propDensity = .3, fire = 0, toxic = 0, rubble = .1;
    private int[] propSet = {Tile.CRATE, Tile.BARREL};
    private Color[] carColors = {new Color(150, 40, 40), new Color(40, 70, 130), new Color(180, 180, 175), new Color(60, 60, 64), new Color(170, 140, 50)};
    private float burntCars = 0.15f;

    private Theme(String name) { this.name = name; }

    static {
        Theme t = HOSPITAL;
        t.ground = Tile.GRASS; t.ground2 = Tile.DIRT; t.road = Tile.ROAD; t.side = Tile.WALK; t.floor = Tile.WTILE; t.floor2 = Tile.WTILE; t.wall = Tile.WALL_WHITE;
        t.darkness = .34f; t.ambient = new Color(12, 14, 40); t.buildChance = .55; t.carDensity = .55; t.treeDensity = .9; t.parks = true;
        t.propSet = new int[]{Tile.CRATE, Tile.BARREL, Tile.DESK};
        t.carColors = new Color[]{new Color(230, 230, 225), new Color(220, 220, 215), new Color(150, 40, 40), new Color(40, 70, 130), new Color(60, 60, 64)};

        t = CITY;
        t.ground = Tile.WALK; t.ground2 = Tile.CONC; t.road = Tile.ROAD; t.side = Tile.WALK; t.floor = Tile.WOOD; t.floor2 = Tile.CARPET; t.wall = Tile.WALL_BRICK;
        t.darkness = .64f; t.ambient = new Color(4, 8, 28); t.buildChance = .6; t.carDensity = .9; t.treeDensity = .9; t.parks = true; t.treeKind = 1;
        t.weather = Weather.Kind.RAIN; t.propSet = new int[]{Tile.CRATE, Tile.BARREL, Tile.DESK}; t.rubble = .3; t.burntCars = 0.3f;

        t = SEWER;
        t.ground = Tile.SEWER; t.ground2 = Tile.RUBBLE; t.road = Tile.WATER; t.side = Tile.CONC; t.floor = Tile.CONC; t.floor2 = Tile.SEWER; t.wall = Tile.WALL_SEWER;
        t.lines = false; t.darkness = .9f; t.ambient = new Color(0, 6, 4); t.flashlight = true; t.buildChance = .8; t.carDensity = 0; t.treeDensity = 0;
        t.treeKind = -1; t.lampStyle = 2; t.propSet = new int[]{Tile.BARREL, Tile.CRATE, Tile.ROCK}; t.rubble = .5; t.weather = Weather.Kind.SPORES;

        t = LAB;
        t.ground = Tile.METAL; t.ground2 = Tile.CONC; t.road = Tile.METAL; t.side = Tile.METAL; t.floor = Tile.WTILE; t.floor2 = Tile.METAL; t.wall = Tile.WALL_STEEL;
        t.lines = false; t.darkness = .5f; t.ambient = new Color(0, 10, 24); t.buildChance = .95; t.carDensity = 0; t.treeDensity = 0; t.treeKind = -1;
        t.lampStyle = 1; t.toxic = 1; t.propSet = new int[]{Tile.TANK, Tile.DESK, Tile.CRATE, Tile.BARREL}; t.propDensity = .6;

        t = MILITARY;
        t.ground = Tile.DIRT; t.ground2 = Tile.GRASS; t.road = Tile.CONC; t.side = Tile.DIRT; t.floor = Tile.CONC; t.floor2 = Tile.METAL; t.wall = Tile.WALL_STEEL;
        t.lines = false; t.darkness = .42f; t.ambient = new Color(30, 12, 4); t.buildChance = .45; t.carDensity = .35; t.treeDensity = .4; t.treeKind = 1;
        t.lampStyle = 3; t.weather = Weather.Kind.DUST; t.propSet = new int[]{Tile.SANDBAG, Tile.CRATE, Tile.BARREL, Tile.SANDBAG}; t.propDensity = .7;
        t.carColors = new Color[]{new Color(78, 88, 56), new Color(70, 78, 50), new Color(96, 92, 66)};

        t = INFERNO;
        t.ground = Tile.ASH; t.ground2 = Tile.RUBBLE; t.road = Tile.ROAD; t.side = Tile.ASH; t.floor = Tile.ASH; t.floor2 = Tile.CONC; t.wall = Tile.WALL_BURNT;
        t.darkness = .55f; t.ambient = new Color(46, 6, 0); t.buildChance = .5; t.carDensity = .5; t.treeDensity = .5; t.treeKind = 2; t.lampStyle = 4;
        t.weather = Weather.Kind.EMBERS; t.fire = 1; t.rubble = .6; t.burntCars = 1f; t.propSet = new int[]{Tile.ROCK, Tile.BARREL, Tile.CRATE};

        t = THEATER;
        t.ground = Tile.CARPET; t.ground2 = Tile.WOOD; t.road = Tile.WOOD; t.side = Tile.CARPET; t.floor = Tile.WOOD; t.floor2 = Tile.CARPET; t.wall = Tile.WALL_BRICK;
        t.lines = false; t.darkness = .72f; t.ambient = new Color(18, 4, 26); t.buildChance = .75; t.carDensity = 0; t.treeDensity = 0; t.treeKind = -1;
        t.lampStyle = 5; t.weather = Weather.Kind.CONFETTI; t.propSet = new int[]{Tile.DESK, Tile.CRATE, Tile.BARREL}; t.propDensity = .5; t.rubble = .2;
    }

    public String getName() { return name; }
    public int getGround() { return ground; }
    public int getGround2() { return ground2; }
    public int getRoad() { return road; }
    public int getSide() { return side; }
    public int getFloor() { return floor; }
    public int getFloor2() { return floor2; }
    public int getWall() { return wall; }
    public boolean hasRoadLines() { return lines; }
    public boolean hasFlashlight() { return flashlight; }
    public boolean hasParks() { return parks; }
    public float getDarkness() { return darkness; }
    public Color getAmbient() { return ambient; }
    public int getTreeKind() { return treeKind; }
    public int getLampStyle() { return lampStyle; }
    public Weather.Kind getWeather() { return weather; }
    public double getBuildChance() { return buildChance; }
    public double getCarDensity() { return carDensity; }
    public double getTreeDensity() { return treeDensity; }
    public double getPropDensity() { return propDensity; }
    public double getFireDensity() { return fire; }
    public double getToxicDensity() { return toxic; }
    public double getRubbleDensity() { return rubble; }
    public int randomProp(java.util.Random r) { return propSet[r.nextInt(propSet.length)]; }
    public Color carColor(int i) { return carColors[i % carColors.length]; }
    public int carColorCount() { return carColors.length; }
    public float getBurntCarChance() { return burntCars; }
}
