package deadsignal.audio;

import deadsignal.core.Util;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.*;

/** Software mixer that synthesises every effect and music loop procedurally at start-up. */
public final class SoundEngine implements Audio, Runnable {
    private static final float RATE = 22050f;
    private static final double TAU = Math.PI * 2;

    /** A single playing sound. Private nested class: nobody outside the mixer needs it. */
    private static final class Voice {
        private final float[] data; private final float vol, pitch; private final boolean loop; private double pos;
        Voice(float[] d, float v, float p, boolean l) { data = d; vol = v; pitch = p; loop = l; }
    }

    private final Map<String, float[]> bank = new HashMap<>();
    private final List<Voice> voices = new ArrayList<>();
    private final Map<String, Long> lastPlay = new HashMap<>();
    private SourceDataLine line;
    private Voice musicVoice;
    private String musicName = "";

    private SoundEngine() { }

    /** Factory: returns a working engine, or a {@link SilentAudio} if the device is unavailable. */
    public static Audio create() {
        try {
            SoundEngine e = new SoundEngine();
            e.synth();
            AudioFormat f = new AudioFormat(RATE, 16, 1, true, false);
            e.line = AudioSystem.getSourceDataLine(f);
            e.line.open(f, 4096);
            e.line.start();
            Thread t = new Thread(e, "audio-mixer");
            t.setDaemon(true);
            t.start();
            return e;
        } catch (Throwable ex) {
            System.out.println("[DeadSignal] audio unavailable - running silent (" + ex.getMessage() + ")");
            return new SilentAudio();
        }
    }

    @Override public void play(String n, float vol, float pitch) {
        float[] d = bank.get(n);
        if (d == null) return;
        long now = System.nanoTime();
        Long lp = lastPlay.get(n);
        if (lp != null && now - lp < 35_000_000L) return;
        lastPlay.put(n, now);
        synchronized (voices) { if (voices.size() < 30) voices.add(new Voice(d, vol, pitch * (0.94f + Util.RNG.nextFloat() * 0.12f), false)); }
    }

    @Override public void music(String n) {
        if (n.equals(musicName)) return;
        musicName = n;
        synchronized (voices) {
            if (musicVoice != null) voices.remove(musicVoice);
            float[] d = bank.get(n);
            musicVoice = d == null ? null : new Voice(d, 0.55f, 1f, true);
            if (musicVoice != null) voices.add(musicVoice);
        }
    }

    @Override public void run() {
        int n = 512;
        float[] mix = new float[n];
        byte[] buf = new byte[n * 2];
        while (true) {
            Arrays.fill(mix, 0);
            synchronized (voices) {
                for (int vi = voices.size() - 1; vi >= 0; vi--) {
                    Voice v = voices.get(vi);
                    boolean done = false;
                    for (int i = 0; i < n; i++) {
                        int ip = (int) v.pos;
                        if (ip >= v.data.length) {
                            if (v.loop) { v.pos -= v.data.length; ip = (int) v.pos; } else { done = true; break; }
                        }
                        mix[i] += v.data[ip] * v.vol;
                        v.pos += v.pitch;
                    }
                    if (done) voices.remove(vi);
                }
            }
            for (int i = 0; i < n; i++) {
                short sv = (short) (Math.tanh(mix[i] * 0.85f) * 30000);
                buf[2 * i] = (byte) sv; buf[2 * i + 1] = (byte) (sv >> 8);
            }
            line.write(buf, 0, buf.length);
        }
    }

    // ------------------------------------------------------------------ synthesis
    private static float[] buf(float sec) { return new float[(int) (sec * RATE)]; }

