package deadsignal.audio;

/**
 * ABSTRACTION: the rest of the game only knows this interface, not how sound is produced.
 * Implementations: {@link SoundEngine} (real synthesised audio) and {@link SilentAudio} (no device).
 */
public interface Audio {
    void play(String sound, float volume, float pitch);
    void music(String track);

    default void play(String sound) { play(sound, 1f, 1f); }
}
