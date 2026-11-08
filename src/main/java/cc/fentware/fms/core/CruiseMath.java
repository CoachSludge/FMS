package cc.fentware.fms.core;

/**
 * Advisory-only cruise calculations. Rocket reach is a configurable assumption,
 * not a measurement of the vanilla firework physics or an autopilot guarantee.
 */
public final class CruiseMath {
    private CruiseMath() {}

    public record FuelEstimate(int travelRockets, int reserveRockets, int totalRockets,
                               int availableRockets, boolean sufficient) {}

    public static FuelEstimate estimateRockets(double distance, double blocksPerRocket,
                                               int reserve, int available) {
        if (!Double.isFinite(distance) || distance < 0) throw new IllegalArgumentException("distance");
        if (!Double.isFinite(blocksPerRocket) || blocksPerRocket <= 0) throw new IllegalArgumentException("reach");
        int travel = (int) Math.min(Integer.MAX_VALUE - Math.max(0, reserve),
                Math.ceil(distance / blocksPerRocket));
        int total = travel + Math.max(0, reserve);
        return new FuelEstimate(travel, Math.max(0, reserve), total, Math.max(0, available),
                available >= total);
    }

    public static String altitudeAdvice(double y, double targetY) {
        if (y < targetY - 8) return "CLIMB";
        if (y > targetY + 8) return "DESCEND";
        return "LEVEL";
    }

    public static double bearingDegrees(double x, double z, double destX, double destZ) {
        return (Math.toDegrees(Math.atan2(-(destX - x), destZ - z)) + 360) % 360;
    }

    public static double headingError(double heading, double target) {
        return (target - heading + 540) % 360 - 180;
    }
}
