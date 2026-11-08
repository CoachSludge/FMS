package cc.fentware.fms.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Compact, paged FMS configuration screen.
 * Fits in typical Minecraft scaled windows without requiring wheel scrolling.
 * All hitboxes are based on the same origin and dimensions as the drawn buttons.
 */
public final class FmsSettingsScreen extends Screen {
    private static final int GREEN = 0xFF72F3A4;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int MUTED = 0xFFB0BBB4;
    private static final int AMBER = 0xFFFFC46C;
    private static final int WIDTH = 300;
    private static final int HEIGHT = 208;
    private static final String[] LABELS = {
        "BINGO FUEL", "SINK RATE", "HIGH SPEED DESCENT", "CHECK WINGS", "WINGS CRITICAL"
    };
    private final Screen parent;
    private int page = 1; // Open audio settings first, since they were previously inaccessible.

    public FmsSettingsScreen(Screen parent) {
        super(Component.literal("FMS Settings"));
        this.parent = parent;
    }

    private int originX() { return (width - WIDTH) / 2; }
    private int originY() { return Math.max(2, (height - HEIGHT) / 2); }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        int x = originX(), y = originY();
        g.fill(x, y, x + WIDTH, y + HEIGHT, 0xEF10221B);
        g.fill(x, y, x + WIDTH, y + 2, GREEN);
        g.text(font, "FMS / SETTINGS", x + 9, y + 7, GREEN, true);
        drawButton(g, x + 8, y + 24, 139, "HUD" + (page == 0 ? "  [ACTIVE]" : ""));
        drawButton(g, x + 153, y + 24, 139, "AUDIO" + (page == 1 ? "  [ACTIVE]" : ""));

        if (page == 0) renderHudPage(g, x, y);
        else renderAudioPage(g, x, y);

        g.fill(x + 8, y + 183, x + WIDTH - 8, y + 184, 0xFF396B52);
        g.text(font, "Changes save automatically", x + 9, y + 190, MUTED, false);
        drawButton(g, x + 238, y + 186, 54, "DONE");
    }

    private void renderHudPage(GuiGraphicsExtractor g, int x, int y) {
        g.text(font, "DISPLAY", x + 9, y + 51, AMBER, false);
        drawButton(g, x + 9, y + 64, 282, "HUD VISIBILITY: " + HudSettings.hudMode.name());
        drawButton(g, x + 9, y + 89, 282, "LAYOUT: " + HudSettings.style.name());
        drawButton(g, x + 9, y + 114, 282, "HUD ENABLED: " + (HudSettings.enabled ? "YES" : "NO"));
        drawButton(g, x + 9, y + 136, 282,
                "XAERO SPACE: " + (HudSettings.avoidXaeroMinimap ? "ON" : "OFF"));
        drawButton(g, x + 9, y + 159, 282,
                "MINIMAP CORNER: " + HudSettings.minimapCorner.name());

    }

    private void renderAudioPage(GuiGraphicsExtractor g, int x, int y) {
        drawButton(g, x + 8, y + 47, 110, "AUDIO: " + (HudSettings.audioEnabled ? "ON" : "OFF"));
        drawButton(g, x + 126, y + 47, 45, "VOL -");
        drawButton(g, x + 177, y + 47, 59, HudSettings.audioVolume + "%");
        drawButton(g, x + 242, y + 47, 50, "VOL +");
        g.text(font, "WARNING TYPE", x + 9, y + 72, MUTED, false);
        g.text(font, "ASSIGNED SOUND", x + 125, y + 72, MUTED, false);

        for (int i = 0; i < LABELS.length; i++) {
            int rowY = y + 84 + i * 19;
            g.text(font, LABELS[i], x + 9, rowY + 5, WHITE, false);
            drawButton(g, x + 122, rowY, 112, HudSettings.alertSound[i]);
            drawButton(g, x + 240, rowY, 52, "TEST");
        }
    }

    private void drawButton(GuiGraphicsExtractor g, int x, int y, int w, String label) {
        g.fill(x, y, x + w, y + 17, 0xDD1D3D30);
        g.fill(x, y + 16, x + w, y + 17, 0xFF396B52);
        g.text(font, label, x + 4, y + 4, WHITE, false);
    }

    private static boolean hit(double px, double py, int x, int y, int w) {
        return px >= x && px < x + w && py >= y && py < y + 17;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x(), my = event.y();
        int x = originX(), y = originY();

        if (hit(mx, my, x + 8, y + 24, 139)) { page = 0; return true; }
        if (hit(mx, my, x + 153, y + 24, 139)) { page = 1; return true; }
        if (hit(mx, my, x + 238, y + 186, 54)) { onClose(); return true; }

        if (page == 0) {
            if (hit(mx, my, x + 9, y + 64, 282)) {
                HudSettings.hudMode = HudSettings.HudMode.values()[
                    (HudSettings.hudMode.ordinal() + 1) % HudSettings.HudMode.values().length];
            } else if (hit(mx, my, x + 9, y + 89, 282)) {
                HudSettings.cycleStyle();
            } else if (hit(mx, my, x + 9, y + 114, 282)) {
                HudSettings.enabled = !HudSettings.enabled;
            } else if (hit(mx, my, x + 9, y + 136, 282)) {
                HudSettings.avoidXaeroMinimap = !HudSettings.avoidXaeroMinimap;
            } else if (hit(mx, my, x + 9, y + 159, 282)) {
                HudSettings.MinimapCorner[] corners = HudSettings.MinimapCorner.values();
                HudSettings.minimapCorner = corners[(HudSettings.minimapCorner.ordinal() + 1) % corners.length];
            } else return super.mouseClicked(event, doubleClick);
        } else {
            if (hit(mx, my, x + 8, y + 47, 110)) {
                HudSettings.audioEnabled = !HudSettings.audioEnabled;
            } else if (hit(mx, my, x + 126, y + 47, 45)) {
                HudSettings.audioVolume = Math.max(0, HudSettings.audioVolume - 10);
            } else if (hit(mx, my, x + 242, y + 47, 50)) {
                HudSettings.audioVolume = Math.min(100, HudSettings.audioVolume + 10);
            } else {
                for (int i = 0; i < LABELS.length; i++) {
                    int rowY = y + 84 + i * 19;
                    if (hit(mx, my, x + 122, rowY, 112)) {
                        HudSettings.alertSound[i] = FmsSounds.nextName(HudSettings.alertSound[i]);
                        HudSettings.save();
                        return true;
                    }
                    if (hit(mx, my, x + 240, rowY, 52)) {
                        FmsSounds.previewName(HudSettings.alertSound[i]);
                        return true;
                    }
                }
                return super.mouseClicked(event, doubleClick);
            }
        }
        HudSettings.save();
        return true;
    }

    @Override
    public void onClose() {
        HudSettings.save();
        minecraft.gui.setScreen(parent);
    }
}
