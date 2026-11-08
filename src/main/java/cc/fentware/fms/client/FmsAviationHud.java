package cc.fentware.fms.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import java.util.Locale;

/**
 * Phase 1.5: transparent aircraft-style flight symbology.
 * Uses axis-aligned raster primitives supported by the 26.3 HUD extractor.
 * The flight path marker is derived from actual movement and yaw; roll is not simulated.
 */
public final class FmsAviationHud {
    private static final int GREEN = 0xFF72F3A4;
    private static final int AMBER = 0xFFFFC46C;
    private static final int RED = 0xFFFF6666;

    private FmsAviationHud() {}

    public static void render(GuiGraphicsExtractor g, boolean hybrid) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        int w = g.guiWidth(), h = g.guiHeight();
        int cx = w / 2, cy = h / 2;
        double pitch = mc.player.getXRot();
        double yaw = Math.floorMod(Math.round(mc.player.getYRot() + 180), 360);
        Vec3 velocity = mc.player.getDeltaMovement();
        double speed = Math.hypot(velocity.x, velocity.z) * 20;
        double vertical = velocity.y * 20;
        double altitude = mc.player.getY();
        ItemStack chest = mc.player.getItemBySlot(EquipmentSlot.CHEST);
        int durability = chest.is(Items.ELYTRA) ? chest.getMaxDamage() - chest.getDamageValue() : 0;
        int rocketCount = mc.player.getInventory().countItem(Items.FIREWORK_ROCKET);

        // All symbology stays within reasonable bounds on smaller game windows.
        int half = Math.max(48, Math.min(125, (w - 70) / 4));
        int top = cy - Math.min(65, h / 5), bottom = cy + Math.min(65, h / 5);
        int left = cx - half, right = cx + half;

        // Heading tape with moving ticks and fixed center pointer.
        int headingY = Math.max(15, cy - 99);
        label(g, "HDG " + String.format(Locale.ROOT, "%03.0f", yaw), cx - 24, headingY - 18, GREEN);
        for (int deg = -45; deg <= 45; deg += 15) {
            int tickX = cx + (int) Math.round(deg * half / 65.0);
            vline(g, tickX, headingY, 5, GREEN);
            String name = String.format(Locale.ROOT, "%03d", Math.floorMod((int) yaw + deg, 360));
            label(g, name, tickX - 9, headingY - 11, GREEN);
        }
        vline(g, cx, headingY - 1, 9, AMBER);

        // Pitch ladder: the view pitch moves the horizon and numbered lines.
        // A positive Minecraft X rotation is nose-down.
        for (int angle = -60; angle <= 60; angle += 10) {
            int y = cy + (int) Math.round((angle - pitch) * 2.1);
            if (y < top || y > bottom) continue;
            int lineHalf = angle == 0 ? 60 : 28;
            int tint = angle == 0 ? AMBER : GREEN;
            hline(g, cx - lineHalf, y, lineHalf - 8, tint);
            hline(g, cx + 8, y, lineHalf - 8, tint);
            if (angle != 0) {
                label(g, String.valueOf(Math.abs(angle)), cx - lineHalf - 18, y - 4, GREEN);
                label(g, String.valueOf(Math.abs(angle)), cx + lineHalf + 5, y - 4, GREEN);
            }
        }

        // Fixed aircraft reference symbol and dynamic velocity vector marker.
        hline(g, cx - 34, cy, 22, GREEN);
        hline(g, cx + 12, cy, 22, GREEN);
        vline(g, cx, cy - 6, 13, GREEN);

        double horizontal = Math.hypot(velocity.x, velocity.z);
        if (horizontal > 0.04) {
            // Minecraft: yaw 0 faces +Z, yaw 90 faces -X.
            double motionYaw = Math.toDegrees(Math.atan2(-velocity.x, velocity.z));
            double headingDelta = wrapDegrees(motionYaw - mc.player.getYRot());
            double flightPitch = Math.toDegrees(Math.atan2(-velocity.y, horizontal));
            int markerX = cx + (int) Math.round(clamp(headingDelta * 2.0, -half + 16, half - 16));
            int markerY = cy + (int) Math.round(clamp((flightPitch - pitch) * 2.1, -52, 52));
            hline(g, markerX - 13, markerY, 8, GREEN);
            hline(g, markerX + 5, markerY, 8, GREEN);
            vline(g, markerX, markerY + 4, 7, GREEN);
            // Tiny open square approximates flight-path circle without a custom shader.
            hline(g, markerX - 4, markerY - 4, 9, GREEN);
            hline(g, markerX - 4, markerY + 4, 9, GREEN);
            vline(g, markerX - 4, markerY - 4, 9, GREEN);
            vline(g, markerX + 4, markerY - 4, 9, GREEN);
        }

        // Speed and altitude ladder tapes, in blocks/sec and world Y.
        int speedX = Math.max(9, left - 56);
        int altX = Math.min(w - 55, right + 24);
        label(g, "SPD", speedX, top - 13, GREEN);
        label(g, "ALT", altX, top - 13, GREEN);
        for (int step = -3; step <= 3; step++) {
            int rowY = cy + step * 18;
            if (rowY < top || rowY > bottom) continue;
            hline(g, speedX + 31, rowY, 10, GREEN);
            hline(g, altX - 8, rowY, 10, GREEN);
            label(g, String.format(Locale.ROOT, "%.0f", Math.max(0, speed - step * 5)), speedX, rowY - 5, GREEN);
            label(g, String.format(Locale.ROOT, "%.0f", altitude - step * 20), altX + 5, rowY - 5, GREEN);
        }
        g.fill(speedX - 3, cy - 9, speedX + 44, cy + 8, 0xD0142D20);
        g.fill(altX - 11, cy - 9, altX + 52, cy + 8, 0xD0142D20);
        label(g, String.format(Locale.ROOT, "%.1f", speed), speedX, cy - 5, GREEN);
        label(g, String.format(Locale.ROOT, "%.0f", altitude), altX + 3, cy - 5, GREEN);

