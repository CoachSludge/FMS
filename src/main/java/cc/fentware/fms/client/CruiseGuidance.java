package cc.fentware.fms.client;

import cc.fentware.fms.core.CruiseMath;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap;

/** Read-only flight-plan estimate using only already loaded terrain. */
public final class CruiseGuidance {
    public record Guidance(double distance, double bearing, double headingError,
                           int recommendedY, boolean completeSurvey,
                           CruiseMath.FuelEstimate fuel, String altitudeAdvice) {}

    private static Object lastWorld;
    private static int lastTick = Integer.MIN_VALUE;
    private static Guidance cached;

    private CruiseGuidance() {}

    public static Guidance current() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || !FmsClient.hasDestination()) {
            cached = null;
            return null;
        }
        if (mc.level == lastWorld && mc.player.tickCount == lastTick) return cached;
        lastWorld = mc.level;
        lastTick = mc.player.tickCount;

        double x = mc.player.getX(), z = mc.player.getZ();
        double dx = FmsClient.destX() - x, dz = FmsClient.destZ() - z;
        double dist = Math.hypot(dx, dz);
        int samples = Math.min(48, Math.max(1, (int) Math.ceil(dist / 32.0)));
        int maxTop = Integer.MIN_VALUE;
        boolean complete = dist <= 32;
        // For distant routes, sample only the first 768 blocks: distant terrain is UNKNOWN.
        double sampledDistance = Math.min(768, dist);
        complete = dist <= 768;
        for (int i = 0; i <= samples; i++) {
            double fraction = (i / (double) samples) * (dist <= 0 ? 0 : sampledDistance / dist);
            int bx = (int) Math.floor(x + dx * fraction);
            int bz = (int) Math.floor(z + dz * fraction);
            if (!mc.level.hasChunk(bx >> 4, bz >> 4)) {
                complete = false;
                continue;
            }
            int top = mc.level.getHeight(Heightmap.Types.MOTION_BLOCKING, bx, bz);
            maxTop = Math.max(maxTop, top);
        }
        int suggested = maxTop == Integer.MIN_VALUE ? HudSettings.unknownCruiseY
                : Math.max(maxTop + HudSettings.cruiseClearance,
                           complete ? 0 : HudSettings.unknownCruiseY);
        suggested = Math.min(300, Math.max(64, suggested));
        if (HudSettings.cruiseOverrideY > 0) suggested = HudSettings.cruiseOverrideY;
        int rockets = mc.player.getInventory().countItem(Items.FIREWORK_ROCKET);
        var fuel = CruiseMath.estimateRockets(dist, HudSettings.blocksPerRocket,
                HudSettings.rocketReserve, rockets);
        double bearing = CruiseMath.bearingDegrees(x, z, FmsClient.destX(), FmsClient.destZ());
        double error = CruiseMath.headingError((mc.player.getYRot() + 180 + 360) % 360, bearing);
        cached = new Guidance(dist, bearing, error, suggested, complete, fuel,
                CruiseMath.altitudeAdvice(mc.player.getY(), suggested));
        return cached;
    }
}
