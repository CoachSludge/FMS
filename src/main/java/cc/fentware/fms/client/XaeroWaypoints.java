package cc.fentware.fms.client;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Read-only bridge for Xaero's Minimap/World Map waypoint files.
 * The World Map displays Minimap waypoints when both mods are installed.
 * Never modifies Xaero's saved data and never reads arbitrary paths.
 */
public final class XaeroWaypoints {
    public record Waypoint(String name, String set, String world, String dimension,
                           int x, int z, Path source) {}
    private static final int MAX_FILES = 128;
    private static final int MAX_WAYPOINTS = 400;
    private static final long MAX_FILE_BYTES = 1024 * 1024;

    private XaeroWaypoints() {}

    public static List<Waypoint> list() {
        Path game = FabricLoader.getInstance().getGameDir();
        Path modern = game.resolve("xaero").resolve("minimap");
        Path legacy = game.resolve("XaeroWaypoints");
        Path root = Files.isDirectory(modern) ? modern : legacy;
        if (!Files.isDirectory(root)) return List.of();

        List<Waypoint> results = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(root, 3)) {
            List<Path> files = paths.filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(".txt"))
                    .filter(p -> p.getParent() != null && p.getParent().getFileName().toString().startsWith("dim%"))
                    .filter(p -> !p.toString().contains("backup"))
                    .sorted(Comparator.comparing(Path::toString))
                    .limit(MAX_FILES).toList();
            for (Path file : files) {
                if (results.size() >= MAX_WAYPOINTS) break;
                if (Files.size(file) > MAX_FILE_BYTES) continue;
                Path relative = root.relativize(file);
                if (relative.getNameCount() != 3) continue;
                String world = decode(relative.getName(0).toString());
                String dimension = relative.getName(1).toString();
                try (Stream<String> lines = Files.lines(file, StandardCharsets.UTF_8)) {
                    lines.filter(line -> line.startsWith("waypoint:")).forEach(line -> {
                        if (results.size() >= MAX_WAYPOINTS) return;
                        String[] fields = line.split(":", -1);
                        if (fields.length < 10) return;
                        try {
                            int x = Integer.parseInt(fields[3]);
                            int z = Integer.parseInt(fields[5]);
                            boolean disabled = Boolean.parseBoolean(fields[7]);
                            if (disabled) return;
                            results.add(new Waypoint(decode(fields[1]), decode(fields[9]),
                                    world, dimension, x, z, file));
                        } catch (NumberFormatException ignored) {
                            // Ignore malformed waypoint lines without affecting the rest.
                        }
                    });
                }
            }
        } catch (IOException e) {
            System.err.println("[FMS] Unable to read Xaero waypoints: " + e.getMessage());
        }
        return List.copyOf(results);
    }

    private static String decode(String name) {
        return name.replace("§§", ":").replace("%us%", "_")
                .replace("%fs%", "/").replace("%bs%", "\\");
    }
}