    private void synth() {
        Random r = new Random(5);
        bank.put("pistol", gun(0.22f, 32f, 150f, 0.7f, 0.35f, r));
        bank.put("shotgun", gun(0.5f, 11f, 75f, 1.0f, 0.25f, r));
        bank.put("smg", gun(0.12f, 48f, 190f, 0.45f, 0.4f, r));
        bank.put("rifle", gun(0.28f, 24f, 120f, 0.75f, 0.5f, r));
        bank.put("flame", noiseBurst(0.3f, 0.12f, r, 0.35f));
        bank.put("explode", boom(1.3f, r));
        bank.put("hit", hit(r));
        bank.put("zdie", growl(0.45f, 95, 0.5f, r));
        bank.put("groan", growl(0.9f, 70, 0.4f, r));
        bank.put("roar", growl(1.6f, 55, 0.9f, r));
        bank.put("hurt", sweep(0.25f, 340, 140, 0.5f, r, 0.3f));
        bank.put("pickup", tones(new float[]{880, 1320}, 0.07f, 0.35f));
        bank.put("cash", tones(new float[]{1568, 2093}, 0.05f, 0.25f));
        bank.put("reload", clicks(new float[]{0f, 0.18f, 0.24f}, r));
        bank.put("empty", clicks(new float[]{0f}, r));
        bank.put("objective", tones(new float[]{523, 659, 784, 1046}, 0.11f, 0.35f));
        bank.put("medal", tones(new float[]{784, 988, 1175, 1568}, 0.08f, 0.35f));
        bank.put("select", tones(new float[]{660}, 0.05f, 0.25f));
        bank.put("buy", tones(new float[]{700, 1050}, 0.06f, 0.3f));
        bank.put("throw", noiseBurst(0.2f, 0.05f, r, 0.25f));
        bank.put("dash", noiseBurst(0.25f, 0.06f, r, 0.3f));
        bank.put("splat", sweep(0.22f, 220, 60, 0.45f, r, 0.6f));
        bank.put("zap", sweep(0.18f, 900, 260, 0.25f, r, 0.1f));
        bank.put("radio", radio(r));
        bank.put("heal", tones(new float[]{440, 554, 659}, 0.08f, 0.3f));
        bank.put("drone", drone(r, false));
        bank.put("boss", drone(r, true));
        bank.put("groove", groove(r));
    }
    private float[] gun(float dur, float decay, float boomHz, float vol, float bright, Random r) {
        float[] d = buf(dur); float lp = 0;
        for (int i = 0; i < d.length; i++) {
            float t = i / RATE, n = r.nextFloat() * 2 - 1;
            lp += (n - lp) * bright;
            float env = (float) Math.exp(-t * decay);
            float boom = (float) Math.sin(TAU * boomHz * t * (1 - t * 1.2)) * (float) Math.exp(-t * decay * 0.5);
            d[i] = (lp * 0.9f * env + boom * 0.8f + (t < 0.003f ? n * 0.6f : 0)) * vol;
        }
        return d;
    }
    private float[] noiseBurst(float dur, float att, Random r, float vol) {
        float[] d = buf(dur); float lp = 0;
        for (int i = 0; i < d.length; i++) {
            float t = i / RATE, n = r.nextFloat() * 2 - 1;
            lp += (n - lp) * 0.18f;
            float env = t < att ? t / att : Math.max(0, 1 - (t - att) / Math.max(0.01f, dur - att));
            d[i] = lp * env * vol * 1.6f;
        }
        return d;
    }
    private float[] boom(float dur, Random r) {
        float[] d = buf(dur); float lp = 0, lp2 = 0;
        for (int i = 0; i < d.length; i++) {
            float t = i / RATE, n = r.nextFloat() * 2 - 1;
            lp += (n - lp) * 0.08f; lp2 += (lp - lp2) * 0.2f;
            float sub = (float) Math.sin(TAU * (48 - t * 20) * t) * (float) Math.exp(-t * 2.5);
            d[i] = (lp2 * 3.2f + sub * 0.9f) * (float) Math.exp(-t * 3.2) * 0.9f;
        }
        return d;
    }
    private float[] hit(Random r) {
        float[] d = buf(0.1f); float lp = 0;
        for (int i = 0; i < d.length; i++) {
            float t = i / RATE, n = r.nextFloat() * 2 - 1; lp += (n - lp) * 0.3f;
            d[i] = (lp * 0.5f + (float) Math.sin(TAU * (260 - t * 1800) * t) * 0.4f) * (float) Math.exp(-t * 40);
        }
        return d;
    }
    private float[] growl(float dur, float f0, float vol, Random r) {
        float[] d = buf(dur); double ph = 0; float lp = 0;
        for (int i = 0; i < d.length; i++) {
            float t = i / RATE;
            double f = f0 * (1 + 0.15 * Math.sin(TAU * 7 * t) + 0.1 * Math.sin(TAU * 2.3 * t)) * (1 - t / dur * 0.25);
            ph += f / RATE;
            double saw = 2 * (ph - Math.floor(ph)) - 1;
            float n = r.nextFloat() * 2 - 1; lp += (n - lp) * 0.1f;
            float env = (float) (Math.min(1, t / 0.08) * Math.max(0, 1 - t / dur));
            d[i] = (float) (saw * 0.45 + lp * 0.5) * env * vol;
        }
        float a = 0;
        for (int i = 0; i < d.length; i++) { a += (d[i] - a) * 0.25f; d[i] = a * 1.6f; }
        return d;
    }
    private float[] sweep(float dur, float f0, float f1, float vol, Random r, float noise) {
        float[] d = buf(dur); double ph = 0;
        for (int i = 0; i < d.length; i++) {
            float t = i / RATE, k = t / dur;
            ph += (f0 + (f1 - f0) * k) / RATE;
            d[i] = (float) ((Math.sin(TAU * ph) * (1 - noise) + (r.nextFloat() * 2 - 1) * noise) * (1 - k) * vol);
        }
        return d;
    }
    private float[] tones(float[] fs, float each, float vol) {
        float[] d = buf(each * fs.length + 0.15f);
        for (int k = 0; k < fs.length; k++) {
            int st = (int) (k * each * RATE);
            for (int i = 0; st + i < d.length; i++) {
                float t = i / RATE;
                d[st + i] += (float) (Math.sin(TAU * fs[k] * t) + 0.3 * Math.sin(TAU * fs[k] * 2 * t)) * (float) Math.exp(-t * 9) * vol;
            }
        }
        return d;
    }
    private float[] clicks(float[] at, Random r) {
        float[] d = buf(at[at.length - 1] + 0.06f);
        for (float a : at) {
            int st = (int) (a * RATE);
            for (int i = 0; i < 500 && st + i < d.length; i++) d[st + i] += (r.nextFloat() * 2 - 1) * (float) Math.exp(-i / 60.0) * 0.6f;
        }
        return d;
    }
    private float[] radio(Random r) {
        float[] d = buf(0.25f);
        for (int i = 0; i < d.length; i++) {
            float t = i / RATE;
            d[i] = (float) ((r.nextFloat() * 2 - 1) * 0.25 + Math.sin(TAU * 1200 * t) * 0.15) * (float) Math.max(0, 1 - t / 0.25);
        }
        return d;
    }
    /** Upbeat 4-bar loop for the Maestro's dance: kick, off-beat hats, walking bass. */
    private float[] groove(Random r) {
        float beat = 0.48f;
        float[] d = buf(beat * 16);
        double[] bass = {55, 55, 65.4, 73.4, 82.4, 73.4, 65.4, 61.7};
        for (int i = 0; i < d.length; i++) {
            double t = i / RATE, b = (t / beat) % 1.0;
            int beatIndex = (int) (t / beat);
            double kick = Math.sin(TAU * (90 - b * 50) * b * beat) * Math.exp(-b * 10) * 0.9;
            double hb = ((t + beat / 2) / beat) % 1.0;
            double hat = (r.nextFloat() * 2 - 1) * Math.exp(-hb * 30) * 0.18;
            double snare = beatIndex % 2 == 1 ? (r.nextFloat() * 2 - 1) * Math.exp(-b * 14) * 0.35 : 0;
            double f = bass[(int) (t / (beat / 2)) % bass.length];
            double bb = ((t / (beat / 2)) % 1.0);
            double bassTone = (Math.sin(TAU * f * t) + 0.4 * Math.sin(TAU * f * 2 * t)) * Math.exp(-bb * 4) * 0.35;
            double stab = beatIndex % 4 == 3 ? Math.sin(TAU * 440 * t) * Math.exp(-b * 6) * 0.08 : 0;
            d[i] = (float) ((kick + hat + snare + bassTone + stab) * 0.6);
        }
        return d;
    }

