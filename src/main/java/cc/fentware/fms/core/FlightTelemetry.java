package cc.fentware.fms.core;

/** Snapshot supplied by the eventual Minecraft client adapter. */
public record FlightTelemetry(double x, double y, double z,
                              double velocityX, double velocityY, double velocityZ,
                              int elytraRemainingDurability, int rocketCount,
                              boolean elytraFlying) {
    public double horizontalSpeedBlocksPerSecond() {
        return 20.0 * Math.hypot(velocityX, velocityZ);
    }
    public double verticalSpeedBlocksPerSecond() { return 20.0 * velocityY; }
}
