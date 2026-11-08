package cc.fentware.fms.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Simple clickable settings screen without relying on changing widget APIs.
 * Select a stock sound per warning; for custom audio replace sounds via a resource pack.
 */
public final class FmsSettingsScreen extends Screen {
    private static final int GREEN = 0xFF72F3A4;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int MUTED = 0xFFB0BBB4;
    private static final String[] LABELS = {"BINGO FUEL", "SINK RATE", "HIGH SPEED DESCENT", "CHECK WINGS", "WINGS CRITICAL"};
    private final Screen parent;

    public FmsSettingsScreen(Screen parent) {
        super(Component.literal("FMS Settings"));
        this.parent = parent;
    }

    private int originX() { return (width - 310) / 2; }
    private int originY() { return Math.max(12, (height - 272) / 2); }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        int x = originX(), y = originY();
        g.fill(x, y, x + 310, y + 268, 0xEF10221B);
        g.fill(x, y, x + 310, y + 2, GREEN);
        g.text(font, "FMS / SETTINGS", x + 10, y + 9, GREEN, true);
        drawButton(g, x + 12, y + 29, 286, "HUD MODE: " + HudSettings.hudMode.name());
        drawButton(g, x + 12, y + 52, 140, "STYLE: " + HudSettings.style.name());
        drawButton(g, x + 158, y + 52, 140, "HUD: " + (HudSettings.enabled ? "ON" : "OFF"));
        drawButton(g, x + 12, y + 76, 140, "AUDIO: " + (HudSettings.audioEnabled ? "ON" : "OFF"));
        drawButton(g, x + 158, y + 76, 43, "VOL -");
        drawButton(g, x + 208, y + 76, 40, HudSettings.audioVolume + "%");
        drawButton(g, x + 255, y + 76, 43, "VOL +");

        g.text(font, "ALERT", x + 13, y + 105, MUTED, false);
        g.text(font, "SOUND (CLICK TO CYCLE)", x + 121, y + 105, MUTED, false);
        for (int i = 0; i < LABELS.length; i++) {
            int rowY = y + 119 + i * 22;
            g.text(font, LABELS[i], x + 13, rowY + 5, WHITE, false);
            drawButton(g, x + 120, rowY, 126, HudSettings.alertSound[i]);
            drawButton(g, x + 250, rowY, 48, "TEST");
        }
        g.text(font, "Use resource packs to replace stock .ogg audio.", x + 12, y + 235, MUTED, false);
        drawButton(g, x + 230, y + 246, 68, "DONE");
    }

    private void drawButton(GuiGraphicsExtractor g, int x, int y, int w, String label) {
        g.fill(x, y, x + w, y + 19, 0xCC1D3D30);
        g.fill(x, y + 18, x + w, y + 19, 0xFF396B52);
        g.text(font, label, x + 4, y + 5, WHITE, false);
    }

    private static boolean hit(double px, double py, int x, int y, int w, int h) {
        return px >= x && px < x + w && py >= y && py < y + h;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x(), my = event.y();
        int x = originX(), y = originY();
        if (hit(mx, my, x + 12, y + 29, 286, 19)) {
            HudSettings.hudMode = HudSettings.HudMode.values()[(HudSettings.hudMode.ordinal() + 1) % HudSettings.HudMode.values().length];
        } else if (hit(mx, my, x + 12, y + 52, 140, 19)) {
            HudSettings.cycleStyle();
        } else if (hit(mx, my, x + 158, y + 52, 140, 19)) {
            HudSettings.enabled = !HudSettings.enabled;
        } else if (hit(mx, my, x + 12, y + 76, 140, 19)) {
            HudSettings.audioEnabled = !HudSettings.audioEnabled;
        } else if (hit(mx, my, x + 158, y + 76, 43, 19)) {
            HudSettings.audioVolume = Math.max(0, HudSettings.audioVolume - 10);
        } else if (hit(mx, my, x + 255, y + 76, 43, 19)) {
            HudSettings.audioVolume = Math.min(100, HudSettings.audioVolume + 10);
        } else if (hit(mx, my, x + 230, y + 246, 68, 19)) {
            onClose();
            return true;
        } else {
            for (int i = 0; i < LABELS.length; i++) {
                int rowY = y + 119 + i * 22;
                if (hit(mx, my, x + 120, rowY, 126, 19)) {
                    HudSettings.alertSound[i] = FmsSounds.nextName(HudSettings.alertSound[i]);
                    HudSettings.save();
                    return true;
                }
                if (hit(mx, my, x + 250, rowY, 48, 19)) {
                    FmsSounds.previewName(HudSettings.alertSound[i]);
                    return true;
                }
            }
            return super.mouseClicked(event, doubleClick);
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
