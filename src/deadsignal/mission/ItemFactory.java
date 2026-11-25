package deadsignal.mission;

import deadsignal.entity.item.ObjectiveItem;

/** Functional interface: builds an objective item at a position (e.g. {@code Radio::new}). */
@FunctionalInterface
public interface ItemFactory {
    ObjectiveItem create(float x, float y);
}
