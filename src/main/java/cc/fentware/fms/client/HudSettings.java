package cc.fentware.fms.client;

import net.fabricmc.loader.api.FabricLoader;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/** Two independent panel positions; uses normalized screen-space coordinates. */
public final class HudSettings {
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("fms-hud.properties");
    public enum Style { BOXED, AVIATION, HYBRID }
    public enum HudMode { ALWAYS, ELYTRA_EQUIPPED, ONLY_FLYING }
    public enum MinimapCorner {
        TOP_RIGHT, TOP_LEFT, BOTTOM_RIGHT, BOTTOM_LEFT;
        public boolean isRight() { return this == TOP_RIGHT || this == BOTTOM_RIGHT; }
        public boolean isBottom() { return this == BOTTOM_RIGHT || this == BOTTOM_LEFT; }
    }
    public static boolean avoidXaeroMinimap = true;
    public static MinimapCorner minimapCorner = MinimapCorner.TOP_RIGHT;
    public static HudMode hudMode = HudMode.ELYTRA_EQUIPPED;
    /** Alert assignments: bingo, sink, descent, wings, critical wings, low terrain, terrain ahead, terrain, pull up, altitude, elytra, low elytra, land, land now. */
    public static String[] alertSound = {"bingo", "caution", "warning", "mastercaution", "warning", "lowterrain", "terrainahead", "terrain", "pullup", "altitude", "elytra", "lowelytra", "land", "landnow", "mastercaution", "mastercaution"};
    public static Style style = Style.AVIATION;
    public static void cycleStyle() {
        Style[] styles = Style.values();
        style = styles[(style.ordinal() + 1) % styles.length];
        save();
    }
    public static boolean maneuverWarnings = true;
    public static boolean terrainWarnings = true;
    public static boolean audioEnabled = true;
    public static int audioVolume = 75;
    public static boolean enabled = true;
    public static double instrumentsX = 0.02, instrumentsY = 0.12;
    public static double navigationX = 0.76, navigationY = 0.12;
    public static int color = 0xFF72F3A4;
    public static int unknownCruiseY = 250;
    public static boolean cruiseGuidance = true;
    public static boolean mapFlightPanel = true;
    public static int cruiseClearance = 40;
    public static int cruiseOverrideY = -1;
    /** Assumed distance per rocket, deliberately conservative and player-tunable. */
    public static int blocksPerRocket = 400;
    public static int rocketReserve = 2;

    private HudSettings() {}

    public static void load() {
        if (!Files.exists(PATH)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(PATH)) {
            p.load(in);
            enabled = Boolean.parseBoolean(p.getProperty("enabled", "true"));
            cruiseGuidance = Boolean.parseBoolean(p.getProperty("cruiseGuidance", "true"));
            mapFlightPanel = Boolean.parseBoolean(p.getProperty("mapFlightPanel", "true"));
            cruiseClearance = bounded(p, "cruiseClearance", 40, 10, 100);
            cruiseOverrideY = bounded(p, "cruiseOverrideY", -1, -1, 300);
            blocksPerRocket = bounded(p, "blocksPerRocket", 400, 100, 2000);
            rocketReserve = bounded(p, "rocketReserve", 2, 0, 16);
            avoidXaeroMinimap = Boolean.parseBoolean(p.getProperty("avoidXaeroMinimap", "true"));
            try { minimapCorner = MinimapCorner.valueOf(p.getProperty("minimapCorner", "TOP_RIGHT")); }
            catch (IllegalArgumentException ignored) { minimapCorner = MinimapCorner.TOP_RIGHT; }
            try { hudMode = HudMode.valueOf(p.getProperty("hudMode", "ELYTRA_EQUIPPED")); }
            catch (IllegalArgumentException ignored) { hudMode = HudMode.ELYTRA_EQUIPPED; }
            for (int i = 0; i < alertSound.length; i++) {
                String stored = p.getProperty("alertSound." + i, alertSound[i]);
                if (FmsSounds.validName(stored)) alertSound[i] = stored;
            }
            maneuverWarnings = Boolean.parseBoolean(p.getProperty("maneuverWarnings", "true"));
            terrainWarnings = Boolean.parseBoolean(p.getProperty("terrainWarnings", "true"));
            audioEnabled = Boolean.parseBoolean(p.getProperty("audioEnabled", "true"));
            try { audioVolume = Math.max(0, Math.min(100, Integer.parseInt(p.getProperty("audioVolume", "75")))); }
            catch (NumberFormatException ignored) { audioVolume = 75; }
            try { style = Style.valueOf(p.getProperty("style", "AVIATION")); }
            catch (IllegalArgumentException ignored) { style = Style.AVIATION; }
            instrumentsX = fraction(p, "instrumentsX", 0.02);
            instrumentsY = fraction(p, "instrumentsY", 0.12);
            navigationX = fraction(p, "navigationX", 0.76);
            navigationY = fraction(p, "navigationY", 0.12);
            unknownCruiseY = Math.max(64, Math.min(320, Integer.parseInt(p.getProperty("unknownCruiseY", "250"))));
        } catch (Exception e) {
            System.err.println("[FMS] Unable to read HUD settings: " + e.getMessage());
        }
    }

    private static int bounded(Properties p, String key, int fallback, int min, int max) {
        try { return Math.max(min, Math.min(max, Integer.parseInt(p.getProperty(key, String.valueOf(fallback))))); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private static double fraction(Properties p, String key, double fallback) {
        try { return Math.max(0, Math.min(1, Double.parseDouble(p.getProperty(key, String.valueOf(fallback))))); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    public static void save() {
        Properties p = new Properties();
        p.setProperty("enabled", String.valueOf(enabled));
        p.setProperty("cruiseGuidance", String.valueOf(cruiseGuidance));
        p.setProperty("mapFlightPanel", String.valueOf(mapFlightPanel));
        p.setProperty("cruiseClearance", String.valueOf(cruiseClearance));
        p.setProperty("cruiseOverrideY", String.valueOf(cruiseOverrideY));
        p.setProperty("blocksPerRocket", String.valueOf(blocksPerRocket));
        p.setProperty("rocketReserve", String.valueOf(rocketReserve));
        p.setProperty("avoidXaeroMinimap", String.valueOf(avoidXaeroMinimap));
        p.setProperty("minimapCorner", minimapCorner.name());
        p.setProperty("hudMode", hudMode.name());
        for (int i = 0; i < alertSound.length; i++) p.setProperty("alertSound." + i, alertSound[i]);
        p.setProperty("maneuverWarnings", String.valueOf(maneuverWarnings));
        p.setProperty("terrainWarnings", String.valueOf(terrainWarnings));
        p.setProperty("audioEnabled", String.valueOf(audioEnabled));
        p.setProperty("audioVolume", String.valueOf(audioVolume));
        p.setProperty("style", style.name());
        p.setProperty("instrumentsX", String.valueOf(instrumentsX));
        p.setProperty("instrumentsY", String.valueOf(instrumentsY));
        p.setProperty("navigationX", String.valueOf(navigationX));
        p.setProperty("navigationY", String.valueOf(navigationY));
        p.setProperty("unknownCruiseY", String.valueOf(unknownCruiseY));
        try {
            Files.createDirectories(PATH.getParent());
            try (OutputStream out = Files.newOutputStream(PATH)) { p.store(out, "FMS HUD layout"); }
        } catch (Exception e) {
            System.err.println("[FMS] Unable to write HUD settings: " + e.getMessage());
        }
    }
}
