package deadsignal.screen;

import deadsignal.game.Game;

import java.awt.Graphics2D;

/**
 * ABSTRACT game state (State pattern). Title, dialogue, gameplay, pause, results, shop,
 * game over and victory are all Screens; Game holds one and calls update()/render() on it.
 */
public abstract class Screen {
    protected final Game game;

    protected Screen(Game game) { this.game = game; }

    public abstract void update(float dt);
    public abstract void render(Graphics2D g);
    /** Hook: called whenever this screen becomes active. */
    public void onEnter() { }
}
