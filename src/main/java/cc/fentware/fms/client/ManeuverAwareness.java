package cc.fentware.fms.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

/**
 * Lightweight flight maneuver advisories (not a real G-meter).
 * Detects rapid changes to the player's actual horizontal travel direction,
 * rather than just camera movement, and aggressive nose-down Elytra flight.
 * Reads one sample per game tick so both HUD and audio can query it.
 */
public final class ManeuverAwareness {
    public enum Maneuver { NONE, HARD_TURN, STEEP_DIVE }
    private static int previousTick = Integer.MIN_VALUE;
    private static Object previousPlayer;
    private static double previousHeading;
    private static boolean hasHeading;
    private static double filteredTurnRate;
    private static int hardTurnTicks;
    private static Maneuver cached = Maneuver.NONE;

    private ManeuverAwareness() {}

    public static Maneuver current() {
        Minecraft mc = Minecraft.getInstance();
        if (!HudSettings.maneuverWarnings || mc.player == null || !mc.player.isFallFlying()) {
            reset();
            return Maneuver.NONE;
        }

        var player = mc.player;
        if (previousPlayer != player) {
            reset();
            previousPlayer = player;
        }
        if (previousTick == player.tickCount) return cached;
        int deltaTicks = previousTick == Integer.MIN_VALUE ? 1 : player.tickCount - previousTick;
        previousTick = player.tickCount;
        if (deltaTicks <= 0 || deltaTicks > 5) {
            hasHeading = false;
            filteredTurnRate = 0;
            hardTurnTicks = 0;
        }

        Vec3 motion = player.getDeltaMovement();
        double horizontal = Math.hypot(motion.x, motion.z);
        double speed = horizontal * 20.0;
        double downward = -motion.y * 20.0;

        // Steep, fast dive without claiming terrain will be hit.
        if (speed >= 16 && downward > 11 && player.getXRot() > 38) {
            cached = Maneuver.STEEP_DIVE;
        } else {
            cached = Maneuver.NONE;
        }

        // Calculate turn rate from motion heading. Smooth it to avoid one-tick jitters.
        if (speed >= 14) {
            double heading = Math.toDegrees(Math.atan2(-motion.x, motion.z));
            if (hasHeading && deltaTicks > 0 && deltaTicks <= 5) {
                double difference = (heading - previousHeading + 540) % 360 - 180;
                double turnRate = Math.abs(difference) * 20.0 / deltaTicks;
                filteredTurnRate = filteredTurnRate * 0.65 + Math.min(turnRate, 400) * 0.35;
                if (filteredTurnRate >= 55) hardTurnTicks++;
                else hardTurnTicks = Math.max(0, hardTurnTicks - 1);
            }
            previousHeading = heading;
            hasHeading = true;
        } else {
            hasHeading = false;
            filteredTurnRate = 0;
            hardTurnTicks = 0;
        }

        if (cached == Maneuver.NONE && hardTurnTicks >= 3) cached = Maneuver.HARD_TURN;
        return cached;
    }

    private static void reset() {
        previousTick = Integer.MIN_VALUE;
        previousPlayer = null;
        hasHeading = false;
        filteredTurnRate = 0;
        hardTurnTicks = 0;
        cached = Maneuver.NONE;
    }
}
