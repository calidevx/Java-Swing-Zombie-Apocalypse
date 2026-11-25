package deadsignal;

import deadsignal.audio.SilentAudio;
import deadsignal.core.Const;
import deadsignal.core.Input;
import deadsignal.core.Util;
import deadsignal.entity.zombie.Zombie;
import deadsignal.game.Game;
import deadsignal.game.GameWorld;
import deadsignal.mission.BossMission;
import deadsignal.screen.*;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;

/** Headless bot that plays the whole campaign and saves screenshots. Used to verify the build. */
final class TestHarness {
    private static int shot;

    private TestHarness() { }

    static void run(String outDir) throws Exception {
        System.setProperty("java.awt.headless", "true");
        File dir = new File(outDir);
        dir.mkdirs();
        Input in = new Input();
        long t0 = System.nanoTime();
        Game game = new Game(in, new SilentAudio());
        System.out.println("startup: " + (System.nanoTime() - t0) / 1_000_000 + " ms");
        game.setGodMode(true);
        game.setQuitHook(() -> { });
        BufferedImage img = new BufferedImage(Const.WIDTH, Const.HEIGHT, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < 90; i++) game.update(1 / 60f);
        snap(game, img, dir, "title");
        in.simulatePress(KeyEvent.VK_ENTER); game.update(1 / 60f);
        snap(game, img, dir, "prologue");
        long frames = 0, renderNs = 0, renders = 0;
        int stageFrames = 0, lastMission = -1, lastChapter = -1, moveTimer = 0;
        boolean dialogShot = false;
        while (!(game.getScreen() instanceof VictoryScreen) && frames < 400000) {
            frames++;
            deadsignal.screen.Screen s = game.getScreen();
            if (s instanceof DialogueScreen) {
                if (!dialogShot && game.getWorld() != null && game.getWorld().hasStarted()) { snap(game, img, dir, "dialog_ch" + (game.getChapterIndex() + 1)); dialogShot = true; }
                ((DialogueScreen) s).finish();
                continue;
            }
            if (s instanceof PlayScreen) {
                GameWorld w = game.getWorld();
                if (game.getChapterIndex() != lastChapter) { lastChapter = game.getChapterIndex(); dialogShot = false; }
                if (w.getMissionIndex() != lastMission) { lastMission = w.getMissionIndex(); stageFrames = 0; }
                float tx = w.getPlayer().getX() + 100, ty = w.getPlayer().getY(), best = Float.MAX_VALUE;
                for (Zombie z : w.getZombies()) { float d = z.distanceTo(w.getPlayer()); if (d < best) { best = d; tx = z.getX(); ty = z.getY(); } }
                if (w.getBoss() != null) { tx = w.getBoss().getX(); ty = w.getBoss().getY(); }
                in.simulateMouse((int) (tx - w.camera().getX()), (int) (ty - w.camera().getY()), true);
                if (--moveTimer <= 0) {
                    moveTimer = 40;
                    in.simulateKey(KeyEvent.VK_W, Util.rf() < 0.3f); in.simulateKey(KeyEvent.VK_S, Util.rf() < 0.3f);
                    in.simulateKey(KeyEvent.VK_A, Util.rf() < 0.3f); in.simulateKey(KeyEvent.VK_D, Util.rf() < 0.3f);
                    if (Util.rf() < 0.2f) in.simulatePress(KeyEvent.VK_SPACE);
                    if (Util.rf() < 0.05f) in.simulatePress(KeyEvent.VK_G);
                    if (Util.rf() < 0.1f) in.simulatePress(KeyEvent.VK_1 + Util.ri(0, 5));
                    if (Util.rf() < 0.01f) in.simulatePress(Util.rf() < 0.5f ? KeyEvent.VK_F : KeyEvent.VK_C);
                }
                game.update(1 / 60f);
                stageFrames++;
                if (frames % 6 == 0) {
                    long a = System.nanoTime();
                    Graphics2D g = img.createGraphics(); game.render(g); g.dispose();
                    renderNs += System.nanoTime() - a; renders++;
                }
                if (w.getMission() != null && game.getScreen() instanceof PlayScreen) {
                    boolean isBoss = w.getMission() instanceof BossMission;
                    if (stageFrames == (isBoss ? 420 : 240)) snap(game, img, dir, "ch" + (game.getChapterIndex() + 1) + "_m" + (w.getMissionIndex() + 1) + (isBoss ? "_boss" : ""));
                    if (stageFrames > (isBoss ? 1100 : 480) && w.isMissionActive()) { w.debugSkipMission(); stageFrames = 0; }
                }
                continue;
            }
            if (s instanceof ResultsScreen) { for (int i = 0; i < 80; i++) game.update(1 / 60f); snap(game, img, dir, "results_ch" + (game.getChapterIndex() + 1)); game.afterResults(); continue; }
            if (s instanceof ShopScreen) {
                game.getProfile().addCash(300);
                ShopScreen shop = (ShopScreen) s;
                shop.buy(game.getShop().getItems().get(Util.ri(0, 7)));
                shop.buy(game.getShop().getItems().get(1));
                for (deadsignal.progress.ShopItem sk : game.getShop().getSkills()) shop.buy(sk);
                game.update(1 / 60f);
                snap(game, img, dir, "shop_after_ch" + (game.getChapterIndex() + 1));
                game.leaveShop();
                continue;
            }
            if (s instanceof GameOverScreen) { game.startChapter(game.getChapterIndex(), true); continue; }
            if (s instanceof PauseScreen) { game.resumePlay(); continue; }
            game.update(1 / 60f);
        }
        snap(game, img, dir, "victory");
        System.out.println("frames simulated: " + frames + "  screen=" + game.getScreen().getClass().getSimpleName()
                + "  kills=" + game.getProfile().getKills() + "  medals=" + game.getProfile().getMedals());
        if (renders > 0) System.out.printf("avg render %.2f ms%n", renderNs / 1e6 / renders);
    }

    private static void snap(Game game, BufferedImage img, File dir, String name) throws Exception {
        Graphics2D g = img.createGraphics(); game.render(g); g.dispose();
        ImageIO.write(img, "png", new File(dir, String.format("%02d_%s.png", shot++, name)));
    }
}
