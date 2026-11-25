# DEAD SIGNAL — Object-Oriented Java Swing Zombie Shooter

A 2D top-down zombie apocalypse shooter with a 6-chapter story (about 18–25 minutes), 6 bosses with MOBA-style signature abilities, weapon unlocks, an uncapped upgrade shop, buyable special skills, medals and fully animated, code-generated sprites. There are **167 classes, interfaces and enums across 20 packages**, and **no asset files are needed**: every tile, sprite and sound is generated in code.

## Bosses and their signature abilities

| Ch | Boss | Signature ability | How to beat it |
|---|---|---|---|
| 1 | **The Censer** | **Miasma**: a toggled toxic cloud that burns and slows you up close | Fight from range while the cloud is up; make him charge into walls |
| 2 | **The Tank** | **Thumper Pack**: stone imps land and stomp an area every second (damage, mini-stun, slow) | Leave the dashed circle immediately |
| 3 | **The Broodmother** | **Blood Rite**: a rune circle erupts after 2.6s, hurting you and **jamming your guns**. **Brood**: births one **spiderling per leg she still has** (6 at the start). Every 1/6 of her HP lost **severs a leg** | Damage her fast to cut the spiderling waves down; leave the red circle before it fills |
| 4 | **Subject 13** | **Fault Line**: a ground crack that stuns and raises a rock wall blocking movement *and* bullets | Dodge-roll sideways; use the wall as cover |
| 5 | **The Abomination** (Col. Graves) | Redesigned as an armoured mutant with bone blades and a minigun. **RAGE bar** fills as you hurt him (and over time). When full he **ENRAGES** for 9s: ×1.6 speed, ×1.5 damage, faster minigun, and **Crushing Leap**: he jumps onto a marked circle and **stuns you for 2s** | Dodge-roll out of the red landing circle (rolling also ignores the stun); kite him while enraged |
| 6 | **The Shepherd** | **Two health bars.** Empty the first and he **sinks into the void** while a horde of runners and spitters attacks; he returns when it thins. **Blink**: teleports around you, leaving afterimages. **Veil Walk**: bullets pass through him. At 1/3 of his second bar he roars **"THE FLOCK DIVIDES!"** and splits into **BONE** (slams), **BLIGHT** (spores) and **SHADE** (blinks behind you and lunges). All three must die | Fire/explosives/Tesla during Veil Walk; kill SHADE first, it is the most fragile |

### Bonus Stage: *Dead Man's Groove* (after Chapter 3)
A detour into the abandoned **Majestic Theater** (coloured stage lights, falling confetti, its own dance-music track).
- **The Maestro**: a zombie bandleader in a tattered tailcoat and top hat, lit by his own spotlight. He backslides away from you and conducts weaving volleys of music-note projectiles.
- **Dead Man's Groove**: every zombie freezes into a synchronised dance (free shots!) while the Maestro takes half damage. Then comes **Crescendo**: the music drops and they all charge at double speed. Below 50% HP he calls an **Encore** of runners.
- **Legendary reward**: the **Tesla Rifle** (weapon slot 6). Its lightning chains to 3 more enemies and deals ENERGY damage, which even the Shepherd's Veil Walk can't ignore. You also get **$500**, a loot shower and the **ENCORE** medal.

### Special skills (shop tab, unlocked by clearing the Bonus Stage)
| Skill | Key | Effect | Upgrades (5 levels) |
|---|---|---|---|
| **Bloodlust** | passive | While your kill combo is **x50 or higher**, your hits heal you (9% of damage at LV1) | +3% lifesteal per level |
| **Shockwave** | F | AoE burst: **stuns** every zombie around you (2.5s), staggers bosses, deals damage. **30s cooldown** | shorter cooldown, wider radius, longer stun |
| **Decoy Dash** | C | Drops a **500 HP decoy** that every zombie nearby attacks instead of you, and **dashes you out** of the crowd. It **detonates when killed**. **60s cooldown** | more decoy HP, bigger blast, shorter cooldown |

Stat upgrades in the shop **no longer stop at level 5**. Prices keep rising and the bonuses have diminishing returns, so money is always worth spending.

### Magnet power-up (rare drop)
About 1 in 80 zombie kills (5% from Brutes) drops a blue **MAGNET**. For 20 seconds it pulls every cash, ammo, health and grenade pickup within a huge radius straight to you.

---

## How to run

**Command line (Java 11+)**
```bash
javac -d out $(find src -name "*.java")      # Windows: run.bat does this for you
java -cp out deadsignal.Main
```
Or just double-click **run.bat** (Windows) / run **./run.sh** (macOS/Linux).

**Eclipse:** File → New → Java Project → untick "Use default location", point it at this folder (with `src` as the source folder) → run `deadsignal.Main`.
**IntelliJ:** Open the folder → mark `src` as *Sources Root* → run `deadsignal.Main`.