    private float[] drone(Random r, boolean boss) {
        float[] d = buf(boss ? 4f : 12f); float lp = 0;
        for (int i = 0; i < d.length; i++) {
            double t = i / RATE, v;
            if (!boss) {
                double l1 = 0.5 + 0.5 * Math.sin(TAU * t / 12), l2 = 0.5 + 0.5 * Math.sin(TAU * t / 6 + 1);
                v = Math.sin(TAU * 55 * t) * 0.28 + Math.sin(TAU * 82.5 * t) * 0.16 * l1 + Math.sin(TAU * 110 * t) * 0.08 * l2
                        + Math.sin(TAU * 116.5 * t) * 0.05 * (1 - l1);
                float n = r.nextFloat() * 2 - 1; lp += (n - lp) * 0.01f;
                v = (v + lp * 0.9 * (0.5 + 0.5 * Math.sin(TAU * t / 4))) * 0.32;
            } else {
                double beat = (t * 2.5) % 1.0;
                double kick = Math.sin(TAU * (60 - beat * 30) * beat * 0.4) * Math.exp(-beat * 9);
                double bass = Math.sin(TAU * 55 * t + Math.sin(TAU * 0.5 * t)) * 0.25 * (((int) (t * 2.5)) % 2 == 0 ? 1 : 0.7);
                double saw = 2 * ((t * 110) % 1) - 1;
                v = (kick * 0.9 + bass + saw * 0.04 * (0.5 + 0.5 * Math.sin(TAU * t / 4))) * 0.45;
            }
            d[i] = (float) v;
        }
        return d;
    }
}
