package deadsignal.game;

/** Difficulty presets. Each constant carries its own multipliers. */
public enum Difficulty {
    EASY("EASY", 0.6f, 0.8f, 0.8f),
    NORMAL("NORMAL", 1f, 1f, 1f),
    HARD("HARD", 1.4f, 1.25f, 1.25f);

    private final String label;
    private final float damageTaken, enemyHealth, spawnRate;

    Difficulty(String label, float damageTaken, float enemyHealth, float spawnRate) {
        this.label = label; this.damageTaken = damageTaken; this.enemyHealth = enemyHealth; this.spawnRate = spawnRate;
    }

    public String getLabel() { return label; }
    public float getDamageTaken() { return damageTaken; }
    public float getEnemyHealth() { return enemyHealth; }
    public float getSpawnRate() { return spawnRate; }
    public Difficulty next() { return values()[(ordinal() + 1) % values().length]; }
    public Difficulty previous() { return values()[(ordinal() + values().length - 1) % values().length]; }
}