| Control | Action | Control | Action |
|---|---|---|---|
| WASD | Move | Mouse | Aim |
| Left click | Shoot | Right click / G | Grenade |
| R | Reload | SPACE | Dodge roll |
| 1–6 / wheel | Switch weapon | Q | Medkit |
| F | Shockwave skill | C | Decoy Dash skill |
| E (hold) | Interact | ESC / P | Pause |
| F11 | Maximise | F9 | (dev) skip objective |

---

## Package map

```
deadsignal                Main, GamePanel (JPanel + game loop), TestHarness
├─ core                   Const, Util, Input, Camera
├─ audio                  Audio «interface», SoundEngine, SilentAudio
├─ gfx                    Assets (sprite factory), Lighting, Weather, ParticleSystem,
│                         Particle, ParticleType, LightEmitter «interface», Flash, sprite enums
├─ world                  GameMap, MapGenerator, MapRenderer, Theme, Tile, MapLight, Tree
├─ entity                 Entity «abstract», Creature «abstract», Player, Grenade, EnemyGrenade,
│  │                      AcidPool, Corpse, Damageable/Burnable/Interactable «interfaces»
│  ├─ zombie              Zombie «abstract», Walker, Soldier, Runner, Brute, Spitter, Exploder, Crawler, Spiderling, ZombieType
│  ├─ boss                Boss «abstract», Censer, Tank, Broodmother, Subject13, Graves, Shepherd, Maestro, BossType, BossBar,
│  │                      ShepherdAspect «abstract», BoneAspect, BlightAspect, ShadeAspect
│  ├─ ability             GroundEffect «abstract», BloodRite, ThumperPack, FaultLine, ToxinSentry
│  ├─ projectile          Projectile «abstract», Bullet, FlameJet, AcidGlob, EnergyOrb, HomingSpore, RockFragment, Boulder
│  ├─ pickup              Pickup «abstract», CashPickup, AmmoPickup, HealthPickup, GrenadePickup, MagnetPickup
│  └─ item                ObjectiveItem «abstract», HoldInteractable «abstract», SupplyItem «abstract»,
│                         Radio, Generator, ResearchDrive, Transmitter, BloodSample, Survivor,
│                         FuelCanister, ExitZone, HiveNode
├─ weapon                 Weapon «abstract», Pistol, Shotgun, SubmachineGun, AssaultRifle, Flamethrower
├─ mission                Mission «abstract», KillMission, SurviveMission, ItemMission, SampleMission,
│                         HiveMission, BossMission, ReachMission, ItemFactory «interface»
├─ story                  Campaign, Chapter, DialogueLine, Speaker, SpawnTable,
│                         Reward «abstract», WeaponReward, SupplyReward, StoryReward
├─ progress               PlayerProfile, Stat, Medal, Shop, ShopItem «abstract», StatUpgrade, GrenadeBundle, MedkitBundle, SkillPurchase
├─ skill                  Skill «abstract», Bloodlust, Shockwave, DecoyDash, SkillType (factory enum)
├─ game                   Game, GameWorld, Difficulty, ChapterStats, ComboCounter, SpawnDirector
├─ ui                     Hud, Announcer, RadioChatter, Portraits, MenuList, TitleBackdrop
└─ screen                 Screen «abstract», TitleScreen, ControlsScreen, DialogueScreen, PlayScreen,
                          PauseScreen, GameOverScreen, ResultsScreen, ShopScreen, VictoryScreen
```

---

## The 4 pillars of OOP: where to find them

### 1. Encapsulation: *hide the data, expose safe methods*

All fields in the project are `private` (or `protected` only where a subclass truly needs them). State changes only through methods that enforce the rules.

| Class | What is hidden | How it is protected |
|---|---|---|
| `progress/PlayerProfile` | cash, upgrade levels, arsenal, grenades, medals | `spend(int)` refuses if you can't afford it; `addGrenades()` caps at 9; `awardMedal()` pays only once; `raiseLevel()` is package-private so **only the shop** can upgrade stats |
| `entity/Creature` | `health`, `maxHealth` | Health can only drop through `takeDamage()` and rise through `heal()`. Nobody can set it directly |
| `entity/Player` | slow, jam and stun timers | Bosses can only call `applySlow()`, `jamWeapons()` and `stun()`; dodge-rolling and god mode are respected inside those methods |
| `skill/Skill` | level, cooldown timer | `tryActivate()` (final) is the only way to fire a skill and refuses while cooling down; `raiseSkill()` is package-private, so only `SkillPurchase` can level skills, and only after `unlockSkills()` |
| `entity/boss/Graves` | rage meter, enrage timer | Rage rises only inside his own `onHurt()`/`think()`; the HUD reads it through `getHudBars()` |
| `weapon/Weapon` | magazine, reserve ammo, cooldown, reload timer | `trigger()` checks cooldown/ammo; `finishReload()` is `private` |
| `mission/Mission` | progress, completed flag | Only `advance()` / `setProgress()` (protected, `final`) can move progress, and they fire the radio lines and completion event |
| `world/GameMap` | raw tile arrays | Package-private, so only `MapGenerator` and `MapRenderer` touch them; everyone else uses `isSolid()`, `collides()`, `flowDirection()`… |
| `game/GameWorld` | every entity list | Lists are private; `getZombies()` returns an **unmodifiable** view; entities use `spawnZombie()`, `addPickup()`, `explode()` |
| `core/Input` | key/mouse state shared between two threads | Thread-safe queues; `poll()` snapshots once per tick |

