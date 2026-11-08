package cc.fentware.fms.core;

import java.util.List;

/** Dependency-free smoke tests: run `bash scripts/test-core.sh`. */
public final class FlightPlannerSmokeTest {
    public static void main(String[] args) {
        var settings = FlightPlanner.Settings.defaults();
        var start = new FlightPlanner.Coordinate(0, 0);
        var end = new FlightPlanner.Coordinate(300, 400);
        var empty = FlightPlanner.plan(start, end, List.of(), settings);
        check(empty.targetY() == 250 && empty.surveyStatus() == FlightPlanner.SurveyStatus.UNKNOWN, "unknown fallback");
        check(empty.horizontalDistance() == 500, "distance");
        var known = FlightPlanner.plan(start, end, List.of(
                new FlightPlanner.TerrainSample(start, 80),
                new FlightPlanner.TerrainSample(end, 180)), settings);
        check(known.targetY() == 220 && known.surveyStatus() == FlightPlanner.SurveyStatus.KNOWN, "known elevation");
        var partial = FlightPlanner.plan(start, end, List.of(
                new FlightPlanner.TerrainSample(start, 300),
                new FlightPlanner.TerrainSample(end, null)), settings);
        check(partial.targetY() == 300 && partial.ceilingConstrained(), "ceiling alert");
        check(WarningLogic.evaluate(new WarningLogic.Inputs(true, 1.5, 100, 25, 6)) == WarningLogic.Warning.PULL_UP, "impact priority");
        check(WarningLogic.evaluate(new WarningLogic.Inputs(true, Double.NaN, 100, 25, 6)) == WarningLogic.Warning.TERRAIN_UNKNOWN, "unknown alert");
        check(Double.isNaN(FlightPlanner.etaSeconds(100, 0)), "stationary ETA");
        System.out.println("FMS core smoke tests passed");
    }
    private static void check(boolean ok, String message) {
        if (!ok) throw new AssertionError(message);
    }
}
