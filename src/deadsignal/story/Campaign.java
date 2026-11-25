package deadsignal.story;

import deadsignal.entity.boss.BossType;
import deadsignal.entity.item.*;
import deadsignal.entity.zombie.ZombieType;
import deadsignal.mission.*;
import deadsignal.weapon.AssaultRifle;
import deadsignal.weapon.Flamethrower;
import deadsignal.weapon.Shotgun;
import deadsignal.weapon.SubmachineGun;
import deadsignal.weapon.TeslaRifle;
import deadsignal.world.Theme;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static deadsignal.story.Speaker.*;

/** The complete storyline: prologue, six chapters and the epilogue. */
public final class Campaign {
    private final List<Chapter> chapters = new ArrayList<>();
    private final List<DialogueLine> prologue, epilogue;

    private static DialogueLine L(Speaker s, String t) { return new DialogueLine(s, t); }

    public Campaign() {
        prologue = Arrays.asList(
                L(NARRATOR, "PORT MERIDIAN.  Population: 2.1 million."),
                L(NARRATOR, "Seventy-two hours ago, something escaped BioGen Laboratories. The news called it the Lazarus Strain."),
                L(NARRATOR, "The dead stopped staying dead. The city fell in a single night."),
                L(NARRATOR, "Kai Navarro, paramedic, was on shift at St. Agnes Hospital when the first patients came in... biting."),
                L(NARRATOR, "Three days later, he wakes up. The city is silent."));
        epilogue = Arrays.asList(
                L(RADIO, "This is Dr. Lena Ortiz. If anyone can hear this... the cure is real. Port Meridian is not lost."),
                L(NARRATOR, "Six months later, the first vaccinated survivors walked out of Port Meridian."),
                L(NARRATOR, "The dead still wander the streets... but the signal never stopped."),
                L(NARRATOR, "THE END."));
        buildChapterOne(); buildChapterTwo(); buildChapterThree(); buildBonusStage(); buildChapterFour(); buildChapterFive(); buildChapterSix();
        int number = 1;
        for (Chapter c : chapters) c.setLabel(c.isBonus() ? "BONUS STAGE" : "CHAPTER " + number++);
    }

    private void buildChapterOne() {
        chapters.add(new Chapter("CODE BLACK", "St. Agnes Hospital", Theme.HOSPITAL, 1101)
                .spawning(1.5f, 22, 10, new SpawnTable().add(ZombieType.WALKER, 80).add(ZombieType.RUNNER, 20))
                .intro(L(KAI, "...Ugh. How long was I out?"),
                        L(KAI, "The ward's empty. The power's out. And there's blood... everywhere."),
                        L(RADIO, "*kssshh*  ...anyone receiving... this is Dr. Lena Ortiz... the Lazarus strain can be... *kssshh*"),
                        L(KAI, "A voice. A real voice! The security office has an emergency radio. I need to answer her."))
                .mission(new ItemMission("Find the security radio and answer the signal", 1, Radio::new, 14, 30)
                        .radio(L(TIP, "WASD to move, mouse to aim, LEFT CLICK to shoot. Follow the yellow arrow."),
                                L(TIP, "Hold E next to the radio to use it."))
                        .completion(L(LENA, "Oh thank God - a survivor! Who is this?"),
                                L(KAI, "Kai Navarro. Paramedic at St. Agnes."),
                                L(LENA, "Listen, Kai. They hunt by sound, and you just made a lot of it. Clear the courtyard!")))
                .mission(new KillMission("Clear the hospital courtyard", 25)
                        .radio(L(TIP, "R reloads. SPACE does a dodge roll - you can't be hurt mid-roll."))
                        .onProgress(12, L(LENA, "You're doing great. Keep moving - never let them surround you.")))
                .mission(new BossMission("Kill The Censer", BossType.CENSER)
                        .briefing(L(KAI, "Green smoke is pouring out of the chapel... and something is walking inside it."),
                                L(LENA, "That was Father Aldous, the hospital chaplain. The strain turned his censer smoke into poison."),
                                L(LENA, "Stay out of that cloud, Kai. And when he charges - dodge sideways!"))
                        .radio(L(TIP, "MIASMA burns and slows you up close. Fight from range while the cloud is up."),
                                L(TIP, "If he charges into a wall he is stunned and takes extra damage. Punish him!")))
                .mission(new ReachMission("Get to the ambulance", "AMBULANCE")
                        .radio(L(LENA, "There's an ambulance in the lot. Keys should still be inside. Go!")))
                .outro(L(KAI, "Made it. Keys are in the ignition."),
                        L(LENA, "Check the lockbox under the seat. EMS kept a shotgun there for emergencies."),
                        L(KAI, "If this isn't an emergency, I don't know what is."),
                        L(LENA, "Drive downtown, to Union Station. I'm hiding below the platforms - I'll explain everything there."))
                .reward(new WeaponReward(Shotgun::new, "PUMP SHOTGUN  -  found in the ambulance lockbox")));
    }

