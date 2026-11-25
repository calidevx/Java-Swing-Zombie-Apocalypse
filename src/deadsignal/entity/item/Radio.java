package deadsignal.entity.item;

import deadsignal.gfx.ItemSprite;

public final class Radio extends SupplyItem {
    public Radio(float x, float y) { super(x, y, ItemSprite.RADIO, 1.6f, "use", "SIGNAL ANSWERED"); }
}
