package deadsignal.world;

/** Package-private record of an abandoned car occupying two tiles. */
final class CarWreck {
    final int tx, ty, colorIndex;
    final boolean horizontal, burnt, flipped;

    CarWreck(int tx, int ty, boolean horizontal, int colorIndex, boolean burnt, boolean flipped) {
        this.tx = tx; this.ty = ty; this.horizontal = horizontal; this.colorIndex = colorIndex; this.burnt = burnt; this.flipped = flipped;
    }
}