### 2. Inheritance: *reuse code through "is-a" relationships*

```
Entity (abstract)
├── Creature (abstract)                      ← health + damage pipeline
│   ├── Player
│   ├── Zombie (abstract)                    ← AI loop, separation, loot
│   │   ├── Walker
│   │   │   └── Soldier                      ← 5 levels deep: Entity→Creature→Zombie→Walker→Soldier
│   │   ├── Runner, Brute, Spitter, Exploder, Crawler, Spiderling
│   ├── Boss (abstract)                      ← dying sequence, contact damage, chase/summon helpers
│   │   └── Censer, Tank, Broodmother, Subject13, Graves, Shepherd, Maestro
│   ├── ShepherdAspect (abstract)            ← the Shepherd's split bodies
│   │   └── BoneAspect, BlightAspect, ShadeAspect
│   ├── Decoy                                ← the Decoy Dash dummy: lures zombies, explodes on death
│   └── ToxinSentry                          ← a Creature that never moves (a turret you can shoot)
├── Projectile (abstract)                    ← movement + collision loop
│   ├── Bullet, FlameJet, AcidGlob
│   ├── EnergyOrb → HomingSpore, PoisonDart, MusicNote
│   ├── TeslaBolt                            ← chain lightning
│   └── RockFragment → Boulder
├── Pickup (abstract) → CashPickup, AmmoPickup, HealthPickup, GrenadePickup, MagnetPickup
├── ObjectiveItem (abstract)
│   ├── HoldInteractable (abstract) → SupplyItem (abstract) → Radio, Generator, ResearchDrive, Transmitter, BloodSample
│   │                               → Survivor
│   └── FuelCanister, ExitZone, HiveNode
├── GroundEffect (abstract)                ← wind-up → trigger → active → end
│   └── BloodRite, ThumperPack, FaultLine
├── Grenade → EnemyGrenade
└── AcidPool, Corpse

Weapon (abstract)   → Pistol, Shotgun, SubmachineGun, AssaultRifle, Flamethrower, TeslaRifle
Mission (abstract)  → KillMission, SurviveMission, BossMission, ReachMission, HiveMission, ItemMission → SampleMission
ShopItem (abstract) → StatUpgrade, GrenadeBundle, MedkitBundle, SkillPurchase
Skill (abstract)    → Bloodlust (passive), Shockwave, DecoyDash
Reward (abstract)   → WeaponReward, SupplyReward, StoryReward, CashReward, CompositeReward
Screen (abstract)   → TitleScreen, PlayScreen, PauseScreen, DialogueScreen, ShopScreen, ResultsScreen, ...
GamePanel extends javax.swing.JPanel          ← inheriting from the Java library
```
`super(...)` constructors pass stats up the chain (e.g. `Soldier` reuses the `Walker` body with tougher numbers), and `super.dropLoot()` / `super.onDeath()` extend the parent's behaviour instead of replacing it (see `Brute`, `Exploder`).

### 3. Abstraction: *define what something does, not how*

**Abstract classes** declare the contract and leave the details to subclasses:
- `Entity`: every object must implement `update(world, dt)` and `render(g, assets)`.
- `Creature`: subclasses must implement `onDeath()`.
- `Boss`: subclasses must implement `think()` (their AI).
- `Skill`: subclasses must implement `getCooldown()` and `activate()`; the base class runs the cooldown timer, the key handling and the HUD data.
- `ShepherdAspect`: subclasses must implement `attack()`, `getSpeed()` and `getPreferredRange()`.
- `Weapon`: subclasses must implement `fireProjectiles()`, `drawModel()` and `newInstance()`.
- `Mission`, `ShopItem`, `Reward`, `Pickup` and `Screen` work the same way.

