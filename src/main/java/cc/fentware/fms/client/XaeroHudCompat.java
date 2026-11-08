package cc.fentware.fms.client;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Optional minimap HUD collision avoidance. This intentionally does not inspect Xaero's
 * internal settings or pretend to know the user's exact map dimensions/placement.
 * Select the configured map corner in FMS settings, or disable avoidance entirely.
 */
public final class XaeroHudCompat {
    private static final boolean INSTALLED =
            FabricLoader.getInstance().isModLoaded("xaerominimap")
            || FabricLoader.getInstance().isModLoaded("xaerominimapfair");

    private XaeroHudCompat() {}

    public static boolean enabled() {
        return INSTALLED && HudSettings.avoidXaeroMinimap;
    }

    /** Return a vertical position that moves a colliding FMS boxed panel clear of the corner reserve. */
    public static int avoid(int x, int y, int panelWidth, int panelHeight, int screenWidth, int screenHeight) {
        if (!enabled()) return y;
        final int reserveWidth = Math.min(154, Math.max(72, screenWidth / 3));
        final int reserveHeight = Math.min(156, Math.max(72, screenHeight / 2));
        final boolean right = HudSettings.minimapCorner.isRight();
        final boolean bottom = HudSettings.minimapCorner.isBottom();
        final int rx = right ? screenWidth - reserveWidth : 0;
        final int ry = bottom ? screenHeight - reserveHeight : 0;
        if (!overlap(x, y, panelWidth, panelHeight, rx, ry, reserveWidth, reserveHeight)) return y;

        // Move the panel to the opposite screen edge; no persistent layout mutation.
        int targetY = bottom ? 8 : Math.max(8, screenHeight - panelHeight - 8);
        if (!overlap(x, targetY, panelWidth, panelHeight, rx, ry, reserveWidth, reserveHeight)) return targetY;

        // Tiny windows: put the panel immediately after/before the reserved space.
        return bottom
                ? Math.max(0, ry - panelHeight - 4)
                : Math.min(Math.max(0, screenHeight - panelHeight), reserveHeight + 4);
    }

    private static boolean overlap(int x, int y, int w, int h, int rx, int ry, int rw, int rh) {
        return x < rx + rw && x + w > rx && y < ry + rh && y + h > ry;
    }
}
