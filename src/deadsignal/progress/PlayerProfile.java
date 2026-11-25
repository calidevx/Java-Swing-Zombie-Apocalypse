package deadsignal.progress;

import deadsignal.skill.SkillType;
import deadsignal.weapon.Pistol;
import deadsignal.weapon.Weapon;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * ENCAPSULATION showcase: everything the player carries between chapters - cash, upgrades,
 * arsenal, consumables, statistics and medals - is private. Other classes can only change it
 * through methods that enforce the rules (e.g. you cannot spend money you don't have,
 * grenades are capped at 9, upgrades at level 5).
 */
public final class PlayerProfile {
    public static final int WEAPON_SLOTS = 6, MAX_GRENADES = 9, MAX_MEDKITS = 5;

    private int cash;
    private final Map<Stat, Integer> levels = new EnumMap<>(Stat.class);
    private final Weapon[] arsenal = new Weapon[WEAPON_SLOTS];
    private int currentSlot;
    private int grenades = 2, medkits = 1;
    private int kills, deaths, flameKills, medkitsUsed;
    private float playTime;
    private final Set<Medal> medals = EnumSet.noneOf(Medal.class);
    private final Map<SkillType, Integer> skillLevels = new EnumMap<>(SkillType.class);
    private boolean skillsUnlocked;

    public PlayerProfile() {
        for (Stat s : Stat.values()) levels.put(s, 0);
        for (SkillType t : SkillType.values()) skillLevels.put(t, 0);
        arsenal[0] = new Pistol();
    }

    /** Deep copy - used as a checkpoint so a chapter can be retried from its start. */
    public PlayerProfile copy() {
        PlayerProfile p = new PlayerProfile();
        p.cash = cash; p.levels.putAll(levels);
        for (int i = 0; i < WEAPON_SLOTS; i++) p.arsenal[i] = arsenal[i] == null ? null : arsenal[i].copy();
        p.currentSlot = currentSlot; p.grenades = grenades; p.medkits = medkits;
        p.kills = kills; p.deaths = deaths; p.flameKills = flameKills; p.medkitsUsed = medkitsUsed; p.playTime = playTime;
        p.medals.addAll(medals);
        p.skillLevels.putAll(skillLevels); p.skillsUnlocked = skillsUnlocked;
        return p;
    }

    // ------------------------------------------------------------------ cash
    public int getCash() { return cash; }
    public void addCash(int amount) { if (amount > 0) cash += amount; }
    /** @return true if the purchase went through. */
    public boolean spend(int amount) {
        if (amount < 0 || amount > cash) return false;
        cash -= amount;
        return true;
    }

    // ------------------------------------------------------------------ upgrades
    public int getLevel(Stat s) { return levels.get(s); }
    /** Stat upgrades are uncapped; effects use diminishing returns so nothing breaks. */
    public boolean canUpgrade(Stat s) { return true; }
    void raiseLevel(Stat s) { if (canUpgrade(s)) levels.put(s, getLevel(s) + 1); }

    public float maxHealth() { return 100 + 20 * getLevel(Stat.VITALITY); }
    public float damageMultiplier() { return 1 + 0.12f * getLevel(Stat.FIREPOWER); }
    public float fireRateMultiplier() { return 1 + 0.8f * (1 - (float) Math.pow(0.88, getLevel(Stat.TRIGGER))); }
    public float reloadSpeedMultiplier() { return 1 + 0.5f * (1 - (float) Math.pow(0.9, getLevel(Stat.TRIGGER))); }
    public float moveSpeedMultiplier() { return 1 + 0.5f * (1 - (float) Math.pow(0.87, getLevel(Stat.AGILITY))); }
    public float damageTakenMultiplier() { return Math.max(0.25f, (float) Math.pow(0.92, getLevel(Stat.KEVLAR))); }
    public float ammoCapacityMultiplier() { return 1 + 0.25f * getLevel(Stat.AMMO_BELT); }

    // ------------------------------------------------------------------ special skills
    public boolean areSkillsUnlocked() { return skillsUnlocked; }
    public void unlockSkills() { skillsUnlocked = true; }
    public int getSkillLevel(SkillType t) { return skillLevels.get(t); }
    void raiseSkill(SkillType t) { if (skillsUnlocked && getSkillLevel(t) < SkillType.MAX_LEVEL) skillLevels.put(t, getSkillLevel(t) + 1); }

    // ------------------------------------------------------------------ arsenal
    public Weapon getCurrentWeapon() { return arsenal[currentSlot]; }
    public Weapon getWeapon(int slot) { return arsenal[slot]; }
    public int getCurrentSlot() { return currentSlot; }
    public boolean ownsWeapon(int slot) { return slot >= 0 && slot < WEAPON_SLOTS && arsenal[slot] != null; }
    public void giveWeapon(Weapon w) { arsenal[w.getSlot()] = w; w.refill(ammoCapacityMultiplier()); }
    public boolean selectWeapon(int slot) { if (!ownsWeapon(slot)) return false; currentSlot = slot; return true; }
    public int nextOwnedSlot(int dir) {
        int s = currentSlot;
        for (int i = 0; i < WEAPON_SLOTS; i++) { s = (s + (dir > 0 ? 1 : WEAPON_SLOTS - 1)) % WEAPON_SLOTS; if (arsenal[s] != null) return s; }
        return currentSlot;
    }
    public void refillAllAmmo() { for (Weapon w : arsenal) if (w != null) w.refill(ammoCapacityMultiplier()); }
    /** @return true if at least one weapon received ammo. */
    public boolean addAmmoToAll(float magazines) {
        boolean any = false;
        for (Weapon w : arsenal) if (w != null && w.addReserve(magazines, ammoCapacityMultiplier())) any = true;
        return any;
    }

    // ------------------------------------------------------------------ consumables
    public int getGrenades() { return grenades; }
    public int getMedkits() { return medkits; }
    public boolean useGrenade() { if (grenades <= 0) return false; grenades--; return true; }
    public boolean useMedkit() { if (medkits <= 0) return false; medkits--; medkitsUsed++; return true; }
    public void addGrenades(int n) { grenades = Math.min(MAX_GRENADES, grenades + n); }
    public void addMedkits(int n) { medkits = Math.min(MAX_MEDKITS, medkits + n); }

    // ------------------------------------------------------------------ statistics & medals
    public void recordKill(boolean byFire) { kills++; if (byFire) flameKills++; }
    public void recordDeath() { deaths++; }
    public void addPlayTime(float seconds) { playTime += seconds; }
    public int getKills() { return kills; }
    public int getDeaths() { return deaths; }
    public int getFlameKills() { return flameKills; }
    public int getMedkitsUsed() { return medkitsUsed; }
    public float getPlayTime() { return playTime; }
    /** @return true only the first time a medal is earned (and pays its bonus). */
    public boolean awardMedal(Medal m) {
        if (!medals.add(m)) return false;
        cash += Medal.CASH_BONUS;
        return true;
    }
    public Set<Medal> getMedals() { return Collections.unmodifiableSet(medals); }
}