        int footY = Math.min(h - 22, bottom + 23);
        label(g, String.format(Locale.ROOT, "VS %+05.1f", vertical), cx - 25, footY, vertical < -16 ? AMBER : GREEN);
        TerrainAwareness.Reading local = TerrainAwareness.current();
        if (Double.isFinite(local.clearance())) {
            label(g, String.format(Locale.ROOT, "AGL %.0f", Math.max(0, local.clearance())),
                    Math.max(4, cx - 100), footY, local.clearance() < 25 ? AMBER : GREEN);
        }
        label(g, "WING " + durability, Math.max(5, cx - 110), Math.min(h - 12, footY + 13), durability <= 20 ? RED : GREEN);
        label(g, "RKT " + rocketCount, Math.min(w - 48, cx + 65), Math.min(h - 12, footY + 13), rocketCount <= 3 ? AMBER : GREEN);

        if (FmsClient.hasDestination()) {
            double dx = FmsClient.destX() - mc.player.getX();
            double dz = FmsClient.destZ() - mc.player.getZ();
            double dist = Math.hypot(dx, dz);
            double bearing = Math.toDegrees(Math.atan2(-dx, dz));
            double delta = wrapDegrees(bearing - mc.player.getYRot());
            int navX = cx + (int) Math.round(clamp(delta * 2.0, -half, half));
            label(g, "NAV", navX - 10, headingY + 10, AMBER);
            String nav = speed > .5
                    ? String.format(Locale.ROOT, "DST %.0f  ETA %s", dist, eta(dist / speed))
                    : String.format(Locale.ROOT, "DST %.0f  ETA --:--", dist);
            label(g, nav, Math.max(4, cx - nav.length() * 3), Math.min(h - 10, footY + 27), GREEN);
            String destinationLabel = FmsClient.destName();
            if (destinationLabel.length() > 22) destinationLabel = destinationLabel.substring(0, 21) + "~";
            label(g, "TO " + destinationLabel, Math.max(4, cx - 65),
                    Math.min(h - 10, footY + 40), AMBER);
            if (HudSettings.cruiseGuidance) {
                CruiseGuidance.Guidance plan = CruiseGuidance.current();
                if (plan != null) {
                    String correction = Math.abs(plan.headingError()) < 5 ? "ON HDG"
                            : plan.headingError() > 0 ? "TURN R " + Math.round(plan.headingError())
                            : "TURN L " + Math.round(-plan.headingError());
                    label(g, "CRZ Y " + plan.recommendedY() + " " + plan.altitudeAdvice() + "  " + correction,
                            Math.max(4, cx - 95), Math.min(h - 10, footY + 53), AMBER);
                    label(g, "RKT ~" + plan.fuel().totalRockets() + " / " + plan.fuel().availableRockets()
                                    + (plan.completeSurvey() ? " LOCAL" : " UNKNOWN ROUTE"),
                            Math.max(4, cx - 92), Math.min(h - 10, footY + 66),
                            plan.fuel().sufficient() ? GREEN : AMBER);
                }
            }
        } else {
            label(g, "NAV NO DEST", cx - 35, Math.min(h - 10, footY + 27), AMBER);
        }

        if (hybrid && !FmsClient.hasDestination()) {
            label(g, "UNSURVEYED TERRAIN  Y " + HudSettings.unknownCruiseY, Math.max(4, cx - 85),
                    Math.min(h - 10, footY + 53), AMBER);
        }
        // Phase 1 has no terrain survey: do not present altitude guidance as confirmed safe.
        FlightWarnings.Alert alert = FlightWarnings.current();
        if (alert.severity() != FlightWarnings.Severity.NONE) {
            int color = alert.severity() == FlightWarnings.Severity.CRITICAL ? RED : AMBER;
            String warning = alert.message();
            int warnX = Math.max(3, cx - warning.length() * 3);
            int warnY = Math.max(4, headingY - 36);
            g.fill(warnX - 5, warnY - 4, Math.min(w, warnX + warning.length() * 6 + 5), warnY + 12, 0xC0201010);
            label(g, warning, warnX, warnY, color);
        }
    }

    private static double wrapDegrees(double x) {
        return (x % 360 + 540) % 360 - 180;
    }
    private static double clamp(double value, double lower, double upper) {
        return Math.max(lower, Math.min(upper, value));
    }
    private static String eta(double seconds) {
        long t = Math.max(0, Math.round(seconds));
        return String.format(Locale.ROOT, "%02d:%02d", t / 60, t % 60);
    }
    private static void hline(GuiGraphicsExtractor g, int x, int y, int length, int color) {
        if (length > 0) g.fill(x, y, x + length, y + 1, color);
    }
    private static void vline(GuiGraphicsExtractor g, int x, int y, int length, int color) {
        if (length > 0) g.fill(x, y, x + 1, y + length, color);
    }
    private static void label(GuiGraphicsExtractor g, String text, int x, int y, int color) {
        g.text(Minecraft.getInstance().font, text, x, y, color, true);
    }
}
