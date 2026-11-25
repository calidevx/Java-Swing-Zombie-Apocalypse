package deadsignal.game;

import deadsignal.audio.Audio;
import deadsignal.core.*;
import deadsignal.entity.*;
import deadsignal.entity.boss.Boss;
import deadsignal.entity.boss.BossType;
import deadsignal.entity.item.ObjectiveItem;
import deadsignal.entity.pickup.*;
import deadsignal.entity.projectile.Projectile;
import deadsignal.entity.zombie.Zombie;
import deadsignal.entity.zombie.ZombieType;
import deadsignal.gfx.*;
import deadsignal.mission.Mission;
import deadsignal.progress.Medal;
import deadsignal.progress.PlayerProfile;
import deadsignal.story.Chapter;
import deadsignal.ui.Announcer;
import deadsignal.ui.Hud;
import deadsignal.ui.RadioChatter;
import deadsignal.world.GameMap;
import deadsignal.world.Tile;
import deadsignal.world.Tree;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * The live state of one chapter: map, entities, effects, missions and camera.
 * All collections are private - entities interact with the world only through the public
 * methods below (spawn, explode, addPickup, onZombieKilled...), never by touching the lists directly.
 * Updating and drawing is fully POLYMORPHIC: the world just calls update()/render() on every Entity.
 */
public final class GameWorld {
    private static final float MISSION_GAP = 1.8f;

    private final Game game;
    private final Chapter chapter;
    private final int chapterIndex;
    private final GameMap map;
    private final Player player;
    private final PlayerProfile profile;
    private final Difficulty difficulty;

    private final List<Zombie> zombies = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Pickup> pickups = new ArrayList<>();
    private final List<ObjectiveItem> items = new ArrayList<>();
    private final List<Grenade> grenades = new ArrayList<>();
    private final List<Entity> effects = new ArrayList<>();
    private final List<Creature> structures = new ArrayList<>();
    private final List<Flash> flashes = new ArrayList<>();
    private final List<FloatingText> texts = new ArrayList<>();
    private final List<Damageable> damageables = new ArrayList<>();
    private final List<LightEmitter> emitters = new ArrayList<>();
    private Boss boss;
    private Decoy decoy;

    private final ParticleSystem particles = new ParticleSystem();
    private final Camera camera = new Camera();
    private final Lighting lighting = new Lighting();
    private final Weather weather = new Weather();
    private final ChapterStats stats = new ChapterStats();
    private final ComboCounter combo = new ComboCounter();
    private final SpawnDirector spawner = new SpawnDirector();
    private final Announcer announcer = new Announcer();
    private final RadioChatter radio = new RadioChatter();
    private final Hud hud = new Hud();

    private int missionIndex;
    private Mission mission;
    private boolean missionActive, bossClean, chapterFinished;
    private float missionDelay = -1, time, slowMo, hurtFlash, whiteFlash, flowTimer, lastBossX, lastBossY;
    private int shotCounter;

    public GameWorld(Game game, int chapterIndex, Chapter chapter, PlayerProfile profile) {
        this.game = game; this.chapterIndex = chapterIndex; this.chapter = chapter; this.profile = profile;
        this.difficulty = game.getDifficulty();
        this.map = GameMap.create(chapter.getTheme(), chapter.getSeed(), game.getAssets());
        profile.refillAllAmmo();
        this.player = new Player(map.getSpawnX(), map.getSpawnY(), profile);
        player.setGodMode(game.isGodMode());
        camera.snapTo(player.getX(), player.getY(), map.getPixelWidth(), map.getPixelHeight());
        map.computeFlowField(player.getX(), player.getY());
        for (int i = 0; i < chapter.getPrePopulate(); i++) {
            float[] pos = findSpawnPoint(520, 2400, false);
            if (pos != null) spawnZombie(chapter.getSpawnTable().pick(Util.RNG), pos[0], pos[1]);
        }
    }

    // ================================================================== missions
    public void beginChapter() {
        announcer.showCard(chapter.getLabel() + "  -  " + chapter.getTitle(), chapter.getSubtitle(), chapter.isBonus() ? new Color(255, 200, 90) : new Color(230, 220, 200));
        beginMission(0);
    }

