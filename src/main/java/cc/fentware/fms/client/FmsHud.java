package cc.fentware.fms.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.DeltaTracker;

import java.util.Locale;

/** Compact, client-only FMS status instruments. */
public final class FmsHud {
    static final int WIDTH = 164;
    static final int INSTRUMENT_HEIGHT = 112;
    static final int NAV_HEIGHT = 77;
    private static final int PANEL = 0xB8101B17;
    private static final int BORDER = 0xAA3A7657;
    private static final int MUTED = 0xFF9CB5A6;
    private static final int AMBER = 0xFFFFC46C;

    private FmsHud() {}

    public static void render(GuiGraphicsExtractor g, DeltaTracker tick) {
        Minecraft mc = Minecraft.getInstance();
        if (!HudSettings.enabled || mc.player == null || mc.gui.hud.isHidden()) return;
        drawInstruments(g, px(HudSettings.instrumentsX, g.guiWidth()), py(HudSettings.instrumentsY, g.guiHeight()));
        drawNavigation(g, px(HudSettings.navigationX, g.guiWidth()), py(HudSettings.navigationY, g.guiHeight()));
        drawWarning(g);
    }

    static int px(double norm, int screenWidth) {
        return (int) Math.round(norm * Math.max(0, screenWidth - WIDTH));
    }
    static int py(double norm, int screenHeight) {
        return (int) Math.round(norm * Math.max(0, screenHeight - INSTRUMENT_HEIGHT));
    }

    static void drawInstruments(GuiGraphicsExtractor g, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        Vec3 velocity = mc.player.getDeltaMovement();
        double speed = Math.hypot(velocity.x, velocity.z) * 20;
        double vsi = velocity.y * 20;
        ItemStack chest = mc.player.getItemBySlot(EquipmentSlot.CHEST);
        boolean wings = chest.is(Items.ELYTRA);
        int remaining = wings ? chest.getMaxDamage() - chest.getDamageValue() : 0;
        int percent = wings && chest.getMaxDamage() > 0 ? (int)(remaining * 100.0 / chest.getMaxDamage()) : 0;
        int rockets = mc.player.getInventory().countItem(Items.FIREWORK_ROCKET);

        panel(g, x, y, INSTRUMENT_HEIGHT, "FMS  /  FLIGHT DATA");
        line(g, x, y, 24, String.format(Locale.ROOT, "SPD   %5.1f B/S", speed), HudSettings.color);
        line(g, x, y, 36, String.format(Locale.ROOT, "ALT   Y %5.0f", mc.player.getY()), HudSettings.color);
        line(g, x, y, 48, String.format(Locale.ROOT, "V/S   %+6.1f B/S", vsi), vsi < -12 ? AMBER : HudSettings.color);
        line(g, x, y, 60, String.format(Locale.ROOT, "HDG   %5.0f DEG", (mc.player.getYRot() + 180 + 360) % 360), HudSettings.color);
        line(g, x, y, 72, wings ? "WING  " + remaining + " HP  /  " + percent + "%" : "WING  NOT EQUIPPED",
                percent <= 10 ? AMBER : HudSettings.color);
        line(g, x, y, 84, "FUEL  " + rockets + " ROCKETS", rockets <= 6 ? AMBER : HudSettings.color);
        line(g, x, y, 98, mc.player.isFallFlying() ? "STATUS ELYTRA FLIGHT" : "STATUS STANDBY", MUTED);
    }

    static void drawNavigation(GuiGraphicsExtractor g, int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        panel(g, x, y, NAV_HEIGHT, "FMS  /  NAVIGATION");
        line(g, x, y, 24, String.format(Locale.ROOT, "POS X %.0f Z %.0f", mc.player.getX(), mc.player.getZ()), HudSettings.color);
        if (FmsClient.hasDestination()) {
            double dx = FmsClient.destX() - mc.player.getX();
            double dz = FmsClient.destZ() - mc.player.getZ();
            double distance = Math.hypot(dx, dz);
            double speed = Math.hypot(mc.player.getDeltaMovement().x, mc.player.getDeltaMovement().z) * 20;
            line(g, x, y, 36, String.format(Locale.ROOT, "DIST  %.0f BLOCKS", distance), HudSettings.color);
            line(g, x, y, 48, speed > 0.5 ? "ETA   " + eta(distance / speed) : "ETA   --:--", HudSettings.color);
        } else {
            line(g, x, y, 36, "DEST  NOT SET", AMBER);
            line(g, x, y, 48, "ETA   --:--", MUTED);
        }
        line(g, x, y, 62, "UNKNOWN TERRAIN: Y " + HudSettings.unknownCruiseY, AMBER);
    }

    private static void drawWarning(GuiGraphicsExtractor g) {
        FlightWarnings.Alert alert = FlightWarnings.current();
        if (alert.severity() == FlightWarnings.Severity.NONE) return;

        Minecraft mc = Minecraft.getInstance();
        int x = Math.max(0, (g.guiWidth() - WIDTH) / 2);
        int y = Math.max(8, g.guiHeight() / 4);
        int color = alert.severity() == FlightWarnings.Severity.CRITICAL ? 0xFFFF6666 : AMBER;
        g.fill(x, y, x + WIDTH, y + 24, 0xD0201414);
        g.fill(x, y, x + WIDTH, y + 2, color);
        g.text(mc.font, "FMS WARNING", x + 7, y + 4, color, true);
        g.text(mc.font, alert.message(), x + 7, y + 14, color, true);
    }

    private static String eta(double seconds) {
        long total = Math.max(0, Math.round(seconds));
        return String.format(Locale.ROOT, "%02d:%02d", total / 60, total % 60);
    }

    private static void panel(GuiGraphicsExtractor g, int x, int y, int height, String title) {
        g.fill(x, y, x + WIDTH, y + height, PANEL);
        g.fill(x, y, x + WIDTH, y + 1, BORDER);
        g.fill(x, y + height - 1, x + WIDTH, y + height, BORDER);
        g.fill(x, y, x + 1, y + height, BORDER);
        g.fill(x + WIDTH - 1, y, x + WIDTH, y + height, BORDER);
        g.text(Minecraft.getInstance().font, title, x + 7, y + 7, 0xFFFFFFFF, true);
        g.fill(x + 6, y + 19, x + WIDTH - 6, y + 20, BORDER);
    }
    private static void line(GuiGraphicsExtractor g, int x, int y, int offset, String value, int color) {
        g.text(Minecraft.getInstance().font, value, x + 7, y + offset, color, true);
    }
}