    private void buildChapterTwo() {
        chapters.add(new Chapter("DEAD STREETS", "Downtown Port Meridian", Theme.CITY, 2202)
                .spawning(1.15f, 30, 14, new SpawnTable().add(ZombieType.WALKER, 55).add(ZombieType.RUNNER, 25).add(ZombieType.BRUTE, 8).add(ZombieType.EXPLODER, 12))
                .intro(L(NARRATOR, "Six blocks from downtown, debris tears through the ambulance's fuel tank."),
                        L(KAI, "Out of gas. Of course."),
                        L(LENA, "The station's emergency gate runs on a generator. You'll need fuel - the wrecks on Fifth Avenue still have some."),
                        L(GRAVES, "Attention, civilians. This is Colonel Graves, Meridian Defense Command. Evacuation is underway at Fort Harlow. Remain indoors."),
                        L(LENA, "Don't listen to him, Kai. Fort Harlow stopped answering my calls two days ago."))
                .mission(new ItemMission("Collect fuel canisters", 4, FuelCanister::new, 12, 48)
                        .radio(L(TIP, "Bloated orange infected EXPLODE. Kill them at range - or near their friends."))
                        .onProgress(2, L(KAI, "Two down. These streets used to be packed with people.")))
                .mission(new SurviveMission("The generator is loud - survive the horde!", 45)
                        .briefing(L(LENA, "The generator's too loud! Every infected in the district heard it. Hold them off while it warms up!"))
                        .onProgress(25, L(LENA, "Generator at sixty percent! Hang on!")))
                .mission(new BossMission("Kill The Tank", BossType.TANK)
                        .briefing(L(LENA, "Seismic readings are spiking... something heavy is coming your way."),
                                L(KAI, "That's not a person anymore. That's a truck with teeth."),
                                L(LENA, "A Tank! It hurls debris and slams the ground. Watch the red warning circles!"))
                        .radio(L(TIP, "THUMPER PACK: stone imps stomp every second - they stun and slow you. Get out of the circle!")))
                .mission(new ReachMission("Enter Union Station", "STATION")
                        .radio(L(LENA, "The gate's open! Get down here before more of them come.")))
                .outro(L(KAI, "Found a fallen officer by the entrance. He won't be needing this anymore."),
                        L(LENA, "Platform nine. Watch your step... the tunnels are worse."))
                .reward(new WeaponReward(SubmachineGun::new, "MP5 SMG  -  recovered from a fallen officer")));
    }

    private void buildChapterThree() {
        chapters.add(new Chapter("UNDERTOW", "Union Station Tunnels", Theme.SEWER, 3303)
                .spawning(1.05f, 32, 14, new SpawnTable().add(ZombieType.WALKER, 40).add(ZombieType.RUNNER, 10).add(ZombieType.SPITTER, 15).add(ZombieType.CRAWLER, 35))
                .intro(L(KAI, "Can't see a thing down here. Flashlight on."),
                        L(LENA, "Survivors are hiding in the maintenance tunnels. Please, Kai - bring them to the platform."),
                        L(LENA, "And the backup generators are dead. Without power, I can't open the safe-room door."))
                .mission(new ItemMission("Rescue the trapped survivors", 4, Survivor::new, 12, 48)
                        .radio(L(TIP, "Hold E next to a survivor to rescue them. Spitters lob acid - keep moving."))
                        .onProgress(1, L(SURVIVOR, "You're... real? Thank you! Thank you!"))
                        .onProgress(3, L(SURVIVOR, "There's something in the deep tunnels. Something with too many legs...")))
                .mission(new ItemMission("Restart the backup generators", 3, Generator::new, 12, 48)
                        .onProgress(2, L(LENA, "Power's flickering back... one more!")))
                .mission(new BossMission("Kill The Broodmother", BossType.BROODMOTHER)
                        .briefing(L(LENA, "Kai... the survivors were right. That sound... it's a nest."),
                                L(KAI, "Oh, that is NOT okay."),
                                L(LENA, "The Broodmother. Kill her and the crawlers stop coming. Stay out of the acid pools!"))
                        .radio(L(TIP, "BLOOD RITE: when a red rune circle appears under you, MOVE. It erupts and jams your guns.")))
                .mission(new ReachMission("Reach Lena's safe room", "SAFE ROOM"))
                .outro(L(LENA, "Kai. You're real. I was starting to think I'd been talking to a ghost."),
                        L(KAI, "Close enough. So what's the plan, Doctor?"),
                        L(LENA, "A cure. I was on the Lazarus team at BioGen. Dr. Voss's research holds the antigen sequence - on three drives in the lab."),
                        L(KAI, "So we walk into the place where all of this started."),
                        L(LENA, "Take this rifle. It belonged to my security escort. He... didn't make it."))
                .reward(new WeaponReward(AssaultRifle::new, "M4 RIFLE  -  pierces through enemies")));
    }