    private void beginMission(int index) {
        missionIndex = index;
        mission = chapter.getMissions().get(index);
        missionActive = false;
        missionDelay = -1;
        items.clear();
        game.showDialogue(mission.getBriefing(), this::activateMission);
    }

    private void activateMission() {
        missionActive = true;
        map.computeFlowField(player.getX(), player.getY());
        mission.start(this);
        game.getAudio().play("objective", 0.7f, 1f);
        announcer.showBanner("NEW OBJECTIVE", new Color(255, 220, 120));
    }

    /** Called by a Mission when its goal is reached. */
    public void onMissionComplete(Mission m) {
        if (m != mission || !missionActive) return;
        missionActive = false;
        missionDelay = MISSION_GAP;
        game.getAudio().play("objective");
        announcer.showBanner("OBJECTIVE COMPLETE", new Color(140, 255, 150));
        profile.addCash(50);
        addText("+$50", player.getX(), player.getY() - 30, new Color(255, 220, 90), 16);
    }

    private void updateMissionFlow(float dt) {
        if (missionActive) mission.update(this, dt);
        if (missionDelay > 0) {
            missionDelay -= dt;
            if (missionDelay <= 0) {
                missionDelay = -1;
                if (missionIndex + 1 < chapter.getMissions().size()) beginMission(missionIndex + 1);
                else { chapterFinished = true; items.clear(); game.completeChapter(); }
            }
        }
    }

    /** Developer shortcut (F9) and test-bot helper: finish the current objective immediately. */
    public void debugSkipMission() {
        if (!missionActive) return;
        if (boss != null && !boss.isDead()) boss.forceDefeat(this);
        else mission.forceComplete(this);
    }

    // ================================================================== main update
    public void update(float rdt) {
        Input in = game.getInput();
        if (in.wasPressed(KeyEvent.VK_F9)) debugSkipMission();
        float scale = 1f;
        if (slowMo > 0) { slowMo -= rdt; scale = 0.35f; }
        float dt = rdt * scale;
        time += dt;
        if (!chapterFinished) stats.tick(rdt);

        player.update(this, dt);
        flowTimer -= dt;
        if (flowTimer <= 0) { flowTimer = 0.2f; map.computeFlowField(player.getX(), player.getY()); }
        rebuildDamageables();

        for (int i = 0; i < zombies.size(); i++) { Zombie z = zombies.get(i); if (!z.isRemoved()) z.update(this, dt); }
        if (boss != null) boss.update(this, dt);
        updateAll(projectiles, dt);
        updateAll(grenades, dt);
        updateAll(pickups, dt);
        updateAll(items, dt);
        updateAll(effects, dt);
        updateAll(structures, dt);
        if (decoy != null) { decoy.update(this, dt); if (decoy.isRemoved()) decoy = null; }
        flashes.removeIf(f -> !f.update(dt));
        texts.removeIf(t -> !t.update(rdt));
        zombies.removeIf(Entity::isRemoved);
        if (boss != null && boss.isRemoved()) boss = null;

        updateMissionFlow(dt);
        spawner.update(this, dt);
        particles.update(dt, map.getDecals());
        radio.update(rdt, game.getAudio());
        announcer.update(rdt, game.getAudio());
        combo.update(dt);
        weather.update(rdt, map.getTheme().getWeather(), game.getRealTime());
        camera.follow(player.getX(), player.getY(), mouseWorldX(), mouseWorldY(), rdt, map.getPixelWidth(), map.getPixelHeight());
        hurtFlash = Math.max(0, hurtFlash - rdt * 2.5f);
        whiteFlash = Math.max(0, whiteFlash - rdt * 1.5f);
        game.getAudio().music(boss != null && !boss.isDead() ? boss.getMusicTrack() : "drone");
    }

    /** POLYMORPHISM: one loop updates any kind of Entity, whatever its concrete class. */
    private <T extends Entity> void updateAll(List<T> list, float dt) {
        for (int i = 0; i < list.size(); i++) { T e = list.get(i); if (!e.isRemoved()) e.update(this, dt); }
        list.removeIf(Entity::isRemoved);
    }

