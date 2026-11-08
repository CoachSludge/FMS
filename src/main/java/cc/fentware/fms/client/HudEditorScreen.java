package cc.fentware.fms.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/** Drag either panel; release saves relative positions into config/fms-hud.properties. */
public final class HudEditorScreen extends Screen {
    private int dragging = -1;
    private double offsetX;
    private double offsetY;

    public HudEditorScreen() { super(Component.literal("FMS HUD Layout")); }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        FmsHud.drawInstruments(g, FmsHud.px(HudSettings.instrumentsX, this.width), FmsHud.py(HudSettings.instrumentsY, this.height));
        FmsHud.drawNavigation(g, FmsHud.px(HudSettings.navigationX, this.width), FmsHud.py(HudSettings.navigationY, this.height));
        g.centeredText(this.font, "FMS HUD EDITOR  -  Drag panels  -  ESC to save", this.width / 2, 18, 0xFF72F3A4);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double x = event.x(), y = event.y();
        int ax = FmsHud.px(HudSettings.instrumentsX, width), ay = FmsHud.py(HudSettings.instrumentsY, height);
        int bx = FmsHud.px(HudSettings.navigationX, width), by = FmsHud.py(HudSettings.navigationY, height);
        if (inside(x, y, bx, by, FmsHud.NAV_HEIGHT)) { dragging = 1; offsetX = x - bx; offsetY = y - by; return true; }
        if (inside(x, y, ax, ay, FmsHud.INSTRUMENT_HEIGHT)) { dragging = 0; offsetX = x - ax; offsetY = y - ay; return true; }
        return super.mouseClicked(event, doubleClick);
    }

    private static boolean inside(double x, double y, int bx, int by, int h) {
        return x >= bx && x < bx + FmsHud.WIDTH && y >= by && y < by + h;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (dragging < 0) return super.mouseDragged(event, dx, dy);
        double nx = clamp((event.x() - offsetX) / Math.max(1, width - FmsHud.WIDTH));
        double ny = clamp((event.y() - offsetY) / Math.max(1, height - FmsHud.INSTRUMENT_HEIGHT));
        if (dragging == 0) { HudSettings.instrumentsX = nx; HudSettings.instrumentsY = ny; }
        else { HudSettings.navigationX = nx; HudSettings.navigationY = ny; }
        return true;
    }

    private static double clamp(double value) { return Math.max(0, Math.min(1, value)); }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (dragging != -1) { dragging = -1; HudSettings.save(); return true; }
        return super.mouseReleased(event);
    }

    @Override
    public void onClose() {
        HudSettings.save();
        super.onClose();
    }
}
