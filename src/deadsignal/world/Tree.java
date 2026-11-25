package deadsignal.world;

/** Immutable tree placement (its canopy is drawn above the characters). */
public final class Tree {
    private final float x, y, scale;
    private final int kind, variant;

    Tree(float x, float y, int kind, int variant, float scale) { this.x = x; this.y = y; this.kind = kind; this.variant = variant; this.scale = scale; }

    public float getX() { return x; }
    public float getY() { return y; }
    public int getKind() { return kind; }
    public int getVariant() { return variant; }
    public float getScale() { return scale; }
}
