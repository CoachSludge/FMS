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
    public static boolean enabled = true;
    public static double instrumentsX = 0.02, instrumentsY = 0.12;
    public static double navigationX = 0.76, navigationY = 0.12;
    public static int color = 0xFF72F3A4;
    public static int unknownCruiseY = 250;

    private HudSettings() {}

    public static void load() {
        if (!Files.exists(PATH)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(PATH)) {
            p.load(in);
            enabled = Boolean.parseBoolean(p.getProperty("enabled", "true"));
            instrumentsX = fraction(p, "instrumentsX", 0.02);
            instrumentsY = fraction(p, "instrumentsY", 0.12);
            navigationX = fraction(p, "navigationX", 0.76);
            navigationY = fraction(p, "navigationY", 0.12);
            unknownCruiseY = Math.max(64, Math.min(320, Integer.parseInt(p.getProperty("unknownCruiseY", "250"))));
        } catch (Exception e) {
            System.err.println("[FMS] Unable to read HUD settings: " + e.getMessage());
        }
    }

    private static double fraction(Properties p, String key, double fallback) {
        try { return Math.max(0, Math.min(1, Double.parseDouble(p.getProperty(key, String.valueOf(fallback))))); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    public static void save() {
        Properties p = new Properties();
        p.setProperty("enabled", String.valueOf(enabled));
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
