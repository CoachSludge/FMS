package cc.fentware.fms.client;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;

/**
 * Trigger audio on warning transitions, not every HUD frame.
 * Sound is one-shot with per-alert cooldown; never claims terrain-aware collision detection.
 */
public final class WarningAudioController {
    private static String lastMessage = "";
    private static String pendingVoice = "";
    private static long pendingAt;
    private static long lastSoundTime;
    private static final long REPEAT_MS = 12000;
    private static final long TRANSITION_MS = 1200;
    private static final long TONE_GAP_MS = 500;

    private WarningAudioController() {}

    public static void tick(Minecraft client) {
        if (client.player == null || !client.player.isFallFlying() || !HudSettings.audioEnabled) {
            reset();
            return;
        }
        FlightWarnings.Alert alert = FlightWarnings.current();
        if (alert.severity() == FlightWarnings.Severity.NONE) {
            reset();
            return;
        }

        long now = System.currentTimeMillis();
        boolean changed = !alert.message().equals(lastMessage);

        // Drop queued callouts if the actual active warning has changed.
        if (!pendingVoice.isEmpty() && !pendingVoice.equals(alert.message())) pendingVoice = "";
        if (!pendingVoice.isEmpty() && now >= pendingAt) {
            FmsSounds.play(soundFor(alert));
            pendingVoice = "";
        }

        boolean urgent = ("PULL UP".equals(alert.message()) || "LAND NOW".equals(alert.message())) && changed;
        boolean due = changed ? now - lastSoundTime >= TRANSITION_MS : now - lastSoundTime >= REPEAT_MS;
        if (urgent || due) {
            // Master tones on warning transitions; repeats play the named callout only.
            boolean shouldTone = changed && HudSettings.warningTones;
            if (shouldTone) {
                String selectedTone = alert.severity() == FlightWarnings.Severity.CRITICAL
                        ? HudSettings.criticalTone : HudSettings.cautionTone;
                FmsSounds.previewName(selectedTone);
            }
            if (urgent || !shouldTone) {
                // Critical flight danger must not wait for a tone to finish.
                FmsSounds.play(soundFor(alert));
                pendingVoice = "";
            } else {
                pendingVoice = alert.message();
                pendingAt = now + TONE_GAP_MS;
            }
            lastSoundTime = now;
        }
        lastMessage = alert.message();
    }

    private static void reset() {
        lastMessage = "";
        pendingVoice = "";
        lastSoundTime = 0;
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
