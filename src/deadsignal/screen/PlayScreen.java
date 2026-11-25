package deadsignal.screen;

import deadsignal.game.Game;
import deadsignal.game.GameWorld;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

/** Active gameplay: forwards everything to the GameWorld. */
public final class PlayScreen extends Screen {
    private final GameWorld world;

    public PlayScreen(Game game, GameWorld world) { super(game); this.world = world; }

    @Override
    public void update(float dt) {
        if (game.getInput().wasPressed(KeyEvent.VK_ESCAPE, KeyEvent.VK_P)) { game.setScreen(new PauseScreen(game, this)); return; }
        world.update(dt);
        if (game.getScreen() == this && world.isPlayerDeadFor(2.4f)) game.setScreen(new GameOverScreen(game, world));
    }

    @Override public void render(Graphics2D g) { world.render(g, true); }

    public GameWorld getWorld() { return world; }
}
