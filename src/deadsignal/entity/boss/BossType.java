package deadsignal.entity.boss;

/** FACTORY ENUM for bosses - each constant builds its own Boss subclass. */
public enum BossType {
    CENSER { @Override public Boss create(float x, float y, float hp) { return new Censer(x, y, hp); } },
    TANK { @Override public Boss create(float x, float y, float hp) { return new Tank(x, y, hp); } },
    BROODMOTHER { @Override public Boss create(float x, float y, float hp) { return new Broodmother(x, y, hp); } },
    SUBJECT13 { @Override public Boss create(float x, float y, float hp) { return new Subject13(x, y, hp); } },
    GRAVES { @Override public Boss create(float x, float y, float hp) { return new Graves(x, y, hp); } },
    SHEPHERD { @Override public Boss create(float x, float y, float hp) { return new Shepherd(x, y, hp); } },
    MAESTRO { @Override public Boss create(float x, float y, float hp) { return new Maestro(x, y, hp); } };

    public abstract Boss create(float x, float y, float hpMultiplier);
}
