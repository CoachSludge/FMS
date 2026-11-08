package cc.fentware.fms.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

/** Fabric 26.3 entrypoint. All game-facing code stays in this package. */
public final class FmsClient implements ClientModInitializer {
    private static Double destinationX, destinationZ;

    public static boolean hasDestination() { return destinationX != null && destinationZ != null; }
    public static double destX() { return destinationX; }
    public static double destZ() { return destinationZ; }
    public static void setDestination(double x, double z) { destinationX = x; destinationZ = z; }
    public static void clearDestination() { destinationX = null; destinationZ = null; }

    @Override
    public void onInitializeClient() {
        HudSettings.load();
        FmsSounds.initialize();
        FmsCommands.register();
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("fms", "flight"));
        KeyMapping toggle = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.fms.toggle", InputConstants.Type.KEYBOARD, InputConstants.KEY_H, category));
        KeyMapping edit = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.fms.editor", InputConstants.Type.KEYBOARD, InputConstants.KEY_J, category));

        KeyMapping style = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.fms.style", InputConstants.Type.KEYBOARD, InputConstants.KEY_K, category));

        KeyMapping mute = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.fms.mute", InputConstants.Type.KEYBOARD, InputConstants.KEY_M, category));
        KeyMapping preview = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.fms.preview", InputConstants.Type.KEYBOARD, InputConstants.KEY_N, category));

        KeyMapping settings = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.fms.settings", InputConstants.Type.KEYBOARD, InputConstants.KEY_O, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (settings.consumeClick()) {
                if (client.player != null) client.gui.setScreen(new FmsSettingsScreen(null));
            }
            while (mute.consumeClick()) {
                HudSettings.audioEnabled = !HudSettings.audioEnabled;
                HudSettings.save();
                if (client.player != null) client.player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "[FMS] Warning sounds " + (HudSettings.audioEnabled ? "enabled" : "muted")));
            }
            while (preview.consumeClick()) FmsSounds.preview(FmsSounds.MASTER_CAUTION);
            WarningAudioController.tick(client);
            while (style.consumeClick()) HudSettings.cycleStyle();
            while (toggle.consumeClick()) { HudSettings.enabled = !HudSettings.enabled; HudSettings.save(); }
            while (edit.consumeClick()) {
                if (client.player != null) client.gui.setScreen(new HudEditorScreen());
            }
        });
        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("fms", "flight_hud"), FmsHud::render);
    }
}
