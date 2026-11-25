package deadsignal.gfx;

import java.awt.Color;

/**
 * ABSTRACTION: anything that lights up the dark. The lighting system never asks
 * "what kind of object are you?" - it only asks these questions (POLYMORPHISM).
 */
public interface LightEmitter {
    float getLightX();
    float getLightY();
    float getLightRadius();
    /** 0..1 strength of the light at the given time (allows flicker). */
    float getLightIntensity(float time);

    /** Optional coloured glow painted over the darkness. */
    default Color getGlowColor() { return null; }
    default int getGlowAlpha() { return 0; }
    default float getGlowRadius() { return getLightRadius() * 0.7f; }
}
