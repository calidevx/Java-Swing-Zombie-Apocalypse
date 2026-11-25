package deadsignal.game;

import deadsignal.audio.Audio;
import deadsignal.core.Input;
import deadsignal.core.Util;
import deadsignal.gfx.Assets;
import deadsignal.progress.Medal;
import deadsignal.progress.PlayerProfile;
import deadsignal.progress.Shop;
import deadsignal.screen.*;
import deadsignal.story.Campaign;
import deadsignal.story.Chapter;
import deadsignal.story.DialogueLine;
import deadsignal.ui.TitleBackdrop;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Top-level controller. Owns the long-lived services and the current {@link Screen}
 * (STATE PATTERN: every screen is a subclass of Screen and the game simply calls
 * screen.update()/render() - polymorphism decides what actually happens).
 */
public final class Game {
    private final Input input;
    private final Audio audio;
    private final Assets assets;
    private final Campaign campaign = new Campaign();
    private final Shop shop = new Shop();
    private final TitleBackdrop backdrop;
    private Screen screen;
    private PlayerProfile profile = new PlayerProfile(), checkpoint;
    private Difficulty difficulty = Difficulty.NORMAL;
    private GameWorld world;
    private PlayScreen playScreen;
    private int chapterIndex;
    private float realTime;
    private boolean godMode;
    private Runnable quitHook = () -> System.exit(0);

    public Game(Input input, Audio audio) {
        this.input = input;
        this.audio = audio;
        this.assets = new Assets();
        this.backdrop = new TitleBackdrop(assets);
        this.screen = new TitleScreen(this);
        audio.music("drone");
    }

    public void update(float dt) {
        input.poll();
        realTime += dt;
        screen.update(dt);
    }

    public void render(Graphics2D g) {
        Util.hq(g);
        screen.render(g);
    }

    // ------------------------------------------------------------------ flow control
    public void setScreen(Screen s) { screen = s; s.onEnter(); }

    public void newGame() {
        profile = new PlayerProfile();
        startChapter(0, false);
    }

    public void startChapter(int index, boolean retry) {
        if (retry && checkpoint != null) profile = checkpoint.copy(); else checkpoint = profile.copy();
        chapterIndex = index;
        Chapter ch = campaign.get(index);
        world = new GameWorld(this, index, ch, profile);
        playScreen = new PlayScreen(this, world);
        List<DialogueLine> intro = new ArrayList<>();
        if (!retry) {
            if (index == 0) intro.addAll(campaign.getPrologue());
            intro.addAll(ch.getIntro());
        }
        audio.music("drone");
        showDialogue(intro, world::beginChapter);
    }

    /** Shows blocking dialogue, then returns to gameplay and runs {@code then}. */
    public void showDialogue(List<DialogueLine> lines, Runnable then) {
        if (lines == null || lines.isEmpty()) { resumePlay(); if (then != null) then.run(); return; }
        setScreen(new DialogueScreen(this, lines, then));
    }

    public void resumePlay() { if (playScreen != null) setScreen(playScreen); }

    public void completeChapter() {
        showDialogue(world.getChapter().getOutro(), () -> setScreen(new ResultsScreen(this, world)));
    }

    public void afterResults() {
        if (world.getChapter().isBonus()) profile.unlockSkills();
        if (chapterIndex == campaign.size() - 1) {
            world.award(Medal.DEAD_SIGNAL);
            showDialogue(campaign.getEpilogue(), () -> setScreen(new VictoryScreen(this)));
        } else setScreen(new ShopScreen(this));
    }

    public void leaveShop() { startChapter(chapterIndex + 1, false); }
    public void retryChapter() { profile.recordDeath(); startChapter(chapterIndex, true); }
    public void returnToTitle() { world = null; playScreen = null; audio.music("drone"); setScreen(new TitleScreen(this)); }
    public void quit() { quitHook.run(); }

    // ------------------------------------------------------------------ accessors
    public Input getInput() { return input; }
    public Audio getAudio() { return audio; }
    public Assets getAssets() { return assets; }
    public Campaign getCampaign() { return campaign; }
    public Shop getShop() { return shop; }
    public TitleBackdrop getBackdrop() { return backdrop; }
    public PlayerProfile getProfile() { return profile; }
    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty d) { difficulty = d; }
    public GameWorld getWorld() { return world; }
    public PlayScreen getPlayScreen() { return playScreen; }
    public int getChapterIndex() { return chapterIndex; }
    public float getRealTime() { return realTime; }
    public Screen getScreen() { return screen; }
    public boolean isGodMode() { return godMode; }
    public void setGodMode(boolean g) { godMode = g; }
    public void setQuitHook(Runnable r) { quitHook = r; }
}
