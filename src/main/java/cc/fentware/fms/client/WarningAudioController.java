package cc.fentware.fms.client;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;

/**
 * A single prioritized FMS alert channel.
 *
 * An escalating emergency interrupts immediately; recovery/downgrade must
 * remain stable before the next announcement. No layered tones or queued voice
 * clips: only one sound can play at a time.
 */
public final class WarningAudioController {
    private static FlightWarnings.Alert active = FlightWarnings.Alert.none();
    private static String observed = "";
    private static long observedSince;
    private static long lastAnnounced;
    private static final long REPEAT_MS = 12000;
    private static final long EQUAL_CHANGE_MS = 800;
    private static final long DOWNGRADE_MS = 1800;
    private static final long CLEAR_MS = 700;

    private WarningAudioController() {}

    public static void tick(Minecraft client) {
        if (client.player == null || !client.player.isFallFlying()
                || !HudSettings.audioEnabled || HudSettings.audioVolume == 0) {
            reset();
            return;
        }

        FlightWarnings.Alert candidate = FlightWarnings.current();
        long now = System.currentTimeMillis();
        if (!candidate.message().equals(observed)) {
            observed = candidate.message();
            observedSince = now;
        }

        if (!candidate.message().equals(active.message())) {
            int nextPriority = priority(candidate);
            int oldPriority = priority(active);
            long stableFor = now - observedSince;
            long requiredStability = nextPriority > oldPriority ? 0
                    : nextPriority == oldPriority ? EQUAL_CHANGE_MS
                    : nextPriority == 0 ? CLEAR_MS : DOWNGRADE_MS;

            if (stableFor >= requiredStability) {
                active = candidate;
                lastAnnounced = now;
                if (active.severity() == FlightWarnings.Severity.NONE) {
                    FmsSounds.stopWarningAudio();
                } else {
                    // A new, higher-priority alert cuts the old clip immediately.
                    // Beeping tones are never overlaid before spoken warnings.
                    FmsSounds.play(soundFor(active));
                }
            }
            return;
        }

        // Only repeat a persistent alert when its previous clip is finished.
        if (active.severity() != FlightWarnings.Severity.NONE
                && now - lastAnnounced >= REPEAT_MS
                && !FmsSounds.isWarningPlaying()) {
            FmsSounds.play(soundFor(active));
            lastAnnounced = now;
        }
    }

    private static int priority(FlightWarnings.Alert alert) {
        return switch (alert.message()) {
            case "PULL UP" -> 100;
            case "TERRAIN" -> 95;
            case "LAND NOW" -> 90;
            case "HIGH SPEED DESCENT" -> 85;
            case "LAND SOON" -> 80;
            case "TERRAIN AHEAD" -> 65;
            case "LOW TERRAIN" -> 60;
            case "LOW ELYTRA" -> 55;
            case "ALTITUDE" -> 50;
            case "ELYTRA HEALTH" -> 40;
            case "SINK RATE" -> 35;
            case "BINGO FUEL" -> 30;
            case "CHECK WINGS - CRITICAL" -> 80;
            case "CHECK WINGS" -> 45;
            default -> alert.severity() == FlightWarnings.Severity.NONE ? 0 : 20;
        };
    }

    private static void reset() {
        FmsSounds.stopWarningAudio();
        active = FlightWarnings.Alert.none();
        observed = "";
        observedSince = 0;
        lastAnnounced = 0;
    }

    private static SoundEvent soundFor(FlightWarnings.Alert alert) {
        int index = switch (alert.message()) {
            case "BINGO FUEL" -> 0;
            case "SINK RATE" -> 1;
            case "HIGH SPEED DESCENT" -> 2;
            case "CHECK WINGS" -> 3;
            case "CHECK WINGS - CRITICAL" -> 4;
            case "LOW TERRAIN" -> 5;
            case "TERRAIN AHEAD" -> 6;
            case "TERRAIN" -> 7;
            case "PULL UP" -> 8;
            case "ALTITUDE" -> 9;
            case "ELYTRA HEALTH" -> 10;
            case "LOW ELYTRA" -> 11;
            case "LAND SOON" -> 12;
            case "LAND NOW" -> 13;
            default -> 1;
        };
        return FmsSounds.resolve(HudSettings.alertSound[index]);
    }
}
