package cc.fentware.fms.core;

import java.util.List;
import java.util.Objects;

/** Pure calculation layer; no Minecraft or Xaero dependencies. */
public final class FlightPlanner {
    private FlightPlanner() {}

    public record Coordinate(double x, double z) {}
    /** A terrain sample along the route. Empty elevation means uncharted/unreliable terrain. */
    public record TerrainSample(Coordinate position, Integer topY) {}
    public record Settings(int defaultUnknownY, int terrainClearance, int maxFlightY) {
        public Settings {
            if (terrainClearance < 0 || maxFlightY < 1) throw new IllegalArgumentException("Invalid flight bounds");
            if (defaultUnknownY > maxFlightY) throw new IllegalArgumentException("Unknown-terrain target exceeds ceiling");
        }
        public static Settings defaults() { return new Settings(250, 40, 300); }
    }
    public enum SurveyStatus { KNOWN, PARTIAL, UNKNOWN }
    public record Plan(int targetY, SurveyStatus surveyStatus, boolean ceilingConstrained,
                       int knownSamples, int unknownSamples, double horizontalDistance) {}

    /** Plan from already sampled terrain. Terrain provider is intentionally replaceable. */
    public static Plan plan(Coordinate origin, Coordinate destination,
                            List<TerrainSample> samples, Settings settings) {
        Objects.requireNonNull(origin);
        Objects.requireNonNull(destination);
        Objects.requireNonNull(samples);
        Objects.requireNonNull(settings);

        int known = 0, unknown = 0;
        int desiredY = Integer.MIN_VALUE;
        for (TerrainSample sample : samples) {
            if (sample.topY() == null) {
                unknown++;
            } else {
                known++;
                desiredY = Math.max(desiredY, saturatingAdd(sample.topY(), settings.terrainClearance()));
            }
        }
        // No samples is unknown, never "clear".
        if (samples.isEmpty()) unknown++;
        SurveyStatus status = known == 0 ? SurveyStatus.UNKNOWN
                : unknown == 0 ? SurveyStatus.KNOWN : SurveyStatus.PARTIAL;
        if (unknown > 0) desiredY = Math.max(desiredY, settings.defaultUnknownY());
        boolean constrained = desiredY > settings.maxFlightY();
        int target = Math.min(settings.maxFlightY(), desiredY);
        double distance = Math.hypot(destination.x() - origin.x(), destination.z() - origin.z());
        return new Plan(target, status, constrained, known, unknown, distance);
    }

    public static double etaSeconds(double distanceBlocks, double horizontalBlocksPerSecond) {
        if (distanceBlocks < 0 || !Double.isFinite(distanceBlocks)) throw new IllegalArgumentException("distance");
        if (horizontalBlocksPerSecond <= 0 || !Double.isFinite(horizontalBlocksPerSecond)) return Double.NaN;
        return distanceBlocks / horizontalBlocksPerSecond;
    }

    private static int saturatingAdd(int a, int b) {
        long sum = (long) a + b;
        return (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, sum));
    }
}