**Interfaces** describe abilities that cut across the class tree:
- `Damageable`: implemented by `Zombie`, `Boss` (via `Creature`) **and** `HiveNode` (an item!). Bullets only know "it's Damageable".
- `LightEmitter`: implemented by `Flash`, `MapLight`, `ObjectiveItem`, `FlameJet`, `EnergyOrb`, `AcidGlob` and `Shepherd`. The lighting engine never asks what the object is.
- `Burnable`: the flamethrower ignites anything `Burnable` (zombies).
- `Interactable`: anything you can hold E on.
- `Audio`: the game talks to an interface. `SoundEngine.create()` hands back either a real `SoundEngine` or a `SilentAudio` when no sound card exists, and nothing else changes.
- `ItemFactory`: a functional interface, used as `Radio::new`, `Survivor::new`…

### 4. Polymorphism: *same call, different behaviour*

| Call site | One line of code | Behaves differently for… |
|---|---|---|
| `GameWorld.updateAll()` | `e.update(this, dt)` | every Entity subclass |
| `GameWorld.render()` | `e.render(g, a)` | every Entity subclass |
| `Boss.update()` | `think(w, dt, dx, dy, d)` | 6 completely different boss AIs |
| `GroundEffect.update()` | `onTrigger(w)` | Blood Rite jams guns, Thumper Pack stomps, Fault Line raises rocks |
| `GameWorld.update()` | `boss.getMusicTrack()` | the Maestro switches to dance music mid-fight; other bosses keep the boss theme |
| `Hud.drawBossBar()` | `boss.getHudBars()` | one bar for most bosses, HP + RAGE for Graves, two lives (then one bar per aspect) for the Shepherd |
| `Boss.render()` | `bodyFrame(...)` | the Broodmother picks the sprite with her remaining legs |
| `Creature.takeDamage()` | `onDeath(...)` | the Shepherd's first "death" sends him into the void instead of ending the fight |
| `Player.handleSkills()` | `skill.tryActivate(...)` | Shockwave stuns, Decoy Dash spawns a decoy and dashes; Bloodlust is passive and reacts in `onDamageDealt()` |
| `Bullet.onHitTarget()` | `target.isImmuneTo(BULLET)` | only the Shepherd during Veil Walk says yes, so the bullets pass through |
| `Player.handleWeapons()` | `wpn.trigger(...)` | pistol bullets vs. 8 shotgun pellets vs. flame jets |
| `Player.render()` / `Hud` | `wpn.drawModel(g)` | each gun draws itself |
| `Projectile.update()` | `onHitTarget(d, w)` | bullet stops, flame passes through and ignites |
| `Creature.takeDamage()` | `modifyIncomingDamage(...)` | armour (Player), fire resistance (Boss), stun weakness (Censer), +50% fire/explosions (veiled Shepherd) |
| `Zombie.update()` | `behave(...)`, `canMelee()` | Spitter retreats and spits; Exploder lights a fuse |
| `GameWorld` events | `mission.onZombieKilled(...)` | only `KillMission` reacts; others ignore it |
| `SpawnDirector` | `mission.spawnRateMultiplier()` | Survive = 3× faster spawns, Boss = slower |
| `ShopScreen` | `item.purchase(p)`, `item.drawIcon(...)` | stat upgrades vs. consumables |
| `ResultsScreen` | `reward.grantTo(p)` | new weapon vs. supplies vs. story ending |
| `Game` | `screen.update()` / `screen.render()` | 9 different screens (State pattern) |
| `ParticleSystem` | `p.getType().draw(...)` | **enum constant-specific methods**: each `ParticleType` draws itself |
| `GameWorld.spawnZombie()` | `type.create(...)` | `ZombieType` / `BossType` are **factory enums**, each constant builds its own subclass |

Both kinds of polymorphism appear:
- **Overriding** (runtime polymorphism) is used everywhere above.
- **Overloading** (compile-time) appears in `Entity.distanceTo(Entity)` vs `distanceTo(float, float)` and `RadioChatter.queue(DialogueLine)` vs `queue(List)`.

---

## Design patterns used (bonus points)
- **Template Method**: `Creature.takeDamage`, `GroundEffect.update`, `Weapon.trigger`, `Zombie.update`, `Boss.update`, `Projectile.update`, `Mission.start`, `ShopItem.purchase`, `Skill.tryActivate`, `ShepherdAspect.update`
- **State**: `Screen` subclasses swapped by `Game.setScreen()`
- **Factory**: `ZombieType`, `BossType`, `SkillType`, `SoundEngine.create()`, `GameMap.create()`
- **Null Object**: `SilentAudio`
- **Composite**: `CompositeReward` bundles several rewards (Tesla Rifle + cash) and is used exactly like a single `Reward`
- **Observer-style events**: entities report to `GameWorld.onZombieKilled / onBossDefeated / onObjectiveItemCompleted`, which forwards to the current `Mission`

---

## Testing
`java -cp out deadsignal.Main --test` runs a bot through the whole campaign headlessly and saves screenshots to `./shots`.
