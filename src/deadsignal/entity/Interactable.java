package deadsignal.entity;

import deadsignal.game.GameWorld;

/** Something the player can use by holding E next to it. */
public interface Interactable {
    boolean canInteract(Player player);
    void interact(GameWorld world, float dt);
}
