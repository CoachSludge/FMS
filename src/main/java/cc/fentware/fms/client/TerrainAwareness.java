package cc.fentware.fms.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

/**
 * Client-only terrain sampling of loaded chunks. This does not use Xaero's
 * saved map, does not force-load chunks, and cannot see distant/unknown terrain.
 * Heightmaps do not detect overhangs, caves, or every horizontal obstacle.
 */
public final class TerrainAwareness {
    public enum Threat { NONE, LOW_TERRAIN, TERRAIN_AHEAD, TERRAIN, PULL_UP }
    public record Reading(Threat threat, double clearance, double timeToTerrain, boolean complete) {
        public static Reading unknown() { return new Reading(Threat.NONE, Double.NaN, Double.NaN, false); }
    }

    private static Reading cached = Reading.unknown();
    private static int lastTick = -1;
    private static long lastWorldTime = Long.MIN_VALUE;

    private TerrainAwareness() {}

    public static Reading current() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || !mc.player.isFallFlying()) {
            cached = Reading.unknown();
            return cached;
        }
        long worldTime = mc.level.getGameTime();
        if (mc.player.tickCount == lastTick && worldTime == lastWorldTime) return cached;
        lastTick = mc.player.tickCount;
        lastWorldTime = worldTime;
        cached = sample(mc);
        return cached;
    }

    private static Reading sample(Minecraft mc) {
        var player = mc.player;
        var level = mc.level;
        Vec3 movement = player.getDeltaMovement();
        double horizontal = Math.hypot(movement.x, movement.z);
        double speed = horizontal * 20;
        double y = player.getY();
        int x = (int) Math.floor(player.getX());
        int z = (int) Math.floor(player.getZ());
        if (!level.hasChunk(x >> 4, z >> 4)) return Reading.unknown();

        double localHeight = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
        double clearance = y - localHeight;
        // Ground-level caution uses measured loaded local surface, not global Y.
        Threat threat = clearance < 7 ? Threat.LOW_TERRAIN : Threat.NONE;
        double firstSeconds = Double.NaN;
        boolean allLoaded = true;

        // Only project meaningful horizontal flight. Predicted position uses
        // actual movement including descent (not camera look direction).
        if (speed > 3) {
            double lookaheadSeconds = Math.min(3.0, 48.0 / speed);
            int steps = 10;
            for (int i = 1; i <= steps; i++) {
                double seconds = lookaheadSeconds * i / steps;
                int sx = (int) Math.floor(player.getX() + movement.x * seconds * 20);
                int sz = (int) Math.floor(player.getZ() + movement.z * seconds * 20);
                if (!level.hasChunk(sx >> 4, sz >> 4)) {
                    allLoaded = false;
                    break;
                }
                double surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING, sx, sz);
                double futureY = y + movement.y * seconds * 20;
                double margin = futureY - surface;
                if (margin < 3.5 && seconds < 1.15) {
                    threat = Threat.PULL_UP;
                    firstSeconds = seconds;
                    break;
                } else if (margin < 7 && seconds < 1.8 && threat.ordinal() < Threat.TERRAIN.ordinal()) {
                    threat = Threat.TERRAIN;
                    firstSeconds = seconds;
                } else if (margin < 14 && threat.ordinal() < Threat.TERRAIN_AHEAD.ordinal()) {
                    threat = Threat.TERRAIN_AHEAD;
                    firstSeconds = seconds;
                }
            }
        }
        return new Reading(threat, clearance, firstSeconds, allLoaded);
    }
}
