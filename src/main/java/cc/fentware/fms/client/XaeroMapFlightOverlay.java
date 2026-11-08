package cc.fentware.fms.client;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

import java.util.Locale;

/**
 * A small read-only flight-plan overlay on Xaero's World Map screen.
 *
 * Uses only Fabric's screen extraction callback; no Xaero class linking,
 * mixins, reflection, waypoint writes, or modified map input handling.
 */
public final class XaeroMapFlightOverlay {
    private static final int WIDTH = 211;
    private static final int COLOR = 0xFF72F3A4;
    private static final int AMBER = 0xFFFFC46C;
    private static final int MUTED = 0xFFB0BBB4;

    private XaeroMapFlightOverlay() {}

    public static void initialize() {
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (!isXaeroWorldMap(screen)) return;
            ScreenEvents.afterExtract(screen).register((rendered, graphics, mouseX, mouseY, delta) ->
                    render(graphics));
        });
    }

    private static boolean isXaeroWorldMap(Screen screen) {
        if (!FabricLoader.getInstance().isModLoaded("xaeroworldmap")) return false;
        String name = screen.getClass().getName().toLowerCase(Locale.ROOT);
        // Restrict overlay to Xaero's actual fullscreen map, not waypoint settings
        // or unrelated Xaero UI. If Xaero changes its class name, do nothing.
        return name.equals("xaero.map.gui.guimap") || name.endsWith(".guimap");
    }

    private static void render(GuiGraphicsExtractor g) {
        if (!HudSettings.mapFlightPanel || !HudSettings.cruiseGuidance) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        int x = Math.max(5, g.guiWidth() - WIDTH - 8);
        int y = 24;
        int height = FmsClient.hasDestination() ? 113 : 57;
        g.fill(x, y, x + WIDTH, y + height, 0xE0192925);
        g.fill(x, y, x + WIDTH, y + 2, COLOR);
        line(g, x, y, 8, "FMS / FLIGHT PLAN", COLOR);
        line(g, x, y, 21, String.format(Locale.ROOT,
                "YOU  X %.0f  Y %.0f  Z %.0f",
                mc.player.getX(), mc.player.getY(), mc.player.getZ()), MUTED);

        if (!FmsClient.hasDestination()) {
            line(g, x, y, 36, "NO DESTINATION SELECTED", AMBER);
            line(g, x, y, 47, "/fms xaero list   or   /fms go X Z", MUTED);
            return;
        }
        CruiseGuidance.Guidance plan = CruiseGuidance.current();
        if (plan == null) return;
        String name = FmsClient.destName();
        if (name.length() > 25) name = name.substring(0, 24) + "~";
        line(g, x, y, 35, "TO " + name, COLOR);
        line(g, x, y, 47, String.format(Locale.ROOT,
                "DEST X %.0f  Z %.0f", FmsClient.destX(), FmsClient.destZ()), COLOR);
        line(g, x, y, 59, String.format(Locale.ROOT,
                "DIST %.0f   HDG %03.0f", plan.distance(), plan.bearing()), COLOR);
        line(g, x, y, 71, "CRUISE Y " + plan.recommendedY() + " / " + plan.altitudeAdvice(), AMBER);
        line(g, x, y, 83, "ROCKETS ~" + plan.fuel().totalRockets() + " / " +
                plan.fuel().availableRockets() + " HELD", plan.fuel().sufficient() ? COLOR : AMBER);
        line(g, x, y, 95, plan.completeSurvey()
                ? "TERRAIN: LOCAL SAMPLE ONLY" : "TERRAIN: ROUTE UNSURVEYED", AMBER);
        line(g, x, y, 105, "ADVISORY ONLY - NO AUTOPILOT", MUTED);
    }

    private static void line(GuiGraphicsExtractor g, int x, int y, int dy, String text, int color) {
        g.text(Minecraft.getInstance().font, text, x + 7, y + dy, color, false);
    }
}
