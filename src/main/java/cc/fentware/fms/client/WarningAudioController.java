package cc.fentware.fms.client;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvent;

/**
 * Trigger audio on warning transitions, not every HUD frame.
 * Sound is one-shot with per-alert cooldown; never claims terrain-aware collision detection.
 */
public final class WarningAudioController {
    private static String lastMessage = "";
    private static long lastSoundTime;
    private static final long REPEAT_MS = 12000;

    private WarningAudioController() {}

    public static void tick(Minecraft client) {
        if (client.player == null || !client.player.isFallFlying()) {
            lastMessage = "";
            return;
        }
        FlightWarnings.Alert alert = FlightWarnings.current();
        if (alert.severity() == FlightWarnings.Severity.NONE) {
            lastMessage = "";
            return;
        }

        long now = System.currentTimeMillis();
        boolean changed = !alert.message().equals(lastMessage);
        // A minimum separation protects against rapid oscillation between warnings.
        if ((changed && now - lastSoundTime >= 1200) || (!changed && now - lastSoundTime >= REPEAT_MS)) {
            FmsSounds.play(soundFor(alert));
            lastSoundTime = now;
        }
        lastMessage = alert.message();
    }

    private static SoundEvent soundFor(FlightWarnings.Alert alert) {
        int index = switch (alert.message()) {
            case "BINGO FUEL" -> 0;
            case "SINK RATE" -> 1;
            case "HIGH SPEED DESCENT" -> 2;
            case "CHECK WINGS" -> 3;
            case "CHECK WINGS - CRITICAL" -> 4;
            default -> 1;
        };
        return FmsSounds.resolve(HudSettings.alertSound[index]);
    }
}