    private void rebuildDamageables() {
        damageables.clear();
        damageables.addAll(zombies);
        for (Creature c : structures) if (!c.isDead()) damageables.add(c);
        if (boss != null) damageables.add(boss);
        for (ObjectiveItem it : items) if (it instanceof Damageable) damageables.add((Damageable) it);
    }

    // ================================================================== rendering
    public void render(Graphics2D g, boolean withHud) {
        Assets a = game.getAssets();
        int sx = camera.viewX(), sy = camera.viewY();
        g.setColor(new Color(8, 8, 10)); g.fillRect(0, 0, Const.WIDTH, Const.HEIGHT);
        int sx0 = Math.max(0, sx), sy0 = Math.max(0, sy), sx1 = Math.min(map.getPixelWidth(), sx + Const.WIDTH), sy1 = Math.min(map.getPixelHeight(), sy + Const.HEIGHT);
        g.drawImage(map.getImage(), sx0 - sx, sy0 - sy, sx1 - sx, sy1 - sy, sx0, sy0, sx1, sy1, null);
        java.awt.geom.AffineTransform base = g.getTransform();
        g.translate(-sx, -sy);
        drawAnimatedTiles(g, a, sx, sy);
        for (Entity e : effects) e.render(g, a);
        for (ObjectiveItem it : items) it.render(g, a);
        for (Pickup p : pickups) p.render(g, a);
        for (Creature c : structures) c.render(g, a);
        if (decoy != null) decoy.render(g, a);
        for (Zombie z : zombies) if (camera.isVisible(z.getX(), z.getY(), 60)) z.render(g, a);
        if (boss != null) boss.render(g, a);
        player.render(g, a);
        for (Grenade gr : grenades) gr.render(g, a);
        for (Projectile p : projectiles) p.render(g, a);
        particles.render(g);
        drawCanopies(g, a);
        for (FloatingText t : texts) t.render(g);
        g.setTransform(base);

        emitters.clear();
        emitters.addAll(flashes);
        emitters.addAll(items);
        for (Projectile p : projectiles) if (p instanceof LightEmitter) emitters.add((LightEmitter) p);
        if (boss instanceof LightEmitter) emitters.add((LightEmitter) boss);
        if (decoy != null) emitters.add(decoy);
        lighting.render(g, map, sx, sy, emitters, true, player.getX(), player.getY(), player.getAim(), game.getRealTime(), -1);
        weather.render(g, map.getTheme().getWeather(), game.getRealTime());
        g.drawImage(a.vignette(), 0, 0, null);
        if (!player.isDead() && player.getHealthFraction() < 0.3f) {
            Composite oc = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Util.clamp(0.45f + 0.35f * (float) Math.sin(game.getRealTime() * 6), 0, 1)));
            g.drawImage(a.redVignette(), 0, 0, null);
            g.setComposite(oc);
        }
        if (hurtFlash > 0) { g.setColor(new Color(200, 0, 0, (int) (hurtFlash * 140))); g.fillRect(0, 0, Const.WIDTH, Const.HEIGHT); }
        if (whiteFlash > 0) { g.setColor(new Color(255, 255, 255, (int) (Math.min(1, whiteFlash) * 180))); g.fillRect(0, 0, Const.WIDTH, Const.HEIGHT); }
        if (withHud) hud.render(g, this, a);
    }

    private void drawAnimatedTiles(Graphics2D g, Assets a, int sx, int sy) {
        int tx0 = Math.max(0, sx / 32), ty0 = Math.max(0, sy / 32);
        int tx1 = Math.min(map.getWidth() - 1, (sx + Const.WIDTH) / 32), ty1 = Math.min(map.getHeight() - 1, (sy + Const.HEIGHT) / 32);
        int fr = (int) (game.getRealTime() * 6);
        for (int ty = ty0; ty <= ty1; ty++) for (int tx = tx0; tx <= tx1; tx++) {
            int id = map.getTile(tx, ty);
            if (!Tile.isAnimated(id)) continue;
            g.drawImage(a.ground(id, fr + tx * 3 + ty), tx * 32, ty * 32, null);
            if (id == Tile.FIRE && Util.rf() < 0.02f)
                particles.spawn(ParticleType.FIRE, tx * 32 + Util.rf(4, 28), ty * 32 + Util.rf(4, 28), Util.rf(-10, 10), Util.rf(-60, -30), 0.6f, 5, new Color(255, 150, 40));
        }
    }

    private void drawCanopies(Graphics2D g, Assets a) {
        float t = game.getRealTime();
        for (Tree tr : map.getTrees()) {
            if (!camera.isVisible(tr.getX(), tr.getY(), 60)) continue;
            BufferedImage img = a.canopy(tr.getKind(), tr.getVariant());
            float sway = (float) Math.sin(t * 1.3 + tr.getX()) * 0.03f;
            Composite oc = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Util.dist(tr.getX(), tr.getY(), player.getX(), player.getY()) < 40 ? 0.45f : 0.95f));
            Util.drawRotated(g, img, tr.getX(), tr.getY() - 6, sway + tr.getX() % 6, tr.getScale());
            g.setComposite(oc);
        }
    }

    // ================================================================== services used by entities
    public Zombie spawnZombie(ZombieType type, float x, float y) {
        float hpMul = (1 + 0.12f * chapterIndex) * difficulty.getEnemyHealth();
        float spMul = 1 + 0.03f * chapterIndex;
        Zombie z = type.create(x, y, hpMul, spMul);
        zombies.add(z);
        return z;
    }

    public void spawnBoss(BossType type) {
        float[] pos = pickReachableTile(7, 13, null, 0, true);
        boss = type.create(pos[0], pos[1], difficulty.getEnemyHealth());
        bossClean = true;
        announcer.showCard(boss.getName(), boss.getSubtitle(), new Color(255, 80, 70));
        game.getAudio().play("roar");
        shake(16);
        for (int i = 0; i < 30; i++)
            particles.spawn(ParticleType.SMOKE, boss.getX() + Util.rf(-40, 40), boss.getY() + Util.rf(-40, 40), Util.rf(-80, 80), Util.rf(-80, 80), 1.2f, 16, new Color(40, 30, 30, 170));
    }

    public void addProjectile(Projectile p) { projectiles.add(p); }
    public void addPickup(Pickup p) { pickups.add(p); }
    public void addItem(ObjectiveItem it) { items.add(it); }
    public void addGrenade(Grenade g) { grenades.add(g); }
    public void addEffect(Entity e) { effects.add(e); }
    /** Stationary hostile objects such as turrets (they can be shot). */
    public void addStructure(Creature c) { structures.add(c); }
    public int structureCount() { return structures.size(); }
    public void addFlash(float x, float y, float r, float t, Color c) { flashes.add(new Flash(x, y, r, t, c)); }
    public void addText(String s, float x, float y, Color c, int size) { texts.add(new FloatingText(s, x, y, c, size)); if (texts.size() > 40) texts.remove(0); }
    public void shake(float amount) { camera.addShake(amount); }
    public void whiteFlash(float amount) { whiteFlash = Math.max(whiteFlash, amount); }
    public int nextShotId() { return ++shotCounter; }

    public void explode(float x, float y, float radius, float damage, Faction faction) {
        for (int i = 0; i < 26; i++) particles.spawn(ParticleType.FIRE, x, y, Util.rf(-1, 1) * radius * 2.2f, Util.rf(-1, 1) * radius * 2.2f, Util.rf(0.25f, 0.6f), Util.rf(6, 12), new Color(255, 170, 60));
        for (int i = 0; i < 16; i++) particles.spawn(ParticleType.SMOKE, x + Util.rf(-20, 20), y + Util.rf(-20, 20), Util.rf(-60, 60), Util.rf(-60, 60), Util.rf(0.8f, 1.6f), Util.rf(10, 18), new Color(60, 56, 52, 150));
        for (int i = 0; i < 18; i++) particles.spawn(ParticleType.SPARK, x, y, Util.rf(-1, 1) * 420, Util.rf(-1, 1) * 420, Util.rf(0.2f, 0.5f), 2, new Color(255, 230, 140));
        particles.spawn(ParticleType.RING, x, y, 0, 0, 0.35f, radius, new Color(255, 220, 160));
        addFlash(x, y, radius * 3, 0.45f, new Color(255, 160, 60));
        shake(12);
        game.getAudio().play("explode");
        Graphics2D d = map.getDecals();
        d.setPaint(new RadialGradientPaint(x, y, radius * 0.5f, new float[]{0, 1}, new Color[]{new Color(10, 8, 6, 110), new Color(10, 8, 6, 0)}));
        Util.oval(d, x, y, radius * 0.5, radius * 0.45);
        int killsBefore = stats.getKills();
        if (faction != Faction.BOSS) for (int i = 0; i < zombies.size(); i++) {
            Zombie z = zombies.get(i);
            if (z.isDead()) continue;
            float dist = Util.dist(x, y, z.getX(), z.getY());
            if (dist < radius + z.getRadius()) {
                float l = Math.max(1, dist);
                z.applyImpulse((z.getX() - x) / l * 500, (z.getY() - y) / l * 500);
                z.takeDamage(damage * (1 - 0.5f * dist / radius), DamageType.EXPLOSION, this);
            }
        }
        if (faction == Faction.PLAYER) {
            if (boss != null && !boss.isDead() && Util.dist(x, y, boss.getX(), boss.getY()) < radius + boss.getRadius())
                boss.takeDamage(damage * 0.8f, DamageType.EXPLOSION, this);
            for (int i = 0; i < structures.size(); i++) {
                Creature c = structures.get(i);
                if (!c.isDead() && Util.dist(x, y, c.getX(), c.getY()) < radius + c.getRadius()) c.takeDamage(damage, DamageType.EXPLOSION, this);
            }
            for (int i = 0; i < items.size(); i++) {
                ObjectiveItem it = items.get(i);
                if (it instanceof Damageable && !((Damageable) it).isDead() && Util.dist(x, y, it.getX(), it.getY()) < radius + it.getRadius())
                    ((Damageable) it).takeDamage(damage, DamageType.EXPLOSION, this);
            }
            if (stats.getKills() - killsBefore >= 5) award(Medal.DEMOLITION);
        } else if (!player.isDead()) {
            float dist = Util.dist(x, y, player.getX(), player.getY());
            if (dist < radius) player.hurt(damage * (1 - 0.5f * dist / radius), x, y, DamageType.EXPLOSION, this);
        }
    }

    /** Visual-only explosion (used while a boss is dying). */
    public void explosionEffect(float x, float y) {
        for (int i = 0; i < 14; i++) particles.spawn(ParticleType.FIRE, x, y, Util.rf(-200, 200), Util.rf(-200, 200), Util.rf(0.2f, 0.5f), Util.rf(5, 10), new Color(255, 170, 60));
        for (int i = 0; i < 6; i++) particles.spawn(ParticleType.SMOKE, x, y, Util.rf(-50, 50), Util.rf(-50, 50), 1f, 12, new Color(60, 50, 50, 140));
        for (int i = 0; i < 8; i++) particles.spawn(ParticleType.BLOOD, x, y, Util.rf(-200, 200), Util.rf(-200, 200), 0.6f, 3, new Color(130, 10, 30));
        addFlash(x, y, 200, 0.3f, new Color(255, 150, 60));
        shake(5);
        game.getAudio().play("explode", 0.6f, 1.2f);
    }

    // ================================================================== events
    public void onZombieKilled(Zombie z, DamageType type) {
        stats.addKill();
        profile.recordKill(type == DamageType.FIRE);
        int c = combo.register();
        stats.updateBestCombo(c);
        if (c >= 15) award(Medal.UNSTOPPABLE);
        if (profile.getKills() >= 1) award(Medal.FIRST_BLOOD);
        if (profile.getKills() >= 100) award(Medal.CENTURION);
        if (profile.getKills() >= 300) award(Medal.BODY_COUNT);
        if (profile.getFlameKills() >= 40) award(Medal.PYROMANIAC);
        effects.add(new Corpse(game.getAssets().zombie(z.getSprite(), z.getVariant(), z.getAnimFrame(), false), z.getX(), z.getY(), z.getAngle()));
        Util.splat(map.getDecals(), z.getX(), z.getY(), z.getRadius() * 1.1f, new Random(), new Color(110, 8, 8));
        game.getAudio().play("zdie", 0.5f, z.getRadius() > 18 ? 0.6f : Util.rf(0.9f, 1.3f));
        if (missionActive) mission.onZombieKilled(this);
    }

    public void onBossDying(Boss b) {
        slowMo = 1.3f; whiteFlash = 0.7f; shake(20);
        game.getAudio().play("roar", 1f, 0.7f);
        projectiles.removeIf(p -> !p.isFriendly());
        grenades.removeIf(g -> g instanceof EnemyGrenade);
        for (int i = 0; i < structures.size(); i++) structures.get(i).takeDamage(1e9f, DamageType.PURGE, this);
        announcer.showBanner(b.getName() + " DEFEATED", new Color(255, 200, 90));
    }

    public void onBossDefeated(Boss b) {
        for (int k = 0; k < 3; k++) explosionEffect(b.getX() + Util.rf(-30, 30), b.getY() + Util.rf(-30, 30));
        for (int k = 0; k < 30; k++) pickups.add(new CashPickup(b.getX() + Util.rf(-30, 30), b.getY() + Util.rf(-30, 30), 10 + chapterIndex * 2));
        pickups.add(new HealthPickup(b.getX(), b.getY()));
        pickups.add(new AmmoPickup(b.getX(), b.getY()));
        pickups.add(new GrenadePickup(b.getX(), b.getY()));
        Util.splat(map.getDecals(), b.getX(), b.getY(), b.getRadius() * 1.6f, new Random(), new Color(100, 8, 20));
        profile.addCash(250);
        addText("BOSS BOUNTY +$250", b.getX(), b.getY() - 50, new Color(255, 220, 90), 18);
        if (bossClean) award(Medal.UNTOUCHABLE);
        lastBossX = b.getX(); lastBossY = b.getY();
        if (missionActive) mission.onBossDefeated(this, b);
    }

    public void purgeAllZombies() {
        for (int i = 0; i < zombies.size(); i++) zombies.get(i).takeDamage(1e9f, DamageType.PURGE, this);
    }

    /** Every landed player attack is reported here so passive skills (Bloodlust) can react. */
    public void onPlayerDamageDealt(float amount) { if (!player.isDead()) player.onDamageDealt(this, amount); }

    /** Places the player's decoy (an older decoy still standing detonates first). */
    public void placeDecoy(Decoy d) {
        if (decoy != null && !decoy.isDead()) decoy.takeDamage(1e9f, DamageType.PURGE, this);
        decoy = d;
    }
    public Decoy getDecoy() { return decoy; }

    public void onObjectiveItemCompleted(ObjectiveItem item) { if (missionActive) mission.onItemCompleted(this, item); }

    public void onPlayerDamaged(float dmg) {
        stats.addDamage(dmg);
        hurtFlash = Math.min(0.6f, hurtFlash + dmg / 40f);
        shake(4);
        if (boss != null) bossClean = false;
    }

    public void onPlayerDeath() { announcer.showBanner("YOU DIED", new Color(220, 40, 40)); }

    public void award(Medal m) { if (profile.awardMedal(m)) announcer.showMedal(m); }

    // ================================================================== queries
    public Interactable findInteractable(Player p) {
        Interactable best = null;
        float bd = Float.MAX_VALUE;
        for (ObjectiveItem it : items) {
            if (!(it instanceof Interactable) || !((Interactable) it).canInteract(p)) continue;
            float d = it.distanceTo(p);
            if (d < bd) { bd = d; best = (Interactable) it; }
        }
        return best;
    }

    public float[] findSpawnPoint(float minD, float maxD, boolean offscreen) {
        for (int tries = 0; tries < 80; tries++) {
            int tx = 1 + Util.RNG.nextInt(map.getWidth() - 2), ty = 1 + Util.RNG.nextInt(map.getHeight() - 2);
            if (map.isSolid(tx, ty) || !map.isReachableTile(tx, ty) || Tile.isHazard(map.getTile(tx, ty))) continue;
            float x = tx * 32 + 16, y = ty * 32 + 16, d = Util.dist(x, y, player.getX(), player.getY());
            if (d < minD || d > maxD) continue;
            if (offscreen && camera.isVisible(x, y, 50)) continue;
            return new float[]{x, y};
        }
        return null;
    }

    public float[] pickReachableTile(int minSteps, int maxSteps, List<float[]> avoid, float sep, boolean open) {
        for (int pass = 0; pass < 4; pass++) {
            for (int tries = 0; tries < 400; tries++) {
                int tx = 1 + Util.RNG.nextInt(map.getWidth() - 2), ty = 1 + Util.RNG.nextInt(map.getHeight() - 2);
                int d = map.stepsAt(tx, ty);
                if (d < minSteps || d > maxSteps || map.isSolid(tx, ty) || Tile.isHazard(map.getTile(tx, ty))) continue;
                if (open && !map.isOpenArea(tx, ty)) continue;
                float x = tx * 32 + 16, y = ty * 32 + 16;
                boolean ok = true;
                if (avoid != null) for (float[] a : avoid) if (Util.dist(a[0], a[1], x, y) < sep) { ok = false; break; }
                if (ok) return new float[]{x, y};
            }
            minSteps = minSteps * 2 / 3; maxSteps = maxSteps * 3 / 2 + 6; sep *= 0.7f; open = false;
        }
        return reachableNear(player.getX() + 64, player.getY());
    }

    public float[] reachableNear(float x, float y) {
        if (map.isReachableNow(x, y)) return new float[]{x, y};
        int cx = GameMap.toTile(x), cy = GameMap.toTile(y);
        for (int r = 1; r < 10; r++)
            for (int oy = -r; oy <= r; oy++) for (int ox = -r; ox <= r; ox++) {
                float px = (cx + ox) * 32 + 16, py = (cy + oy) * 32 + 16;
                if (map.isReachableNow(px, py)) return new float[]{px, py};
            }
        return new float[]{player.getX() + 40, player.getY()};
    }

    // ================================================================== getters
    public Game getGame() { return game; }
    public GameMap getMap() { return map; }
    public Player getPlayer() { return player; }
    public PlayerProfile getProfile() { return profile; }
    public Chapter getChapter() { return chapter; }
    public int getChapterIndex() { return chapterIndex; }
    public Difficulty getDifficulty() { return difficulty; }
    public Audio audio() { return game.getAudio(); }
    public Input input() { return game.getInput(); }
    public Camera camera() { return camera; }
    public ParticleSystem particles() { return particles; }
    public ChapterStats stats() { return stats; }
    public ComboCounter combo() { return combo; }
    public RadioChatter radio() { return radio; }
    public Announcer announcer() { return announcer; }
    public float getTime() { return time; }
    public float mouseWorldX() { return game.getInput().getMouseX() + camera.getX(); }
    public float mouseWorldY() { return game.getInput().getMouseY() + camera.getY(); }
    public List<Zombie> getZombies() { return Collections.unmodifiableList(zombies); }
    public List<ObjectiveItem> getItems() { return Collections.unmodifiableList(items); }
    public List<Damageable> getDamageables() { return damageables; }
    public int zombieCount() { return zombies.size(); }
    public int getMaxZombies() { return chapter.getMaxZombies(); }
    public Boss getBoss() { return boss; }
    public Mission getMission() { return mission; }
    public int getMissionIndex() { return missionIndex; }
    public boolean isMissionActive() { return missionActive; }
    public boolean hasStarted() { return mission != null; }
    public float getLastBossX() { return lastBossX; }
    public float getLastBossY() { return lastBossY; }
    public boolean isPlayerDeadFor(float seconds) { return player.isDead() && player.getDeathTimer() > seconds; }
}