    private void buildBonusStage() {
        chapters.add(new Chapter("DEAD MAN'S GROOVE", "The Majestic Theater", Theme.THEATER, 3707).bonus()
                .spawning(1.0f, 30, 14, new SpawnTable().add(ZombieType.WALKER, 50).add(ZombieType.RUNNER, 30).add(ZombieType.BRUTE, 8).add(ZombieType.EXPLODER, 12))
                .intro(L(NARRATOR, "On the road to BioGen, a sound drifts out of the dead city... music."),
                        L(KAI, "Is that... a brass band? Coming from the old Majestic Theater?"),
                        L(LENA, "Every infected in the district is shuffling toward it. Something in there is calling them."),
                        L(LENA, "The theater's prop vault is on my map. If anything valuable survived the outbreak, it's in there."),
                        L(KAI, "One quick detour. What could possibly go wrong?"))
                .mission(new KillMission("Clear the theater lobby", 20)
                        .radio(L(TIP, "BONUS STAGE: a side trip - but the reward at the end is legendary.")))
                .mission(new BossMission("Defeat The Maestro", BossType.MAESTRO)
                        .briefing(L(KAI, "The curtain's going up... and there's a conductor on stage. A dead one."),
                                L(LENA, "He's leading them with that baton! When they start dancing, shoot. When the music stops - RUN."))
                        .radio(L(TIP, "DEAD MAN'S GROOVE: zombies freeze and dance - free shots! Then CRESCENDO: they all charge at double speed."),
                                L(TIP, "The Maestro takes half damage while he conducts the dance. Clear the dance floor first.")))
                .mission(new ReachMission("Raid the prop vault", "PROP VAULT")
                        .radio(L(LENA, "The prop vault is behind the stage. Grab whatever's in there!")))
                .outro(L(KAI, "Lena... you need to see this. Some kind of prototype rifle, wrapped in copper coils."),
                        L(LENA, "A Tesla coil rig from the stage show... someone turned it into a real weapon."),
                        L(LENA, "Its lightning jumps between targets. It might even hurt things bullets can't touch."),
                        L(KAI, "Best encore ever."))
                .reward(new CompositeReward("LEGENDARY: TESLA RIFLE (chain lightning) + $500",
                        new WeaponReward(TeslaRifle::new, "TESLA RIFLE"), new CashReward(500))));
    }

    private void buildChapterFour() {
        chapters.add(new Chapter("LAZARUS", "BioGen Laboratories", Theme.LAB, 4404)
                .spawning(0.95f, 34, 16, new SpawnTable().add(ZombieType.WALKER, 30).add(ZombieType.RUNNER, 30).add(ZombieType.BRUTE, 10).add(ZombieType.SPITTER, 15).add(ZombieType.EXPLODER, 15))
                .intro(L(NARRATOR, "BioGen Laboratories. Where the dead first woke up."),
                        L(LENA, "Three research drives. Voss kept them in separate wings."),
                        L(LENA, "And Kai - stay out of the green spills. That's concentrated strain."))
                .mission(new ItemMission("Download the research drives", 3, ResearchDrive::new, 12, 48)
                        .onProgress(1, L(LENA, "Decrypting... these are Voss's personal logs."))
                        .onProgress(2, L(LENA, "Oh God. Voss injected HIMSELF with the strain. He called himself 'the Shepherd'. He thought he could LEAD them."))
                        .onProgress(3, L(LENA, "...And Graves knew. The military funded all of it. They're not evacuating anyone, Kai. They're covering it up.")))
                .mission(new KillMission("Fight through the containment wing", 35))
                .mission(new BossMission("Kill Subject 13", BossType.SUBJECT13)
                        .briefing(L(SYSTEM, "CONTAINMENT BREACH - WING C - SUBJECT 13 RELEASED"),
                                L(LENA, "Subject 13... their 'perfect soldier'. It's faster than anything you've seen. Dodge, Kai. Keep dodging!"))
                        .radio(L(TIP, "FAULT LINE: it rips the floor open in a line - the rocks stun you and block your path and bullets. Roll aside!")))
                .mission(new ReachMission("Escape to the loading dock", "LOADING DOCK"))
                .outro(L(KAI, "We've got the data. What now?"),
                        L(LENA, "To finish the cure I need a sample of the ORIGINAL strain. That means Voss himself."),
                        L(GRAVES, "To all units: Operation Clean Slate begins at dawn. Incendiary payloads are authorized over Port Meridian."),
                        L(KAI, "He's going to burn the whole city. With everyone still in it."),
                        L(LENA, "The bombers take coordinates from uplink transmitters at Fort Harlow. Take this - the incinerator crew won't need it."))
                .reward(new WeaponReward(Flamethrower::new, "FLAMETHROWER  -  sets the infected ablaze")));
    }

