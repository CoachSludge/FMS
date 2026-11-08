package cc.fentware.fms.core;

/** Pure warning policies. Audio priority/debouncing belongs in the client adapter. */
public final class WarningLogic {
    private WarningLogic() {}
    public enum Warning { NONE, TERRAIN_UNKNOWN, LOW_WINGS, BINGO_FUEL, TERRAIN, PULL_UP }
    public record Inputs(boolean terrainUnknown, double projectedImpactSeconds,
                         int remainingDurability, int remainingRockets, int rocketReserve) {}

    public static Warning evaluate(Inputs input) {
        if (Double.isFinite(input.projectedImpactSeconds()) && input.projectedImpactSeconds() >= 0) {
            if (input.projectedImpactSeconds() <= 2.5) return Warning.PULL_UP;
            if (input.projectedImpactSeconds() <= 6.0) return Warning.TERRAIN;
        }
        if (input.remainingDurability() <= 10) return Warning.LOW_WINGS;
        if (input.remainingRockets() <= input.rocketReserve()) return Warning.BINGO_FUEL;
        if (input.terrainUnknown()) return Warning.TERRAIN_UNKNOWN;
        return Warning.NONE;
    }
}
