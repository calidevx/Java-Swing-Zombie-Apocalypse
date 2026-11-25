package deadsignal.entity.zombie;

/**
 * FACTORY ENUM: each constant overrides {@link #create} to build its own subclass.
 * Callers just say {@code ZombieType.RUNNER.create(...)} - no if/else chains needed (polymorphism).
 */
public enum ZombieType {
    WALKER { @Override public Zombie create(float x, float y, float hp, float sp) { return new Walker(x, y, hp, sp); } },
    RUNNER { @Override public Zombie create(float x, float y, float hp, float sp) { return new Runner(x, y, hp, sp); } },
    BRUTE { @Override public Zombie create(float x, float y, float hp, float sp) { return new Brute(x, y, hp, sp); } },
    SPITTER { @Override public Zombie create(float x, float y, float hp, float sp) { return new Spitter(x, y, hp, sp); } },
    EXPLODER { @Override public Zombie create(float x, float y, float hp, float sp) { return new Exploder(x, y, hp, sp); } },
    CRAWLER { @Override public Zombie create(float x, float y, float hp, float sp) { return new Crawler(x, y, hp, sp); } },
    SOLDIER { @Override public Zombie create(float x, float y, float hp, float sp) { return new Soldier(x, y, hp, sp); } },
    SPIDERLING { @Override public Zombie create(float x, float y, float hp, float sp) { return new Spiderling(x, y, hp, sp); } };

    public abstract Zombie create(float x, float y, float hpMultiplier, float speedMultiplier);
}