    private void buildChapterFive() {
        chapters.add(new Chapter("SCORCHED EARTH", "Fort Harlow Military Base", Theme.MILITARY, 5505)
                .spawning(0.85f, 38, 16, new SpawnTable().add(ZombieType.WALKER, 10).add(ZombieType.RUNNER, 27).add(ZombieType.BRUTE, 7)
                        .add(ZombieType.SPITTER, 10).add(ZombieType.EXPLODER, 8).add(ZombieType.SOLDIER, 48))
                .intro(L(NARRATOR, "Fort Harlow. The last line of defense."),
                        L(KAI, "No soldiers. Just... them. Still in uniform."),
                        L(LENA, "Three uplink transmitters. Sabotage them and the bombers stay grounded."))
                .mission(new ItemMission("Sabotage the uplink transmitters", 3, Transmitter::new, 12, 48)
                        .onProgress(2, L(GRAVES, "Someone is in my base. Whoever you are - you are making a terrible mistake.")))
                .mission(new SurviveMission("Hold out while Lena jams the signal", 60)
                        .briefing(L(LENA, "The last transmitter is rebooting itself! I need sixty seconds to jam it - keep them off you!"))
                        .onProgress(30, L(LENA, "Jamming at fifty percent... keep going!")))
                .mission(new BossMission("Defeat Colonel Graves", BossType.GRAVES)
                        .briefing(L(GRAVES, "You should have stayed indoors, paramedic."),
                                L(KAI, "Colonel... you've been bitten."),
                                L(GRAVES, "I've been ENLIGHTENED. Voss showed me. There is no cure. There is only the Flock... and the Shepherd!"))
                        .radio(L(TIP, "TOXIN SENTRIES poison and slow you - but they can be destroyed. Shoot them down!")))
                .mission(new ReachMission("Reach the helipad", "HELIPAD"))
                .outro(L(LENA, "The bombers are grounded. The city has time - not much, but some."),
                        L(KAI, "Graves said the Shepherd is at BioGen Tower."),
                        L(LENA, "Then that's where this ends."))
                .reward(new SupplyReward(3, 2, "MILITARY CACHE  -  +3 grenades, +2 medkits")));
    }

    private void buildChapterSix() {
        chapters.add(new Chapter("DEAD SIGNAL", "BioGen Tower, Ground Zero", Theme.INFERNO, 6606)
                .spawning(0.8f, 40, 18, new SpawnTable().add(ZombieType.WALKER, 25).add(ZombieType.RUNNER, 25).add(ZombieType.BRUTE, 12)
                        .add(ZombieType.SPITTER, 12).add(ZombieType.EXPLODER, 12).add(ZombieType.CRAWLER, 14))
                .intro(L(NARRATOR, "Dawn. BioGen Tower burns like a torch above the dead city."),
                        L(VOSS, "Kai Navarro. The paramedic. You've spent your life saving people... So have I. I saved them from death itself."),
                        L(KAI, "You turned them into monsters."),
                        L(VOSS, "I made them a flock. And every flock needs its Shepherd."),
                        L(LENA, "Kai, those pulsing growths are hive nodes. They're feeding him. Destroy them!"))
                .mission(new HiveMission("Destroy the hive nodes", 4)
                        .onProgress(2, L(VOSS, "You're hurting my children, Kai."))
                        .onProgress(4, L(LENA, "That's the last one! He's exposed!")))
                .mission(new BossMission("Defeat The Shepherd", BossType.SHEPHERD)
                        .briefing(L(VOSS, "Enough. Let me show you what eternity looks like."))
                        .radio(L(TIP, "Keep your FLAMETHROWER and GRENADES ready. Some things bullets cannot touch.")))
                .mission(new SampleMission("Collect the Shepherd's blood sample")
                        .radio(L(LENA, "Kai... is he dead? Get the sample. Quickly!")))
                .outro(L(LENA, "I have it. The original strain. With Voss's sequence... I can make the cure."),
                        L(KAI, "Then let's tell the world."),
                        L(LENA, "Every radio from here to the coast. All frequencies."))
                .reward(new StoryReward("THE CURE  -  Port Meridian has a future")));
    }

    public int size() { return chapters.size(); }
    public Chapter get(int index) { return chapters.get(index); }
    public List<DialogueLine> getPrologue() { return Collections.unmodifiableList(prologue); }
    public List<DialogueLine> getEpilogue() { return Collections.unmodifiableList(epilogue); }
}
