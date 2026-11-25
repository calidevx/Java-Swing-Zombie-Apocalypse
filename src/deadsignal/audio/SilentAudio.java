package deadsignal.audio;

/** Null-object implementation used when no audio device exists (or in headless tests). */
public final class SilentAudio implements Audio {
    @Override public void play(String sound, float volume, float pitch) { }
    @Override public void music(String track) { }
}
