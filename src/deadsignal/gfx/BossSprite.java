package deadsignal.gfx;

/** Which boss body to draw. Each constant knows its frame size. */
public enum BossSprite {
    CENSER(108), TANK(136), BROODMOTHER(150), SUBJECT13(96), GRAVES(140), SHEPHERD(172), MAESTRO(112);

    private final int frameSize;
    BossSprite(int size) { frameSize = size; }
    public int getFrameSize() { return frameSize; }
}
