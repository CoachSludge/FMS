package cc.fentware.fms.client;

import net.minecraft.client.Minecraft;

/**
 * Conservative screen-space reserve for vanilla hotbar, XP, health, armor,
 * hunger, breath and mount status. This deliberately reserves space even if
 * individual vanilla widgets are temporarily hidden.
 */
public final class VanillaHudSafeArea {
    private VanillaHudSafeArea() {}

    public static int bottom(Minecraft client, int screenHeight) {
        boolean survival = client.player != null && !client.player.getAbilities().instabuild;
        // GUI-scaled pixels. Survival has two status rows + experience + hotbar.
        int reserve = survival ? 78 : 42;
        return Math.max(0, screenHeight - reserve);
    }

    public static int clampPanel(int y, int panelHeight, int screenHeight) {
        int available = bottom(Minecraft.getInstance(), screenHeight);
        return Math.max(4, Math.min(y, Math.max(4, available - panelHeight - 4)));
    }

    public static boolean textFits(int textY, int screenHeight) {
        return textY >= 3 && textY + 10 <= bottom(Minecraft.getInstance(), screenHeight);
    }
}
