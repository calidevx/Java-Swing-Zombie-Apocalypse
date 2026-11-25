package deadsignal.game;

/** Per-chapter performance numbers and the star rating derived from them. */
public final class ChapterStats {
    private int kills, shots, hits, lastHitShot = -1, bestCombo;
    private float damageTaken, time;

    public void addKill() { kills++; }
    public void addShot() { shots++; }
    /** Counts at most one hit per trigger pull (a shotgun blast is one shot). */
    public void registerHit(int shotId) { if (shotId != lastHitShot) { hits++; lastHitShot = shotId; } }
    public void addDamage(float d) { damageTaken += d; }
    public void tick(float dt) { time += dt; }
    public void updateBestCombo(int c) { bestCombo = Math.max(bestCombo, c); }

    public float accuracy() { return shots > 0 ? hits / (float) shots : 0; }
    public int stars() {
        int s = 3;
        if (damageTaken > 120) s--;
        if (damageTaken > 320) s--;
        if (accuracy() > 0.55f && s < 3) s++;
        return s;
    }
    public int bonus() { return 100 + stars() * 80 + kills * 2; }

    public int getKills() { return kills; }
    public int getShots() { return shots; }
    public float getDamageTaken() { return damageTaken; }
    public float getTime() { return time; }
    public int getBestCombo() { return bestCombo; }
}
