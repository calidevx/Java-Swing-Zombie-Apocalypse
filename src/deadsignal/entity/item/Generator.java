package deadsignal.entity.item;

import deadsignal.gfx.ItemSprite;

public final class Generator extends SupplyItem {
    public Generator(float x, float y) { super(x, y, ItemSprite.GENERATOR, 2.0f, "restart", "GENERATOR ONLINE"); }
}
