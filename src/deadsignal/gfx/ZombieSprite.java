package deadsignal.gfx;

/** Which zombie body to draw. Each constant knows its frame size. */
public enum ZombieSprite {
    WALKER(48), RUNNER(48), BRUTE(84), SPITTER(54), EXPLODER(60), CRAWLER(44), SOLDIER(48), SPIDERLING(40);

    private final int frameSize;
    ZombieSprite(int size) { frameSize = size; }
    public int getFrameSize() { return frameSize; }
}
